package com.clusterpulse.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "nodes")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Node {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nodeId;

    @Column(nullable = false)
    private String nodeName;

    @Column(nullable = false)
    private String region;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdDate;

    public static final Status DEFAULT_STATUS = Status.HEALTHY;
    public static final String DEFAULT_REGION = "us-east-1";

    public static Node create(String nodeId, String nodeName) {
        return Node.builder()
                .nodeId(nodeId)
                .nodeName(nodeName)
                .region(DEFAULT_REGION)
                .status(DEFAULT_STATUS)
                .build();
    }

    public enum Status {
        HEALTHY,
        UNHEALTHY,
        DEGRADED
    }
}
