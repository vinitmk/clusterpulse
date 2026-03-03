# ClusterPulse 🖥️

> A distributed systems learning project built to master AI-assisted development using Cursor IDE.

---

## What This Is

ClusterPulse is a full-stack infrastructure monitoring platform built to demonstrate production-grade software engineering across the entire stack — from database to cloud deployment.

It monitors a simulated 5-node cluster in real time, visualizes metrics via a React dashboard and Grafana, fires email alerts on anomalies, and uses Claude AI to narrate cluster health in plain English.

**Built iteratively over 5 iterations, each adding a production concern:**

| | What | Why It Matters |
|-|------|----------------|
| 1 | Spring Boot REST API + metric simulator | Backend foundation |
| 2 | TimescaleDB + Docker Compose | Time-series persistence |
| 3 | React dashboard + Claude AI narrator | Full stack + AI integration |
| 4 | JUnit tests + Kubernetes + GitHub Actions CI/CD | Production engineering |
| 5 | Email alerts + Grafana + WebSocket + AWS ECS | Observability + cloud |

**Tech:** Java 17 · Spring Boot 4 · TimescaleDB · React · Recharts · Docker · Kubernetes · GitHub Actions · Grafana · WebSocket (STOMP) · Anthropic Claude API · AWS ECS Fargate

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

### ✅ Iteration 2 — Persistence + Containerization
TimescaleDB persistence and full Docker Compose stack.

**What was built:**
- `Node` and `NodeMetrics` — converted to JPA entities with proper relationships and auditing
- `NodeRepository` and `NodeMetricsRepository` — Spring Data JPA repositories with time-series query methods
- `DataInitializer` — seeds 5 nodes on startup, idempotent (skips if already exists)
- `Dockerfile` — multi-stage build for minimal production image
- `docker-compose.yml` — runs the full stack (backend + TimescaleDB) with a single command

**Key decisions:**
- TimescaleDB (PostgreSQL extension) chosen for native time-series performance
- Hibernate `ddl-auto=update` used for schema management in dev
- Docker volume ensures metric data persists across container restarts
- Port 5433 used for TimescaleDB external access to avoid conflict with local PostgreSQL

**Running with Docker:**
```bash
git clone https://github.com/vinitmk/clusterpulse.git
cd clusterpulse
docker compose up --build
```

Metrics persist to TimescaleDB and survive container restarts. Verified 1,600+ rows flowing after a single session.

---

### ✅ Iteration 3 — React Dashboard + AI Narrator
Live frontend dashboard with AI-powered cluster health analysis.

**What was built:**
- `NodeGrid` — health cards for each node with color-coded borders (green/yellow/red) based on CPU and latency thresholds, updating every 3 seconds
- `MetricsChart` — live Recharts line chart showing CPU% and latency per node with a dropdown to switch nodes, refreshing every 5 seconds
- `AiNarrator` — streams cluster health analysis from Claude API, displayed in a terminal-style panel
- `AiAnalysisService` — Spring service that collects latest metrics, builds an SRE-style prompt, and calls the Anthropic Claude API
- `POST /api/ai/analyze` — new backend endpoint returning plain-English cluster health summary

**Key decisions:**
- Claude Haiku (`claude-haiku-4-5-20251001`) chosen for speed and cost efficiency
- Model, API URL, and API key all configurable via environment variables — no hardcoded values
- CORS configured properly for production-readiness, not just Vite proxy workaround
- Vite dev server with proxy configured for local development

**Running the full stack:**
```bash
# Terminal 1 — backend + database
docker compose up --build

# Terminal 2 — frontend
cd frontend
npm run dev
```

Open `http://localhost:5173` to see the live dashboard.

**Environment variables required:**
| Variable | Description |
|----------|-------------|
| `ANTHROPIC_API_KEY` | Your Anthropic API key from console.anthropic.com |
| `ANTHROPIC_MODEL` | Model to use (default: `claude-haiku-4-5-20251001`) |
| `ANTHROPIC_API_URL` | API endpoint (default: `https://api.anthropic.com/v1/messages`) |

---

### ✅ Iteration 4 — Testing + Kubernetes + CI/CD
Production-grade testing, container orchestration, and automated pipeline.

**What was built:**
- `MetricsServiceTest` — 8 unit tests covering all service methods using JUnit 5 and Mockito
- `k8s/deployment.yaml` — Kubernetes Deployment with 2 replicas and self-healing
- `k8s/service.yaml` — Kubernetes Service exposing the backend via NodePort
- `k8s/configmap.yaml` — Non-secret config (database URL, model name) managed by Kubernetes
- `k8s/secret.yaml` — Encrypted storage for the Anthropic API key
- `.github/workflows/ci.yml` — GitHub Actions pipeline that runs tests and builds Docker image on every push

**Key decisions:**
- `k8s/secret.yaml` added to `.gitignore` — API keys never committed to git
- `imagePullPolicy: Never` in deployment — uses locally built image inside minikube
- `BackendApplicationTests` decoupled from database — unit tests run without Docker
- Surefire plugin configured with `-XX:+EnableDynamicAgentLoading` to suppress Mockito warnings

