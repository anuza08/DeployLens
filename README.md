# DeployLens

> Real-time Jenkins CI/CD pipeline monitoring dashboard — built with Spring Boot, React, Prometheus, and Grafana.

---

## What is DeployLens?

DeployLens gives you a single pane of glass to monitor all your Jenkins pipelines. It automatically collects build data every 30 seconds, surfaces failures instantly, and pushes metrics to Grafana for historical analysis and alerting.

No more hunting through Jenkins logs to find what broke. DeployLens shows you which pipelines are healthy, which are failing, and how your success rate is trending — all in one place.

---

## Features

<img width="1440" height="900" alt="Screenshot 2026-04-13 at 3 59 33 PM" src="https://github.com/user-attachments/assets/57c241d3-48d4-47bf-8be3-2ec11d1af05a" />
<img width="1440" height="900" alt="Screenshot 2026-04-13 at 3 59 17 PM" src="https://github.com/user-attachments/assets/266ec224-42cd-4678-aafd-eb1046e848be" />
- **Live Dashboard** — React UI that auto-refreshes every 30 seconds
- **Pipeline Health Cards** — per-job success rate, last build status, avg duration, build history
- **Failure Tracker** — chronological list of recent failures across all pipelines
- **Analytics Charts** — bar chart of build results per pipeline + radial success rate gauge
- **Prometheus Metrics** — exposes `deploylens_jobs_total`, `deploylens_jobs_failing`, `deploylens_success_rate`
- **Grafana Dashboard** — auto-provisioned time-series graphs and stat panels
- **Auto Data Collection** — Spring Boot scheduler polls Jenkins API every 30s
- **Mock Data Fallback** — works out of the box even without a live Jenkins instance

---

## Architecture

```
Browser (React :3000)
       │
       ├──► Spring Boot API (:8080)  ──polls──► Jenkins (:8090)
       │         │
       │         └── /actuator/prometheus
       │                    │
       └──► Grafana (:3001) ◄── Prometheus (:9090) ──scrapes──┘
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3, Micrometer |
| Frontend | React 18, Recharts, Vite |
| Metrics | Prometheus, Grafana 10 |
| CI/CD | Jenkins LTS |
| Infrastructure | Docker, Docker Compose, Nginx |

---

## Quick Start

### Prerequisites

- Docker Desktop running
- Git

### Run with Docker Compose

```bash
# 1. Clone the repo
git clone <your-repo-url>
cd DeployLens

# 2. Set up environment
cp .env.example .env
# Edit .env with your Jenkins credentials (optional — mock data works without it)

# 3. Start everything
docker-compose up --build
```

Wait ~2 minutes for all services to build and start.

| Service | URL | Credentials |
|---|---|---|
| React Dashboard | http://localhost:3000 | — |
| Spring Boot API | http://localhost:8081 | — |
| Grafana | http://localhost:3001 | admin / deploylens |
| Prometheus | http://localhost:9090 | — |
| Jenkins | http://localhost:8090 | setup on first run |

---

### Run Locally (without Docker)

**Prerequisites:** Java 17, Maven, Node.js 18+

```bash
# Terminal 1 — Backend
cd backend
mvn spring-boot:run

# Terminal 2 — Frontend
cd frontend
npm install
npm start
```

---

## Connecting to Jenkins

### 1. Get your Jenkins API Token

1. Login to Jenkins → click your username → **Configure**
2. **API Token** → **Add new Token** → Generate
3. Copy the token

### 2. Set credentials

```bash
# .env
JENKINS_BASE_URL=http://localhost:8090
JENKINS_USERNAME=admin
JENKINS_API_TOKEN=your-token-here
```

```bash
docker-compose restart deploylens-backend
```

### 3. Verify connection

```bash
curl http://localhost:8081/api/v1/pipelines/health
```

---

## API Reference

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/pipelines/health` | Overall health summary |
| GET | `/api/v1/pipelines/metrics` | Full metrics snapshot |
| GET | `/api/v1/pipelines/jobs` | All pipeline jobs |
| GET | `/api/v1/pipelines/jobs/{name}/builds` | Builds for a specific job |
| GET | `/api/v1/pipelines/failures` | Recent failures across all jobs |
| POST | `/api/v1/pipelines/refresh` | Trigger immediate data collection |

### Example response — `/api/v1/pipelines/health`

```json
{
  "totalJobs": 4,
  "healthyJobs": 3,
  "failingJobs": 1,
  "unstableJobs": 0,
  "overallSuccessRate": 68.3,
  "collectedAt": "2026-04-13T10:21:59Z"
}
```

---

## Project Structure

```
DeployLens/
├── backend/                        # Spring Boot application
│   └── src/main/java/com/deploylens/
│       ├── controller/             # REST API endpoints
│       ├── service/                # Jenkins polling + metrics aggregation
│       ├── scheduler/              # @Scheduled data collection every 30s
│       ├── model/                  # PipelineJob, BuildInfo, PipelineMetrics
│       └── config/                 # CORS, Jenkins RestTemplate, Actuator
├── frontend/                       # React + Vite application
│   └── src/
│       ├── components/             # Dashboard, PipelineCard, Charts, FailureTracker
│       └── services/api.js         # Axios API client
├── grafana/
│   ├── dashboards/                 # Auto-provisioned Grafana dashboard JSON
│   └── provisioning/              # Datasource + dashboard provisioning config
├── prometheus/
│   └── prometheus.yml             # Scrape config for Spring Boot metrics
├── docker-compose.yml
├── .env.example                    # Environment variable template
└── .gitignore
```

---

## Grafana Dashboard

The Jenkins Pipeline Health dashboard is auto-provisioned on first startup. It includes:

- **Total / Healthy / Failing jobs** — stat panels with color thresholds
- **Overall Success Rate** — gauge panel (green > 80%, yellow > 50%, red below)
- **Success Rate Over Time** — time-series line chart
- **Healthy vs Failing Over Time** — comparative time-series chart

Access at **http://localhost:3001** → Dashboards → DeployLens → Jenkins Pipeline Health

---

## Testing the Application

### Trigger builds via Jenkins API

```bash
# Single build
curl -X POST http://localhost:8090/job/<job-name>/build \
  --user admin:<api-token>

# Trigger 5 builds to populate charts
for i in {1..5}; do
  curl -X POST http://localhost:8090/job/<job-name>/build \
    --user admin:<api-token>
  sleep 2
done
```

### Force immediate metrics refresh

```bash
curl -X POST http://localhost:8081/api/v1/pipelines/refresh
```

### Check Prometheus metrics

```bash
curl http://localhost:8081/actuator/prometheus | grep deploylens
```

---

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `JENKINS_BASE_URL` | `http://jenkins:8090` | Jenkins server URL |
| `JENKINS_USERNAME` | `admin` | Jenkins username |
| `JENKINS_API_TOKEN` | — | Jenkins API token |
| `JENKINS_POLL_INTERVAL_MS` | `30000` | How often to poll Jenkins (ms) |
| `GF_SECURITY_ADMIN_PASSWORD` | `deploylens` | Grafana admin password |

---

## Stopping the Application

```bash
# Stop all containers
docker-compose down

# Stop and remove all data (volumes)
docker-compose down -v
```

---

## License

MIT
