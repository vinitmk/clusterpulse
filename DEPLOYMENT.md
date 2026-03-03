# Deployment Guide

## Local Development (Docker Compose)

The fastest way to run the full stack locally:
```bash
git clone https://github.com/vinitmk/clusterpulse.git
cd clusterpulse
cp .env.example .env   # fill in your values
docker compose up --build
```

| Service | URL |
|---------|-----|
| Backend API | http://localhost:8080 |
| Frontend | http://localhost:5173 |
| Grafana | http://localhost:3000 |
| TimescaleDB | localhost:5433 |

---

## Local Kubernetes (minikube)

Requires: `minikube`, `kubectl`, Docker
```bash
# Start cluster
minikube start
eval $(minikube docker-env)

# Build image into minikube
docker build -t clusterpulse-backend:latest ./backend

# Create secret (copy from .env first)
cp k8s/secret.yaml.example k8s/secret.yaml
# Edit k8s/secret.yaml with your actual API key

# Deploy
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# Verify
kubectl get pods

# Access (keep this terminal open)
minikube service clusterpulse-backend --url
```

### Useful kubectl commands
```bash
# Watch pods
kubectl get pods -w

# View logs
kubectl logs -l app=clusterpulse-backend

# Self-healing demo
kubectl delete pod <pod-name>
kubectl get pods   # replacement starts immediately

# Scale up
kubectl scale deployment clusterpulse-backend --replicas=3

# Tear down
kubectl delete -f k8s/
```

---

## Cloud Deployment — AWS ECS Fargate (Pending)

> Status: AWS account verification in progress. Steps documented and ready to execute.

### Prerequisites

- AWS account with billing verified
- AWS CLI installed: `brew install awscli`
- AWS CLI configured: `aws configure`

### Step 1 — Create ECR Repository
```bash
aws ecr create-repository \
  --repository-name clusterpulse-backend \
  --region us-east-1
```

Note the `repositoryUri` from the output — you'll need it in Step 3.

### Step 2 — Authenticate Docker to ECR
```bash
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin \
  <your-account-id>.dkr.ecr.us-east-1.amazonaws.com
```

### Step 3 — Build and Push Image
```bash
docker build -t clusterpulse-backend ./backend

docker tag clusterpulse-backend:latest \
  <your-account-id>.dkr.ecr.us-east-1.amazonaws.com/clusterpulse-backend:latest

docker push \
  <your-account-id>.dkr.ecr.us-east-1.amazonaws.com/clusterpulse-backend:latest
```

### Step 4 — Create ECS Cluster
```bash
aws ecs create-cluster \
  --cluster-name clusterpulse \
  --region us-east-1
```

### Step 5 — Create Task Definition

Create `ecs/task-definition.json`:
```json
{
  "family": "clusterpulse-backend",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "arn:aws:iam::<account-id>:role/ecsTaskExecutionRole",
  "containerDefinitions": [
    {
      "name": "backend",
      "image": "<account-id>.dkr.ecr.us-east-1.amazonaws.com/clusterpulse-backend:latest",
      "portMappings": [
        { "containerPort": 8080, "protocol": "tcp" }
      ],
      "environment": [
        { "name": "SPRING_DATASOURCE_URL", "value": "jdbc:postgresql://<your-db-host>:5432/clusterpulse" },
        { "name": "SPRING_DATASOURCE_USERNAME", "value": "clusterpulse" },
        { "name": "ANTHROPIC_MODEL", "value": "claude-haiku-4-5-20251001" }
      ],
      "secrets": [
        { "name": "ANTHROPIC_API_KEY", "valueFrom": "arn:aws:secretsmanager:us-east-1:<account-id>:secret:clusterpulse/anthropic-api-key" },
        { "name": "SPRING_DATASOURCE_PASSWORD", "valueFrom": "arn:aws:secretsmanager:us-east-1:<account-id>:secret:clusterpulse/db-password" }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/clusterpulse-backend",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```
```bash
aws ecs register-task-definition \
  --cli-input-json file://ecs/task-definition.json \
  --region us-east-1
```

### Step 6 — Create ECS Service
```bash
aws ecs create-service \
  --cluster clusterpulse \
  --service-name clusterpulse-backend \
  --task-definition clusterpulse-backend \
  --desired-count 1 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[<subnet-id>],securityGroups=[<sg-id>],assignPublicIp=ENABLED}" \
  --region us-east-1
```

### Step 7 — Verify
```bash
aws ecs describe-services \
  --cluster clusterpulse \
  --services clusterpulse-backend \
  --region us-east-1

aws ecs list-tasks --cluster clusterpulse --region us-east-1
aws ecs describe-tasks --cluster clusterpulse --tasks <task-arn> --region us-east-1
```

### Architecture on AWS
```
Internet
    ↓
Application Load Balancer (public)
    ↓
ECS Service (Fargate)
    └── Task (clusterpulse-backend container)
            ↓
        Secrets Manager (API keys)
        CloudWatch Logs (container logs)
        ECR (Docker image registry)
```

### Cost Estimate (Free Tier)

| Resource | Free Tier | Est. Cost After |
|----------|-----------|-----------------|
| ECS Fargate (0.5 vCPU, 1GB) | First 3 months free | ~$15/month |
| ECR storage | 500MB free | ~$0.10/GB |
| CloudWatch Logs | 5GB free | ~$0.50/GB |
| Data transfer | 1GB free | ~$0.09/GB |

---

## CI/CD Pipeline

GitHub Actions runs on every push to `main`. To extend CI to auto-deploy to ECS on merge to `main`, add to `.github/workflows/ci.yml`:
```yaml
- name: Deploy to ECS
  run: |
    aws ecs update-service \
      --cluster clusterpulse \
      --service clusterpulse-backend \
      --force-new-deployment
  env:
    AWS_ACCESS_KEY_ID: ${{ secrets.AWS_ACCESS_KEY_ID }}
    AWS_SECRET_ACCESS_KEY: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
    AWS_DEFAULT_REGION: us-east-1
```