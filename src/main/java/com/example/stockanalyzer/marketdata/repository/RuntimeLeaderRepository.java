package com.example.stockanalyzer.marketdata.repository;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.stockanalyzer.marketdata.entities.RuntimeLeader;

public interface RuntimeLeaderRepository extends JpaRepository<RuntimeLeader, String> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update RuntimeLeader r
            set r.ownerId = :ownerId, r.heartbeatAt = :now
            where r.lockName = :lockName
            and (r.ownerId = :ownerId or r.heartbeatAt < :staleBefore)
            """)
    int claim(
            @Param("lockName") String lockName,
            @Param("ownerId") String ownerId,
            @Param("now") Instant now,
            @Param("staleBefore") Instant staleBefore);
}
