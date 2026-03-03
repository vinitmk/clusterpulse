# Contributing & Local Setup

## Prerequisites

| Tool | Version | Install |
|------|---------|---------|
| Java | 17+ | `brew install openjdk@17` |
| Maven | 3.9+ | `brew install maven` |
| Docker | Latest | https://docker.com |
| Node.js | 18+ | `brew install node` |
| minikube | Latest | `brew install minikube` (optional, for K8s) |
| kubectl | Latest | `brew install kubernetes-cli` (optional) |

---

## Quick Start
```bash
# 1. Clone
git clone https://github.com/vinitmk/clusterpulse.git
cd clusterpulse

# 2. Set up environment
cp .env.example .env
# Edit .env with your Anthropic API key and Gmail app password

# 3. Start backend + database
docker compose up --build

# 4. Start frontend (new terminal)
cd frontend
npm install
npm run dev
```

Open:
- **Dashboard** → http://localhost:5173
- **Backend API** → http://localhost:8080/api/metrics
- **Grafana** → http://localhost:3000 (admin/admin)

---

## Project Structure
```
clusterpulse/
├── backend/                          # Spring Boot 4, Java 17
│   ├── src/main/java/com/clusterpulse/backend/
│   │   ├── config/                   # WebConfig, DataInitializer, WebSocketConfig
│   │   ├── controller/               # MetricsController, AiController
│   │   ├── model/                    # Node, NodeMetrics (JPA entities)
│   │   ├── repository/               # Spring Data JPA repositories
│   │   ├── service/                  # MetricsService, AiAnalysisService,
│   │   │                             # AlertService, MetricsBroadcaster
│   │   └── simulator/                # NodeMetricSimulator
│   └── src/test/                     # JUnit 5 + Mockito unit tests
├── frontend/                         # React + Vite
│   └── src/
│       ├── components/               # NodeGrid, MetricsChart, AiNarrator
│       ├── hooks/                    # useMetrics (WebSocket)
│       └── App.jsx
├── grafana/
│   └── provisioning/                 # Auto-provisioned datasource + dashboards
├── k8s/                              # Kubernetes manifests
├── ecs/                              # AWS ECS task definitions (coming)
├── .github/workflows/                # GitHub Actions CI/CD
├── docker-compose.yml
├── .env.example                      # Copy to .env and fill in values
├── DEPLOYMENT.md                     # Full deployment guide
└── README.md
```

---

## Running Tests
```bash
cd backend
./mvnw test
```

All 8 unit tests should pass. Tests use Mockito mocks and do not require a running database.

---

## Development Workflow

### Backend changes
```bash
# Option A — rebuild Docker image
docker compose up --build

# Option B — run locally (requires local PostgreSQL or TimescaleDB)
cd backend
./mvnw spring-boot:run
```

### Frontend changes

Vite hot-reloads automatically — just save the file and the browser updates instantly.

### Adding a new metric endpoint

1. Add method to `MetricsService`
2. Add unit test in `MetricsServiceTest`
3. Expose via `MetricsController`
4. Run `./mvnw test` to verify

---

## Environment Variables Reference

| Variable | Required | Description |
|----------|----------|-------------|
| `ANTHROPIC_API_KEY` | Yes | Claude API key from console.anthropic.com |
| `ANTHROPIC_MODEL` | No | Default: `claude-haiku-4-5-20251001` |
| `MAIL_USERNAME` | No | Gmail address for alerts |
| `MAIL_PASSWORD` | No | Gmail App Password (16 chars) |
| `POSTGRES_DB` | No | Default: `clusterpulse` |
| `POSTGRES_USER` | No | Default: `clusterpulse` |
| `POSTGRES_PASSWORD` | No | Default: `clusterpulse` |

---

## Alert Thresholds

Configurable in `backend/src/main/resources/application.properties`:
```properties
alert.cpu.threshold=85.0          # CPU % to trigger alert
alert.latency.threshold=300.0     # Latency ms to trigger alert
alert.cooldown.minutes=100        # Minutes between repeat alerts per node
alert.recipient=${MAIL_USERNAME}  # Email to send alerts to
```

---

## Grafana

Dashboards are auto-provisioned — no manual setup needed.

If panels show "No data":
1. Go to Connections → Data sources → TimescaleDB
2. Set Database field to `clusterpulse`
3. Click Save & test
4. Refresh the dashboard

---

## Kubernetes (local)

See [DEPLOYMENT.md](DEPLOYMENT.md) for full minikube setup instructions.

---

## CI/CD

GitHub Actions runs on every push to `main`:
- Compiles the backend
- Runs all unit tests
- Builds Docker image
- Fails the build if any test fails

Check the Actions tab on GitHub to see the pipeline status.