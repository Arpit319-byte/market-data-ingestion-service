package com.example.stockanalyzer.marketdata.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stockanalyzer.marketdata.auth.KiteClientFactory;
import com.example.stockanalyzer.marketdata.entities.DataSource;
import com.example.stockanalyzer.marketdata.entities.Exchange;
import com.example.stockanalyzer.marketdata.entities.Instrument;
import com.example.stockanalyzer.marketdata.entities.InstrumentType;
import com.example.stockanalyzer.marketdata.entities.ProviderType;
import com.example.stockanalyzer.marketdata.exception.MarketDataException;
import com.example.stockanalyzer.marketdata.repository.DataSourceRepository;
import com.example.stockanalyzer.marketdata.repository.ExchangeRepository;
import com.example.stockanalyzer.marketdata.repository.InstrumentRepository;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstrumentSyncService {

    private final DataSourceRepository dataSourceRepository;
    private final ExchangeRepository exchangeRepository;
    private final InstrumentRepository instrumentRepository;
    private final KiteClientFactory kiteClientFactory;

    @Value("${kite.instruments.max:1300}")
    private int maxInstruments;

    @Transactional
    public int syncNseEquities() {
        Exchange nse = exchangeRepository.findByCode("NSE")
                .orElseThrow(() -> new MarketDataException("NSE exchange is not seeded"));
        DataSource kite = dataSourceRepository.findByIsActiveTrueOrderByPriorityAsc().stream()
                .filter(source -> ProviderType.KITE.equals(source.getProviderType()))
                .findFirst()
                .orElseThrow(() -> new MarketDataException("No active Kite data source configured"));

        List<com.zerodhatech.models.Instrument> dump;
        try {
            dump = kiteClientFactory.createAuthenticated(kite).getInstruments("NSE");
        } catch (KiteException | java.io.IOException | org.json.JSONException e) {
            throw new MarketDataException("Failed to download the Kite instrument master", e);
        }

        int saved = 0;
        for (com.zerodhatech.models.Instrument source : dump) {
            if (saved >= maxInstruments) {
                break;
            }
            if (!"EQ".equalsIgnoreCase(source.instrument_type) || !"NSE".equalsIgnoreCase(source.exchange)) {
                continue;
            }
            upsert(nse, source);
            saved++;
        }
        log.info("Synced {} NSE equity instruments", saved);
        return saved;
    }

    private void upsert(Exchange exchange, com.zerodhatech.models.Instrument source) {
        Instrument instrument = instrumentRepository.findBySymbolAndExchangeId(source.tradingsymbol, exchange.getId())
                .orElseGet(Instrument::new);
        instrument.setSymbol(source.tradingsymbol);
        instrument.setName(source.name == null || source.name.isBlank() ? source.tradingsymbol : source.name);
        instrument.setExchange(exchange);
        instrument.setSegment(source.segment == null ? "NSE" : source.segment);
        instrument.setInstrumentType(InstrumentType.EQUITY);
        instrument.setKiteInstrumentToken(source.instrument_token);
        instrument.setActive(true);
        instrumentRepository.save(instrument);
    }
}
