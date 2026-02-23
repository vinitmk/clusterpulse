package com.clusterpulse.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "node_metrics", indexes = {
        @Index(name = "idx_node_metrics_node_id", columnList = "node_id"),
        @Index(name = "idx_node_metrics_timestamp", columnList = "timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "node_fk")
    private Node node;

    @Column(name = "node_id", nullable = false, length = 255)
    private String nodeId;

    @Column(nullable = false)
    private double cpuPercent;

    @Column(nullable = false)
    private double memoryPercent;

    @Column(nullable = false)
    private double networkLatencyMs;

    @Column(nullable = false)
    private Instant timestamp;

    public static NodeMetrics of(String nodeId, double cpuPercent, double memoryPercent, double networkLatencyMs) {
        return NodeMetrics.builder()
                .nodeId(nodeId)
                .cpuPercent(cpuPercent)
                .memoryPercent(memoryPercent)
                .networkLatencyMs(networkLatencyMs)
                .timestamp(Instant.now())
                .build();
    }
}