**Running on Kubernetes (local):**
```bash
# Start minikube
minikube start
eval $(minikube docker-env)

# Build image into minikube
docker build -t clusterpulse-backend:latest ./backend

# Deploy
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# Access
minikube service clusterpulse-backend --url
```

**Self-healing demo:**
```bash
# Delete a pod — Kubernetes immediately replaces it
kubectl delete pod <pod-name>
kubectl get pods
```

**CI/CD pipeline triggers on every push to main:**
- Runs all unit tests
- Fails build if any test fails
- Builds Docker image

---

### ✅ Iteration 5 — Alerting + Grafana + WebSocket
Production observability with real-time push, email alerting, and professional dashboards.

**Phase A — Email Alerting:**
- `AlertService` — evaluates metrics every 5 seconds, fires email alerts when thresholds breached
- Per-node cooldown (configurable) prevents alert spam
- Gmail SMTP integration via Spring JavaMailSender
- Alerts fire on CPU > 85% or latency > 300ms

**Phase B — Grafana Dashboards:**
- Grafana added to `docker-compose.yml` as a container
- Auto-provisioned datasource connecting directly to TimescaleDB
- 3 dashboards auto-loaded on startup: CPU per node, latency per node, memory per node
- 5-second auto-refresh, color-coded thresholds (green/yellow/red)
- No backend API involved — Grafana queries TimescaleDB directly via SQL

**Phase C — WebSocket Live Push:**
- `WebSocketConfig` — configures STOMP message broker over native WebSocket
- `MetricsBroadcaster` — pushes latest metrics to `/topic/metrics` every second
- React `useMetrics` hook replaced polling with STOMP WebSocket subscription
- Verified via Chrome DevTools Network → Socket tab showing status 101 (WebSocket upgrade)
- Dashboard now shows live `● Live` / `● Disconnected` connection indicator

**Key decisions:**
- SockJS skipped in favor of native WebSocket (`brokerURL`) to avoid Vite compatibility issues
- `global: 'globalThis'` polyfill added to `vite.config.js` for browser compatibility
- Grafana datasource UID hardcoded in dashboard JSON to match provisioned datasource
- Alert cooldown configurable via `application.properties` — set to 100 minutes during development

**Running Grafana:**

Open `http://localhost:3000` — login with `admin/admin`. Dashboard loads automatically under ClusterPulse folder.

**Alert thresholds (configurable in `application.properties`):**
| Property | Default | Description |
|----------|---------|-------------|
| `alert.cpu.threshold` | 85.0 | CPU % to trigger alert |
| `alert.latency.threshold` | 300.0 | Latency ms to trigger alert |
| `alert.cooldown.minutes` | 100 | Minutes between repeat alerts per node |

**Environment variables added:**
| Variable | Description |
|----------|-------------|
| `MAIL_USERNAME` | Gmail address for sending alerts |
| `MAIL_PASSWORD` | Gmail App Password (16-character, not your regular password) |

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Java 17, Spring Boot 4, Spring Web, Spring Scheduling |
| Persistence | TimescaleDB, Spring Data JPA, Hibernate |
| Containerization | Docker, Docker Compose |
| Frontend | React, Recharts, Vite, Axios, STOMP.js |
| AI Integration | Claude Haiku via Anthropic API |
| Orchestration | Kubernetes (minikube), kubectl |
| CI/CD | GitHub Actions |
| Observability | Grafana, JavaMailSender (Gmail SMTP) |
| Real-time | WebSocket (STOMP), Spring Message Broker |

---

## Project Structure
```
clusterpulse/
├── backend/
│   ├── src/main/java/com/clusterpulse/backend/
│   │   ├── config/         (WebConfig, DataInitializer, WebSocketConfig)
│   │   ├── controller/     (MetricsController)
│   │   ├── model/          (Node, NodeMetrics)
│   │   ├── repository/     (NodeRepository, NodeMetricsRepository)
│   │   ├── service/        (MetricsService, AiAnalysisService, AlertService, MetricsBroadcaster)
│   │   └── simulator/      (NodeMetricSimulator)
│   └── src/test/java/com/clusterpulse/backend/
│       └── service/        (MetricsServiceTest)
├── frontend/
│   └── src/
│       ├── components/     (NodeGrid, MetricsChart, AiNarrator)
│       ├── hooks/          (useMetrics)
│       └── App.jsx
├── grafana/
│   └── provisioning/
│       ├── datasources/    (timescaledb.yaml)
│       └── dashboards/     (dashboards.yaml, cluster-health.json)
├── k8s/
│   ├── configmap.yaml
│   ├── deployment.yaml
│   ├── secret.yaml         (gitignored - contains API key)
│   └── service.yaml
├── .github/
│   └── workflows/
│       └── ci.yml
├── docker-compose.yml
├── .env.example
├── CONTRIBUTING.md
├── DEPLOYMENT.md
└── README.md
```

---

*Iteration 5 Phase D: AWS ECS cloud deployment — in progress, see [DEPLOYMENT.md](DEPLOYMENT.md).*