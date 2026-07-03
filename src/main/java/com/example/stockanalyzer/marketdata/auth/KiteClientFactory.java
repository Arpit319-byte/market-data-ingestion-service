package com.example.stockanalyzer.marketdata.auth;

import org.springframework.stereotype.Component;
import com.example.stockanalyzer.marketdata.entites.BrokerSession;
import com.example.stockanalyzer.marketdata.entites.DataSource;
import com.example.stockanalyzer.marketdata.exception.AuthenticationException;
import com.example.stockanalyzer.marketdata.service.BrokerSessionService;
import com.zerodhatech.kiteconnect.KiteConnect;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KiteClientFactory {

    private final BrokerSessionService brokerSessionService;

    public KiteConnect create(DataSource dataSource) {
        KiteConnect kiteConnect = new KiteConnect(dataSource.getApiKey());
        kiteConnect.setSessionExpiryHook(() -> brokerSessionService.expireSession(dataSource));
        return kiteConnect;
    }

    public KiteConnect createAuthenticated(DataSource dataSource) {
        BrokerSession session = brokerSessionService.findActiveSession(dataSource)
                .orElseThrow(() -> new AuthenticationException(
                        "No active Kite session for data source: " + dataSource.getName()));

        KiteConnect kiteConnect = create(dataSource);
        kiteConnect.setAccessToken(session.getAccessToken());
        return kiteConnect;
    }
}
