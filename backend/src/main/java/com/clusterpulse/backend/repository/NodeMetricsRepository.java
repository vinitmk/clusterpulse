package com.clusterpulse.backend.repository;


import com.clusterpulse.backend.model.NodeMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NodeMetricsRepository extends JpaRepository<NodeMetrics, UUID> {

    // Find the latest metric for a given nodeId (ordered by timestamp desc, returns one)
    Optional<NodeMetrics> findFirstByNodeIdOrderByTimestampDesc(String nodeId);

    // Last 100 metrics for a node, newest first (for sliding window)
    List<NodeMetrics> findTop100ByNodeIdOrderByTimestampDesc(String nodeId);

    // Find all metrics for a nodeId within a given time range (inclusive or exclusive as needed)
    List<NodeMetrics> findAllByNodeIdAndTimestampBetween(String nodeId, Instant start, Instant end);

    // Find all metrics newer than a given instant
    List<NodeMetrics> findAllByTimestampAfter(Instant timestamp);

    // Custom query to get the latest metric per node (using subquery)
    @Query("""
        SELECT nm FROM NodeMetrics nm
        WHERE nm.timestamp = (
            SELECT MAX(nm2.timestamp)
            FROM NodeMetrics nm2
            WHERE nm2.nodeId = nm.nodeId
        )
    """)
    List<NodeMetrics> findLatestMetricPerNode();
}