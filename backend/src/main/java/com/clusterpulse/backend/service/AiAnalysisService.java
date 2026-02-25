package com.clusterpulse.backend.service;

import com.clusterpulse.backend.model.NodeMetrics;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiAnalysisService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final MetricsService metricsService;
    private final HttpClient httpClient;

    @Value("${ANTHROPIC_API_KEY:}")
    private String apiKey;

    @Value("${ANTHROPIC_MODEL:claude-haiku-4-5-20251001}")
    private String model;

    @Value("${ANTHROPIC_API_URL:https://api.anthropic.com/v1/messages}")
    private String apiUrl;

    public AiAnalysisService(MetricsService metricsService) {
        this.metricsService = metricsService;
        this.httpClient = HttpClient.newBuilder().build();
    }

    /**
     * Gets latest metrics for all nodes, builds a prompt, calls Claude, and returns the assistant text.
     */
    public String analyze() throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("ANTHROPIC_API_KEY is not set");
        }

        Map<String, NodeMetrics> latest = metricsService.getAllLatestMetrics();
        String clusterDescription = formatClusterState(latest);
        String prompt = buildPrompt(clusterDescription);

        ObjectNode body = OBJECT_MAPPER.createObjectNode();
        body.put("model", model);
        body.put("max_tokens", 1024);
        ArrayNode messages = OBJECT_MAPPER.createArrayNode();
        ObjectNode userMessage = OBJECT_MAPPER.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);
        body.set("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(body), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() != 200) {
            throw new RuntimeException("Anthropic API error: " + response.statusCode() + " " + response.body());
        }

        JsonNode root = OBJECT_MAPPER.readTree(response.body());
        JsonNode content = root.path("content");
        if (!content.isArray() || content.isEmpty()) {
            return "No analysis returned.";
        }
        JsonNode firstBlock = content.get(0);
        if (firstBlock.has("text")) {
            return firstBlock.get("text").asText();
        }
        return "No text in response.";
    }

    private String formatClusterState(Map<String, NodeMetrics> latest) {
        if (latest.isEmpty()) {
            return "No metrics available for any node.";
        }
        return latest.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    NodeMetrics m = e.getValue();
                    return String.format("%s: CPU %.1f%%, Memory %.1f%%, Latency %.1f ms (at %s)",
                            e.getKey(),
                            m.getCpuPercent(),
                            m.getMemoryPercent(),
                            m.getNetworkLatencyMs(),
                            m.getTimestamp() != null ? m.getTimestamp().toString() : "n/a");
                })
                .collect(Collectors.joining("\n"));
    }

    private String buildPrompt(String clusterDescription) {
        return "You are an SRE. Based on the following cluster metrics, give a 3–4 sentence plain English health summary of the cluster. "
                + "Focus on overall health, any nodes that look stressed (high CPU/memory or latency), and a brief recommendation if needed.\n\n"
                + "Cluster state (latest metrics per node):\n" + clusterDescription;
    }
}
