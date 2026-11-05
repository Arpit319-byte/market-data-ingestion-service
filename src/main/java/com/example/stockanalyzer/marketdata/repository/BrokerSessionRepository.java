package com.example.stockanalyzer.marketdata.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.stockanalyzer.marketdata.entities.BrokerSession;
import com.example.stockanalyzer.marketdata.entities.BrokerSessionStatus;
import com.example.stockanalyzer.marketdata.entities.DataSource;

public interface BrokerSessionRepository extends JpaRepository<BrokerSession, Long> {

    Optional<BrokerSession> findFirstByDataSourceAndStatusOrderByLoginTimeDesc(
            DataSource dataSource,
            BrokerSessionStatus status);
}
