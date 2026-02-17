package com.clusterpulse.backend.model;

public record NodeMetrics(
    String nodeId,
    double cpuPercent,
    double memoryPercent,
    double networkLatencyMs,
    long timestamp
) {
    public static NodeMetrics of(String nodeId, double cpuPercent, double memoryPercent, double networkLatencyMs) {
        return new NodeMetrics(nodeId, cpuPercent, memoryPercent, networkLatencyMs, System.currentTimeMillis());
    }
}