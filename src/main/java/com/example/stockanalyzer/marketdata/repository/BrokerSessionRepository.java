package com.example.stockanalyzer.marketdata.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.stockanalyzer.marketdata.entites.BrokerSession;
import com.example.stockanalyzer.marketdata.entites.BrokerSessionStatus;
import com.example.stockanalyzer.marketdata.entites.DataSource;

public interface BrokerSessionRepository extends JpaRepository<BrokerSession, Long> {

    Optional<BrokerSession> findFirstByDataSourceAndStatusOrderByLoginTimeDesc(
            DataSource dataSource,
            BrokerSessionStatus status);
}
