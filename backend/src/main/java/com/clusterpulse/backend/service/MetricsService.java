package com.clusterpulse.backend.service;

import com.clusterpulse.backend.model.NodeMetrics;
import com.clusterpulse.backend.model.NodeWindow;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class MetricsService {

    private static final int WINDOW_SIZE = 100;

    // Map: nodeId -> sliding window of metrics with its own lock
    private final ConcurrentMap<String, NodeWindow> metricsStore = new ConcurrentHashMap<>();

    /**
     * Adds a new NodeMetrics instance for the given node, maintaining a maximum window size.
     */
    public void addMetrics(NodeMetrics m) {
        // Atomically get or create the window for this nodeId and update it under its own lock.
        NodeWindow window = metricsStore.computeIfAbsent(
                m.nodeId(),
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
    }

    /**
     * Returns the latest NodeMetrics for the specified node ID, or null if none are available.
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
     * Returns a map of nodeId -> latest NodeMetrics for all nodes. (If window is empty for a node, excludes it.)
     */
    public Map<String, NodeMetrics> getAllLatestMetrics() {
        Map<String, NodeMetrics> latestMap = new HashMap<>();

        for (Map.Entry<String, NodeWindow> entry : metricsStore.entrySet()) {
            NodeWindow window = entry.getValue();

            window.getLock().lock();
            try {
                NodeMetrics latest = window.getDeque().peekLast();
                if (latest != null) {
                    latestMap.put(entry.getKey(), latest);
                }
            } finally {
                window.getLock().unlock();
            }
        }

        return latestMap;
    }

    /**
     * Returns an immutable list of the sliding window of metrics for the specified nodeId,
     * or an empty list if the node has no metrics yet.
     */
    public List<NodeMetrics> getMetricsWindow(String nodeId) {
        NodeWindow window = metricsStore.get(nodeId);
        if (window == null) return Collections.emptyList();

        window.getLock().lock();
        try {
            return List.copyOf(window.getDeque());
        } finally {
            window.getLock().unlock();
        }
    }
}