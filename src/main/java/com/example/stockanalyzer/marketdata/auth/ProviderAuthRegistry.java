package com.example.stockanalyzer.marketdata.auth;

import java.util.List;
import org.springframework.stereotype.Component;
import com.example.stockanalyzer.marketdata.entites.DataSource;
import com.example.stockanalyzer.marketdata.exception.MarketDataException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProviderAuthRegistry {

    private final List<ProviderAuthService> authServices;

    public ProviderAuthService resolve(DataSource dataSource) {
        return authServices.stream()
                .filter(service -> service.supports(dataSource))
                .findFirst()
                .orElseThrow(() -> new MarketDataException(
                        "No auth service found for data source: " + dataSource.getName()));
    }
}
