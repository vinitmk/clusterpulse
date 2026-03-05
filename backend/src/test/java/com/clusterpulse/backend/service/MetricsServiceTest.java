package com.clusterpulse.backend.service;

import com.clusterpulse.backend.model.NodeMetrics;
import com.clusterpulse.backend.repository.NodeMetricsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsServiceTest {

    @Mock
    private NodeMetricsRepository nodeMetricsRepository;

    @InjectMocks
    private MetricsService metricsService;

    private NodeMetrics metric1;
    private NodeMetrics metric2;

    @BeforeEach
    void setUp() {
        metric1 = NodeMetrics.of("node-1", 75.0, 60.0, 45.0);
        metric2 = NodeMetrics.of("node-1", 85.0, 70.0, 120.0);
    }

    @Test
    void addMetrics_savesToDatabase() {
        when(nodeMetricsRepository.save(any(NodeMetrics.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        metricsService.addMetrics(metric1);

        verify(nodeMetricsRepository, times(1)).save(metric1);
    }

    @Test
    void getLatestMetrics_returnsLastAddedMetric() {
        metricsService.addMetrics(metric1);
        metricsService.addMetrics(metric2);

        NodeMetrics latest = metricsService.getLatestMetrics("node-1");

        assertThat(latest).isEqualTo(metric2);
        assertThat(latest.getCpuPercent()).isEqualTo(85.0);
    }

    @Test
    void getLatestMetrics_forUnknownNode_returnsNull() {
        NodeMetrics result = metricsService.getLatestMetrics("unknown-node");

        assertThat(result).isNull();
    }

    @Test
    void getAllLatestMetrics_returnsMapFromDatabase() {
        NodeMetrics metricNode1 = NodeMetrics.of("node-1", 75.0, 60.0, 45.0);
        NodeMetrics metricNode2 = NodeMetrics.of("node-2", 85.0, 70.0, 120.0);

        when(nodeMetricsRepository.findLatestMetricPerNode())
                .thenReturn(Arrays.asList(metricNode1, metricNode2));

        Map<String, NodeMetrics> result = metricsService.getAllLatestMetrics();

        assertThat(result).hasSize(2);
        assertThat(result).containsKey("node-1");
        assertThat(result).containsKey("node-2");
        assertThat(result.get("node-1").getCpuPercent()).isEqualTo(75.0);
        assertThat(result.get("node-2").getCpuPercent()).isEqualTo(85.0);
    }

    @Test
    void getMetricsWindow_returnsTop100FromDatabase() {
        when(nodeMetricsRepository.findTop100ByNodeIdOrderByTimestampDesc("node-1"))
                .thenReturn(Arrays.asList(metric1, metric2));

        List<NodeMetrics> result = metricsService.getMetricsWindow("node-1");

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(metric1, metric2);
    }

    @Test
    void getMetricsWindow_forUnknownNode_returnsEmptyList() {
        when(nodeMetricsRepository.findTop100ByNodeIdOrderByTimestampDesc("unknown"))
                .thenReturn(Collections.emptyList());

        List<NodeMetrics> result = metricsService.getMetricsWindow("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    void addMetrics_multipleNodes_storesIndependently() {
        NodeMetrics nodeMetric1 = NodeMetrics.of("node-1", 50.0, 40.0, 30.0);
        NodeMetrics nodeMetric2 = NodeMetrics.of("node-2", 80.0, 60.0, 50.0);

        metricsService.addMetrics(nodeMetric1);
        metricsService.addMetrics(nodeMetric2);

        assertThat(metricsService.getLatestMetrics("node-1")).isEqualTo(nodeMetric1);
        assertThat(metricsService.getLatestMetrics("node-2")).isEqualTo(nodeMetric2);
        verify(nodeMetricsRepository, times(2)).save(any(NodeMetrics.class));
    }
}