package com.clusterpulse.backend.controller;

import com.clusterpulse.backend.model.Node;
import com.clusterpulse.backend.model.NodeMetrics;
import com.clusterpulse.backend.service.AiAnalysisService;
import com.clusterpulse.backend.service.MetricsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class MetricsController {

    private final MetricsService metricsService;
    private final AiAnalysisService aiAnalysisService;

    // For demo: list of available node IDs and their display names
    private static final List<Node> NODES = List.of(
            Node.create("node-1", "Node 1"),
            Node.create("node-2", "Node 2"),
            Node.create("node-3", "Node 3"),
            Node.create("node-4", "Node 4"),
            Node.create("node-5", "Node 5")
    );

    public MetricsController(MetricsService metricsService, AiAnalysisService aiAnalysisService) {
        this.metricsService = metricsService;
        this.aiAnalysisService = aiAnalysisService;
    }

    /**
     * GET /api/metrics
     * Returns the latest metrics for all nodes.
     */
    @GetMapping("/metrics")
    public Map<String, NodeMetrics> getAllLatestMetrics() {
        return metricsService.getAllLatestMetrics();
    }

    /**
     * GET /api/metrics/{nodeId}
     * Returns the latest metrics for a given nodeId.
     */
    @GetMapping("/metrics/{nodeId}")
    public ResponseEntity<NodeMetrics> getLatestMetricsForNode(@PathVariable String nodeId) {
        Optional<Node> nodeOpt = NODES.stream().filter(n -> n.getNodeId().equals(nodeId)).findFirst();
        if (nodeOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        NodeMetrics metrics = metricsService.getLatestMetrics(nodeId);
        if (metrics == null) {
            // Node exists, but no data (treat this as 404 for metric)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(metrics);
    }

    /**
     * GET /api/metrics/{nodeId}/history
     * Returns the metrics window for a given nodeId.
     */
    @GetMapping("/metrics/{nodeId}/history")
    public ResponseEntity<List<NodeMetrics>> getMetricsWindowForNode(@PathVariable String nodeId) {
        Optional<Node> nodeOpt = NODES.stream().filter(n -> n.getNodeId().equals(nodeId)).findFirst();
        if (nodeOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        List<NodeMetrics> window = metricsService.getMetricsWindow(nodeId);
        if (window.isEmpty()) {
            // Node exists but no data
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(window);
    }

    /**
     * GET /api/nodes
     * Returns the list of all nodes with their current status.
     */
    @GetMapping("/nodes")
    public List<Map<String, Object>> getAllNodesWithStatus() {
        Map<String, NodeMetrics> latestMetrics = metricsService.getAllLatestMetrics();
        return NODES.stream().map(node -> {
            Map<String, Object> nodeInfo = new HashMap<>();
            nodeInfo.put("nodeId", node.getNodeId());
            nodeInfo.put("nodeName", node.getNodeName());
            nodeInfo.put("region", node.getRegion());
            nodeInfo.put("status", node.getStatus().name());
            NodeMetrics metrics = latestMetrics.get(node.getNodeId());
            nodeInfo.put("latestMetric", metrics);
            return nodeInfo;
        }).collect(Collectors.toList());
    }

    /**
     * POST /api/ai/analyze
     * Gets latest metrics for all nodes, sends them to Claude, and returns a 3–4 sentence SRE health summary as plain text.
     */
    @PostMapping(value = "/ai/analyze", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> analyzeCluster() {
        try {
            return ResponseEntity.ok(aiAnalysisService.analyze());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Analysis failed: " + e.getMessage());
        }
    }
}

