# ClusterPulse 🖥️

> A distributed systems learning project built to master AI-assisted development using Cursor IDE.

---

## Why This Project Exists

I'm a senior engineer learning how to effectively use AI coding assistants as a force multiplier. Rather than doing toy exercises, I'm building a real-world infrastructure tooling project incrementally, using Cursor IDE as my primary AI pair programmer.

**End goal:** A full-stack infrastructure monitoring dashboard with AI-powered health narration, containerized and deployable to Kubernetes. Built one iteration at a time.

---

## Iterations

### ✅ Iteration 1 — Backend Core
Spring Boot foundation with a node metric simulator and REST API.

**What was built:**
- `NodeMetricSimulator` — generates realistic CPU, memory, and latency metrics for 5 nodes every 5 seconds with configurable spike probability
- `MetricsService` — thread-safe in-memory store with a per-node sliding window of last 100 readings
- `MetricsController` — REST API with 4 endpoints for querying live and historical metrics

**API endpoints:**
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/metrics` | Latest metrics for all nodes |
| GET | `/api/metrics/{nodeId}` | Latest metric for a specific node |
| GET | `/api/metrics/{nodeId}/history` | Full history window for a node |
| GET | `/api/nodes` | All nodes with current health status |

---

## Running Locally

**Prerequisites:** Java 17+, Maven
```bash
git clone https://github.com/vinitmk/clusterpulse.git
cd clusterpulse/backend
./mvnw spring-boot:run
```

---

*More iterations coming — persistence, React dashboard, AI narration, and Kubernetes.*