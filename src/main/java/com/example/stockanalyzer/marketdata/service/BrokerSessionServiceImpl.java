package com.example.stockanalyzer.marketdata.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stockanalyzer.marketdata.entites.BrokerSession;
import com.example.stockanalyzer.marketdata.entites.BrokerSessionStatus;
import com.example.stockanalyzer.marketdata.entites.DataSource;
import com.example.stockanalyzer.marketdata.repository.BrokerSessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrokerSessionServiceImpl implements BrokerSessionService {

    private final BrokerSessionRepository brokerSessionRepository;

    @Override
    public BrokerSession save(BrokerSession brokerSession) {
        return brokerSessionRepository.save(brokerSession);
    }

    @Override
    public Optional<BrokerSession> findActiveSession(DataSource dataSource) {
        return brokerSessionRepository.findFirstByDataSourceAndStatusOrderByLoginTimeDesc(
                dataSource,
                BrokerSessionStatus.ACTIVE);
    }

    @Override
    @Transactional
    public void expireSession(DataSource dataSource) {
        findActiveSession(dataSource).ifPresent(session -> {
            session.setStatus(BrokerSessionStatus.EXPIRED);
            brokerSessionRepository.save(session);
        });
    }
}
