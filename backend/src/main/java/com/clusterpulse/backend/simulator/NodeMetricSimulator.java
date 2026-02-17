package com.clusterpulse.backend.simulator;

import com.clusterpulse.backend.config.SimulatorProperties;
import com.clusterpulse.backend.model.NodeMetrics;
import com.clusterpulse.backend.service.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
public class NodeMetricSimulator {

    private static final Logger logger = LoggerFactory.getLogger(NodeMetricSimulator.class);

    private static final String[] NODE_IDS = {"node-1", "node-2", "node-3", "node-4", "node-5"};

    private final MetricsService metricsService;
    private final SimulatorProperties properties;
    private final Random random = new Random();

    // To produce drifting/slower trends for memory and cpu baseline
    private final Map<String, Double> lastCpu = new HashMap<>();
    private final Map<String, Double> lastMemory = new HashMap<>();

    public NodeMetricSimulator(MetricsService metricsService, SimulatorProperties properties) {
        this.metricsService = metricsService;
        this.properties = properties;
        // Initialize memory and CPU for each node
        for (String nodeId : NODE_IDS) {
            double cpuMin = properties.getCpu().getMin();
            double cpuMax = properties.getCpu().getMax();
            double memoryMin = properties.getMemory().getMin();
            double memoryMax = properties.getMemory().getMax();
            lastCpu.put(nodeId, cpuMin + random.nextDouble() * (cpuMax - cpuMin));
            lastMemory.put(nodeId, memoryMin + random.nextDouble() * (memoryMax - memoryMin));
        }
    }

    @Scheduled(fixedRate = 5000)
    public void generateMetrics() {
        StringBuilder summary = new StringBuilder("Simulated NodeMetrics: ");

        for (String nodeId : NODE_IDS) {
            // CPU: fluctuate quickly, with spike
            double cpuMin = properties.getCpu().getMin();
            double cpuMax = properties.getCpu().getMax();
            double spikeProbability = properties.getSpikeProbability();
            double cpu = driftValue(lastCpu.get(nodeId), cpuMin, cpuMax, 8.0, 15.0, spikeProbability, 85.0, 95.0);
            lastCpu.put(nodeId, cpu);

            // Memory: slow drift, occasionally moves + or - a bit, stays within [MEMORY_MIN, MEMORY_MAX]
            double memoryMin = properties.getMemory().getMin();
            double memoryMax = properties.getMemory().getMax();
            double memory = restrictToRange(
                    lastMemory.get(nodeId) + (random.nextGaussian() * 2.0),
                    memoryMin, memoryMax
            );
            lastMemory.put(nodeId, memory);

            // Network Latency: mostly low, rare high spike
            double latency;
            if (random.nextDouble() < spikeProbability) {
                double latencySpikeMin = properties.getLatency().getSpike().getMin();
                double latencySpikeMax = properties.getLatency().getSpike().getMax();
                latency = latencySpikeMin + random.nextDouble() * (latencySpikeMax - latencySpikeMin);
            } else {
                double latencyNormalMin = properties.getLatency().getNormal().getMin();
                double latencyNormalMax = properties.getLatency().getNormal().getMax();
                latency = latencyNormalMin + random.nextDouble() * (latencyNormalMax - latencyNormalMin);
            }

            NodeMetrics metrics = NodeMetrics.of(
                    nodeId,
                    round(cpu),
                    round(memory),
                    round(latency)
            );
            metricsService.addMetrics(metrics);
            summary.append(String.format("[%s: CPU=%.1f%%, Mem=%.1f%%, Lat=%.1fms] ", nodeId, cpu, memory, latency));
        }

        logger.info(summary.toString().trim());
    }

    private double driftValue(
            double lastValue,
            double min,
            double max,
            double drift,
            double spikeAmount,
            double spikeProbability,
            double spikeMin,
            double spikeMax
    ) {
        double newValue;
        if (random.nextDouble() < spikeProbability) {
            // Occasional spike
            newValue = spikeMin + random.nextDouble() * (spikeMax - spikeMin);
        } else {
            // Drift up or down, normal fluctuation
            newValue = lastValue + (random.nextGaussian() * drift);
            if (newValue < min) newValue = min + random.nextDouble() * Math.abs(drift) * 2;
            if (newValue > max) newValue = max - random.nextDouble() * Math.abs(drift) * 2;
        }
        return restrictToRange(newValue, min, max);
    }

    private double restrictToRange(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}