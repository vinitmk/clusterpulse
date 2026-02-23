package com.clusterpulse.backend.service;

import com.clusterpulse.backend.model.NodeMetrics;
import com.clusterpulse.backend.model.NodeWindow;
import com.clusterpulse.backend.repository.NodeMetricsRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class MetricsService {

    private static final int WINDOW_SIZE = 100;

    private final NodeMetricsRepository nodeMetricsRepository;

    // In-memory cache: nodeId -> sliding window of latest metrics (fast path for single-node latest)
    private final ConcurrentMap<String, NodeWindow> metricsStore = new ConcurrentHashMap<>();

    public MetricsService(NodeMetricsRepository nodeMetricsRepository) {
        this.nodeMetricsRepository = nodeMetricsRepository;
    }

    /**
     * Adds a new NodeMetrics instance: writes to both in-memory cache and database.
     */
    public void addMetrics(NodeMetrics m) {
        // Update in-memory cache
        NodeWindow window = metricsStore.computeIfAbsent(
                m.getNodeId(),
                k -> new NodeWindow(new ArrayDeque<>(WINDOW_SIZE))
        );

        window.getLock().lock();
        try {
            if (window.getDeque().size() == WINDOW_SIZE) {
                window.getDeque().removeFirst();
            }
            window.getDeque().addLast(m);
        } finally {
            window.getLock().unlock();
        }

        // Persist to database (same entity may get id assigned after save)
        nodeMetricsRepository.save(m);
    }

    /**
     * Returns the latest NodeMetrics for the specified node ID from the cache, or null if none.
     */
    public NodeMetrics getLatestMetrics(String nodeId) {
        NodeWindow window = metricsStore.get(nodeId);
        if (window == null) return null;

        window.getLock().lock();
        try {
            return window.getDeque().peekLast();
        } finally {
            window.getLock().unlock();
        }
    }

    /**
     * Returns a map of nodeId -> latest NodeMetrics for all nodes, from the database.
     */
    public Map<String, NodeMetrics> getAllLatestMetrics() {
        return nodeMetricsRepository.findLatestMetricPerNode().stream()
                .collect(Collectors.toMap(NodeMetrics::getNodeId, nm -> nm, (a, b) -> a));
    }

    /**
     * Returns the last 100 metrics for the given node, newest first, from the database.
     */
    public List<NodeMetrics> getMetricsWindow(String nodeId) {
        List<NodeMetrics> list = nodeMetricsRepository.findTop100ByNodeIdOrderByTimestampDesc(nodeId);
        return list != null ? list : Collections.emptyList();
    }
}
