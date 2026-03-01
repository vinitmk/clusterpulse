package com.clusterpulse.backend.service;

import com.clusterpulse.backend.model.NodeMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class MetricsBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;
    private final MetricsService metricsService;

    public MetricsBroadcaster(SimpMessagingTemplate messagingTemplate, MetricsService metricsService) {
        this.messagingTemplate = messagingTemplate;
        this.metricsService = metricsService;
    }

    @Scheduled(fixedDelay = 1000)
    public void broadcastMetrics() {
        Map<String, NodeMetrics> latest = metricsService.getAllLatestMetrics();
        if (!latest.isEmpty()) {
            messagingTemplate.convertAndSend("/topic/metrics", latest);
        }
    }
}