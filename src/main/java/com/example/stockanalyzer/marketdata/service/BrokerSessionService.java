package com.example.stockanalyzer.marketdata.service;

import java.util.Optional;
import com.example.stockanalyzer.marketdata.entites.BrokerSession;
import com.example.stockanalyzer.marketdata.entites.DataSource;

public interface BrokerSessionService {

    BrokerSession save(BrokerSession brokerSession);

    Optional<BrokerSession> findActiveSession(DataSource dataSource);

    void expireSession(DataSource dataSource);
}
