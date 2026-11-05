package com.example.stockanalyzer.marketdata.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.stockanalyzer.marketdata.entities.RuntimeLeader;
import com.example.stockanalyzer.marketdata.repository.RuntimeLeaderRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ClusterLeaderService {

    public static final String LOCK_NAME = "cluster-leader";

    private final RuntimeLeaderRepository runtimeLeaderRepository;
    private final TransactionTemplate transactionTemplate;
    private final TransactionTemplate insertTemplate;
    private final long staleSeconds;
    private final String instanceId = UUID.randomUUID().toString();
    private volatile boolean leader;

    public ClusterLeaderService(
            RuntimeLeaderRepository runtimeLeaderRepository,
            PlatformTransactionManager transactionManager,
            @Value("${app.cluster.leader-stale-seconds:60}") long staleSeconds) {
        this.runtimeLeaderRepository = runtimeLeaderRepository;
        this.staleSeconds = staleSeconds;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.insertTemplate = new TransactionTemplate(transactionManager);
        this.insertTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public boolean renew() {
        return claim(instanceId);
    }

    public boolean claim(String ownerId) {
        boolean won = Boolean.TRUE.equals(transactionTemplate.execute(status -> claimInTransaction(ownerId)));
        if (instanceId.equals(ownerId)) {
            if (won != leader) {
                log.info("Cluster leadership {}", won ? "acquired" : "lost");
            }
            leader = won;
        }
        return won;
    }

    public boolean isLeader() {
        return leader;
    }

    public String instanceId() {
        return instanceId;
    }

    private boolean claimInTransaction(String ownerId) {
        Instant now = Instant.now();
        Instant staleBefore = now.minusSeconds(staleSeconds);
        if (runtimeLeaderRepository.claim(LOCK_NAME, ownerId, now, staleBefore) > 0) {
            return true;
        }
        if (runtimeLeaderRepository.existsById(LOCK_NAME)) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(insertTemplate.execute(status -> {
                runtimeLeaderRepository.saveAndFlush(new RuntimeLeader(LOCK_NAME, ownerId, now));
                return true;
            }));
        } catch (DataIntegrityViolationException e) {
            return runtimeLeaderRepository.claim(LOCK_NAME, ownerId, Instant.now(), staleBefore) > 0;
        }
    }
}
