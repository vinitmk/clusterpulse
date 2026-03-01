package com.clusterpulse.backend.service;

import com.clusterpulse.backend.model.NodeMetrics;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class AlertService {

    private final MetricsService metricsService;
    private final JavaMailSender mailSender;

    @Value("${alert.cpu.threshold}")
    private double cpuThreshold;

    @Value("${alert.latency.threshold}")
    private double latencyThreshold;

    @Value("${alert.cooldown.minutes}")
    private int cooldownMinutes;

    @Value("${alert.recipient}")
    private String recipient;

    // Tracks last alert time per node to enforce cooldown
    private final ConcurrentHashMap<String, Instant> lastAlertTime = new ConcurrentHashMap<>();

    public AlertService(MetricsService metricsService, JavaMailSender mailSender) {
        this.metricsService = metricsService;
        this.mailSender = mailSender;
    }

    @Scheduled(fixedDelay = 5000)
    public void evaluateAlerts() {
        Map<String, NodeMetrics> latest = metricsService.getAllLatestMetrics();

        for (Map.Entry<String, NodeMetrics> entry : latest.entrySet()) {
            String nodeId = entry.getKey();
            NodeMetrics m = entry.getValue();

            boolean cpuAlert = m.getCpuPercent() > cpuThreshold;
            boolean latencyAlert = m.getNetworkLatencyMs() > latencyThreshold;

            if ((cpuAlert || latencyAlert) && !isInCooldown(nodeId)) {
                String subject = buildSubject(nodeId, cpuAlert, latencyAlert);
                String body = buildBody(nodeId, m, cpuAlert, latencyAlert);
                sendAlert(subject, body);
                lastAlertTime.put(nodeId, Instant.now());
                log.warn("Alert fired for {} - CPU: {}% Latency: {}ms", 
                    nodeId, m.getCpuPercent(), m.getNetworkLatencyMs());
            }
        }
    }

    private boolean isInCooldown(String nodeId) {
        Instant last = lastAlertTime.get(nodeId);
        if (last == null) return false;
        return Instant.now().isBefore(last.plusSeconds(cooldownMinutes * 60L));
    }

    private String buildSubject(String nodeId, boolean cpuAlert, boolean latencyAlert) {
        if (cpuAlert && latencyAlert) {
            return "🚨 ClusterPulse Alert: " + nodeId + " — High CPU + High Latency";
        } else if (cpuAlert) {
            return "⚠️ ClusterPulse Alert: " + nodeId + " — High CPU";
        } else {
            return "⚠️ ClusterPulse Alert: " + nodeId + " — High Latency";
        }
    }

    private String buildBody(String nodeId, NodeMetrics m, boolean cpuAlert, boolean latencyAlert) {
        StringBuilder sb = new StringBuilder();
        sb.append("ClusterPulse has detected an anomaly on ").append(nodeId).append(":\n\n");
        if (cpuAlert) {
            sb.append("🔴 CPU: ").append(String.format("%.1f", m.getCpuPercent()))
              .append("% (threshold: ").append(cpuThreshold).append("%)\n");
        }
        if (latencyAlert) {
            sb.append("🔴 Latency: ").append(String.format("%.1f", m.getNetworkLatencyMs()))
              .append("ms (threshold: ").append(latencyThreshold).append("ms)\n");
        }
        sb.append("\nMemory: ").append(String.format("%.1f", m.getMemoryPercent())).append("%");
        sb.append("\nTimestamp: ").append(m.getTimestamp());
        sb.append("\n\n— ClusterPulse Monitoring");
        return sb.toString();
    }

    private void sendAlert(String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(body);
            mailSender.send(message);
            log.info("Alert email sent to {}", recipient);
        } catch (Exception e) {
            log.error("Failed to send alert email: {}", e.getMessage());
        }
    }
}