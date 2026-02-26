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

## Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Java 17, Spring Boot 4, Spring Web, Spring Scheduling |
| Persistence | TimescaleDB, Spring Data JPA, Hibernate |
| Containerization | Docker, Docker Compose |
| Frontend | React, Recharts, Vite, Axios |
| AI Integration | Claude Haiku via Anthropic API |
| Orchestration | Kubernetes (minikube), kubectl |
| CI/CD | GitHub Actions |

---

## Project Structure
```
clusterpulse/
├── backend/
│   ├── src/main/java/com/clusterpulse/backend/
│   │   ├── config/         (WebConfig, DataInitializer)
│   │   ├── controller/     (MetricsController)
│   │   ├── model/          (Node, NodeMetrics)
│   │   ├── repository/     (NodeRepository, NodeMetricsRepository)
│   │   ├── service/        (MetricsService, AiAnalysisService)
│   │   └── simulator/      (NodeMetricSimulator)
│   └── src/test/java/com/clusterpulse/backend/
│       └── service/        (MetricsServiceTest)
├── frontend/
│   └── src/
│       ├── components/     (NodeGrid, MetricsChart, AiNarrator)
│       ├── hooks/          (useMetrics)
│       └── App.jsx
├── k8s/
│   ├── configmap.yaml
│   ├── deployment.yaml
│   ├── secret.yaml         (gitignored - contains API key)
│   └── service.yaml
├── .github/
│   └── workflows/
│       └── ci.yml
├── docker-compose.yml
└── README.md
```

---

*Coming next — Iteration 5: Alerting, Grafana dashboards, and cloud deployment.*