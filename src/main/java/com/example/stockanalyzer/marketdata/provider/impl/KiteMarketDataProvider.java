package com.example.stockanalyzer.marketdata.provider.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;
import com.example.stockanalyzer.marketdata.auth.ProviderAuthRegistry;
import com.example.stockanalyzer.marketdata.auth.KiteClientFactory;
import com.example.stockanalyzer.marketdata.dto.FetchCandlesRequest;
import com.example.stockanalyzer.marketdata.entites.Candle;
import com.example.stockanalyzer.marketdata.entites.DataSource;
import com.example.stockanalyzer.marketdata.entites.Instrument;
import com.example.stockanalyzer.marketdata.entites.IntervalType;
import com.example.stockanalyzer.marketdata.entites.ProviderType;
import com.example.stockanalyzer.marketdata.exception.MarketDataException;
import com.example.stockanalyzer.marketdata.mapper.KiteCandleMapper;
import com.example.stockanalyzer.marketdata.mapper.KiteIntervalMapper;
import com.example.stockanalyzer.marketdata.provider.MarketDataProvider;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.HistoricalData;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KiteMarketDataProvider implements MarketDataProvider {

    private final KiteClientFactory kiteClientFactory;
    private final ProviderAuthRegistry authRegistry;

    @Override
    public boolean supports(DataSource dataSource) {
        return dataSource != null
                && (ProviderType.KITE.equals(dataSource.getProviderType())
                || dataSource.getName().toLowerCase().contains("kite"));
    }

    @Override
    public String getProviderName() {
        return "Kite";
    }

    @Override
    public List<Candle> fetchCandles(
            DataSource dataSource,
            Instrument instrument,
            FetchCandlesRequest request) {

        if (!authRegistry.resolve(dataSource).hasValidSession(dataSource)) {
            throw new MarketDataException("No valid Kite session for data source: " + dataSource.getName());
        }
        if (instrument.getKiteInstrumentToken() == null) {
            throw new MarketDataException("Missing Kite instrument token for: " + instrument.getSymbol());
        }

        IntervalType intervalType = request.getIntervalType() != null
                ? request.getIntervalType()
                : IntervalType.ONE_DAY;
        Instant to = request.getTo() != null ? request.getTo() : Instant.now();
        Instant from = request.getFrom() != null
                ? request.getFrom()
                : to.minusSeconds(intervalType.getMinutes() * 60L * 3L);

        try {
            KiteConnect kiteConnect = kiteClientFactory.createAuthenticated(dataSource);
            HistoricalData response = kiteConnect.getHistoricalData(
                    Date.from(from),
                    Date.from(to),
                    String.valueOf(instrument.getKiteInstrumentToken()),
                    KiteIntervalMapper.toKiteInterval(intervalType),
                    false,
                    false);

            ZoneId exchangeZone = ZoneId.of(instrument.getExchange().getTimezone());
            return response.dataArrayList.stream()
                    .map(bar -> KiteCandleMapper.toCandle(instrument, intervalType, bar, exchangeZone))
                    .toList();
        } catch (Exception e) {
            throw new MarketDataException("Failed to fetch Kite candles for: " + instrument.getSymbol(), e);
        } catch (KiteException e) {
            throw new MarketDataException("Failed to fetch Kite candles for: " + instrument.getSymbol(), e);
        }
    }
}
