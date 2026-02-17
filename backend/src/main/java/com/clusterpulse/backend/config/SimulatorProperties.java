package com.clusterpulse.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "simulator.metrics")
public class SimulatorProperties {

    private double spikeProbability = 0.07;

    private CpuThresholds cpu = new CpuThresholds();
    private MemoryThresholds memory = new MemoryThresholds();
    private LatencyThresholds latency = new LatencyThresholds();

    public double getSpikeProbability() {
        return spikeProbability;
    }

    public void setSpikeProbability(double spikeProbability) {
        this.spikeProbability = spikeProbability;
    }

    public CpuThresholds getCpu() {
        return cpu;
    }

    public void setCpu(CpuThresholds cpu) {
        this.cpu = cpu;
    }

    public MemoryThresholds getMemory() {
        return memory;
    }

    public void setMemory(MemoryThresholds memory) {
        this.memory = memory;
    }

    public LatencyThresholds getLatency() {
        return latency;
    }

    public void setLatency(LatencyThresholds latency) {
        this.latency = latency;
    }

    public static class CpuThresholds {
        private double min = 10.0;
        private double max = 95.0;

        public double getMin() {
            return min;
        }

        public void setMin(double min) {
            this.min = min;
        }

        public double getMax() {
            return max;
        }

        public void setMax(double max) {
            this.max = max;
        }
    }

    public static class MemoryThresholds {
        private double min = 40.0;
        private double max = 90.0;

        public double getMin() {
            return min;
        }

        public void setMin(double min) {
            this.min = min;
        }

        public double getMax() {
            return max;
        }

        public void setMax(double max) {
            this.max = max;
        }
    }

    public static class LatencyThresholds {
        private Normal normal = new Normal();
        private Spike spike = new Spike();

        public Normal getNormal() {
            return normal;
        }

        public void setNormal(Normal normal) {
            this.normal = normal;
        }

        public Spike getSpike() {
            return spike;
        }

        public void setSpike(Spike spike) {
            this.spike = spike;
        }

        public static class Normal {
            private double min = 5.0;
            private double max = 50.0;

            public double getMin() {
                return min;
            }

            public void setMin(double min) {
                this.min = min;
            }

            public double getMax() {
                return max;
            }

            public void setMax(double max) {
                this.max = max;
            }
        }

        public static class Spike {
            private double min = 200.0;
            private double max = 500.0;

            public double getMin() {
                return min;
            }

            public void setMin(double min) {
                this.min = min;
            }

            public double getMax() {
                return max;
            }

            public void setMax(double max) {
                this.max = max;
            }
        }
    }
}
