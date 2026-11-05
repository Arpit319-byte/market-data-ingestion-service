package com.example.stockanalyzer.marketdata.stream.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.example.stockanalyzer.marketdata.auth.KiteCredentials;
import com.example.stockanalyzer.marketdata.entities.BrokerSession;
import com.example.stockanalyzer.marketdata.entities.DataSource;
import com.example.stockanalyzer.marketdata.entities.Instrument;
import com.example.stockanalyzer.marketdata.entities.ProviderType;
import com.example.stockanalyzer.marketdata.exception.AuthenticationException;
import com.example.stockanalyzer.marketdata.exception.MarketDataException;
import com.example.stockanalyzer.marketdata.service.BrokerSessionService;
import com.example.stockanalyzer.marketdata.stream.MarketDataStream;
import com.example.stockanalyzer.marketdata.stream.TradeTick;
import com.example.stockanalyzer.marketdata.stream.TradeTickListener;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Tick;
import com.zerodhatech.ticker.KiteTicker;
import com.zerodhatech.ticker.OnError;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "kite.stream", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KiteLiveMarketDataStream implements MarketDataStream {

    private static final int MAX_TOKENS_PER_CONNECTION = 3000;

    private final BrokerSessionService brokerSessionService;
    private final TradeTickListener tradeTickListener;
    private final KiteCredentials kiteCredentials;
    private final Object lock = new Object();

    private KiteTicker ticker;
    private volatile boolean running;
    private volatile Map<Long, Instrument> instrumentsByToken = Map.of();

    @Override
    public boolean supports(DataSource dataSource) {
        return dataSource != null
                && (ProviderType.KITE.equals(dataSource.getProviderType())
                || (dataSource.getName() != null && dataSource.getName().toLowerCase().contains("kite")));
    }

    @Override
    public void start(DataSource dataSource, List<Instrument> instruments) {
        String apiKey = kiteCredentials.apiKey(dataSource);

        ArrayList<Long> tokens = new ArrayList<>();
        Map<Long, Instrument> tokenIndex = new HashMap<>();
        for (Instrument instrument : instruments) {
            if (instrument.getKiteInstrumentToken() != null) {
                tokens.add(instrument.getKiteInstrumentToken());
                tokenIndex.put(instrument.getKiteInstrumentToken(), instrument);
            }
        }
        if (tokens.isEmpty()) {
            log.warn("No instruments with a Kite token are active; live stream was not started");
            return;
        }
        if (tokens.size() > MAX_TOKENS_PER_CONNECTION) {
            log.warn("Subscribing to the first {} of {} instruments; one Kite connection cannot exceed that",
                    MAX_TOKENS_PER_CONNECTION, tokens.size());
            tokens.subList(MAX_TOKENS_PER_CONNECTION, tokens.size()).clear();
        }

        BrokerSession session = brokerSessionService.findActiveSession(dataSource)
                .orElseThrow(() -> new AuthenticationException(
                        "No active Kite session for data source: " + dataSource.getName()));

        synchronized (lock) {
            if (running && ticker != null && ticker.isConnectionOpen()) {
                log.info("Kite live stream is already connected");
                return;
            }
            disconnectQuietly();
            instrumentsByToken = Map.copyOf(tokenIndex);

            KiteTicker connection = new KiteTicker(session.getAccessToken(), apiKey);
            connection.setTryReconnection(true);
            try {
                connection.setMaximumRetries(10);
                connection.setMaximumRetryInterval(30);
            } catch (KiteException e) {
                throw new MarketDataException("Failed to configure Kite live stream retries", e);
            }

            connection.setOnConnectedListener(() -> {
                connection.subscribe(tokens);
                connection.setMode(tokens, KiteTicker.modeFull);
                log.info("Kite live stream subscribed to {} instruments", tokens.size());
            });
            connection.setOnDisconnectedListener(() -> log.warn("Kite live stream disconnected"));
            connection.setOnErrorListener(loggingErrorListener());
            connection.setOnTickerArrivalListener(ticks -> tradeTickListener.onTicks(toTradeTicks(ticks)));
            try {
                connection.connect();
            } catch (RuntimeException e) {
                disconnect(connection);
                throw new MarketDataException("Failed to connect Kite live stream", e);
            }
            ticker = connection;
            running = true;
        }
    }

    @Override
    public void stop() {
        synchronized (lock) {
            disconnectQuietly();
            running = false;
            instrumentsByToken = Map.of();
        }
    }

    @Override
    public boolean isRunning() {
        return running && ticker != null && ticker.isConnectionOpen();
    }

    private List<TradeTick> toTradeTicks(List<Tick> ticks) {
        List<TradeTick> tradeTicks = new ArrayList<>();
        for (Tick tick : ticks) {
            Instrument instrument = instrumentsByToken.get(tick.getInstrumentToken());
            if (instrument == null || tick.getLastTradedPrice() <= 0) {
                continue;
            }
            Instant timestamp = tick.getTickTimestamp() != null
                    ? tick.getTickTimestamp().toInstant()
                    : Instant.now();
            tradeTicks.add(new TradeTick(
                    instrument,
                    timestamp,
                    BigDecimal.valueOf(tick.getLastTradedPrice()),
                    Math.max(0L, tick.getVolumeTradedToday())));
        }
        return tradeTicks;
    }

    private OnError loggingErrorListener() {
        return new OnError() {
            @Override
            public void onError(Exception exception) {
                log.error("Kite live stream error", exception);
            }

            @Override
            public void onError(KiteException exception) {
                log.error("Kite live stream error", exception);
            }

            @Override
            public void onError(String message) {
                log.error("Kite live stream error: {}", message);
            }
        };
    }

    private void disconnectQuietly() {
        if (ticker == null) {
            return;
        }
        disconnect(ticker);
        ticker = null;
    }

    private void disconnect(KiteTicker connection) {
        try {
            if (connection.isConnectionOpen()) {
                connection.disconnect();
            }
        } catch (RuntimeException e) {
            log.warn("Failed to disconnect Kite live stream", e);
        }
    }
}
