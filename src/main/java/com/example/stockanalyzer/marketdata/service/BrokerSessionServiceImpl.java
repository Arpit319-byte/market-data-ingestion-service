package com.example.stockanalyzer.marketdata.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stockanalyzer.marketdata.entities.BrokerSession;
import com.example.stockanalyzer.marketdata.entities.BrokerSessionStatus;
import com.example.stockanalyzer.marketdata.entities.DataSource;
import com.example.stockanalyzer.marketdata.repository.BrokerSessionRepository;
import com.example.stockanalyzer.marketdata.security.TokenCipher;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrokerSessionServiceImpl implements BrokerSessionService {

    private final BrokerSessionRepository brokerSessionRepository;
    private final TokenCipher tokenCipher;

    @Override
    public BrokerSession save(BrokerSession brokerSession) {
        brokerSession.setAccessToken(tokenCipher.encrypt(brokerSession.getAccessToken()));
        brokerSession.setRefreshToken(tokenCipher.encrypt(brokerSession.getRefreshToken()));
        return brokerSessionRepository.save(brokerSession);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BrokerSession> findActiveSession(DataSource dataSource) {
        return brokerSessionRepository.findFirstByDataSourceAndStatusOrderByLoginTimeDesc(
                dataSource,
                BrokerSessionStatus.ACTIVE)
                .map(this::detachedCopy);
    }

    @Override
    @Transactional
    public void expireSession(DataSource dataSource) {
        findActiveSession(dataSource).ifPresent(session -> {
            session.setStatus(BrokerSessionStatus.EXPIRED);
            save(session);
        });
    }

    private BrokerSession detachedCopy(BrokerSession session) {
        BrokerSession copy = new BrokerSession();
        copy.setId(session.getId());
        copy.setCreatedAt(session.getCreatedAt());
        copy.setUpdatedAt(session.getUpdatedAt());
        copy.setDataSource(session.getDataSource());
        copy.setBrokerUserId(session.getBrokerUserId());
        copy.setAccessToken(tokenCipher.decrypt(session.getAccessToken()));
        copy.setRefreshToken(tokenCipher.decrypt(session.getRefreshToken()));
        copy.setLoginTime(session.getLoginTime());
        copy.setExpiresAt(session.getExpiresAt());
        copy.setStatus(session.getStatus());
        return copy;
    }
}
