package com.example.stockanalyzer.marketdata.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "runtime_leader")
public class RuntimeLeader {

    @Id
    @Column(name = "lock_name", length = 64)
    private String lockName;

    @Column(name = "owner_id", nullable = false, length = 64)
    private String ownerId;

    @Column(name = "heartbeat_at", nullable = false)
    private Instant heartbeatAt;
}
