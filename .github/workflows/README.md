# GitHub Actions CI/CD Pipeline

## Overview

This repository uses GitHub Actions for automated testing and deployment of the backend service.

## Workflow: Deploy Backend

**File**: `deploy-backend.yml`

### Pipeline Stages

1. **Test Stage** (runs first)
   - Sets up Go environment
   - Runs E2E tests on `daily_test` database
   - **Blocks deployment if tests fail**

2. **Deploy Stage** (runs only if tests pass)
   - Builds Docker image
   - Deploys to production server
   - Performs health checks

### Required GitHub Secrets

Configure these secrets in GitHub repository settings:

| Secret Name | Description | Example |
|------------|-------------|---------|
| `DB_USER` | Database username | `root` |
| `DB_PASSWORD` | Database password | `your_password` |
| `DB_HOST` | Database host (EC2 IP) | `13.203.37.93` |
| `JWT_SECRET` | JWT signing secret | `your_jwt_secret` |
| `AWS_REGION` | AWS region for S3 | `ap-south-1` |
| `AWS_ACCESS_KEY_ID` | AWS access key | `AKIAXXXXXXXX` |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key | `xxxxxxxx` |
| `S3_BUCKET_NAME` | S3 bucket name | `daily-memo-dev` |

### Setting Up Secrets

1. Go to GitHub repository → Settings → Secrets and variables → Actions
2. Click "New repository secret"
3. Add each secret with its name and value

### Test Database Setup

The CI/CD pipeline uses a separate test database (`daily_test`) to avoid affecting production data:

```bash
# Create test database (one-time setup on EC2)
docker run -i --rm mysql:8.0 mysql -h 13.203.37.93 -u root -pYOUR_PASSWORD \
  -e "CREATE DATABASE IF NOT EXISTS daily_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Apply schema
cd backend/src
sed 's/daily_dev/daily_test/g' common/db/mysql/init.sql | \
  docker run -i --rm mysql:8.0 mysql -h 13.203.37.93 -u root -pYOUR_PASSWORD
```

### Running Tests Locally

```bash
cd backend/src
./scripts/run_tests.sh all
```

### Workflow Triggers

- **Push to main branch** with changes in:
  - `backend/**`
  - `.github/workflows/deploy-backend.yml`
- **Manual trigger** via GitHub Actions UI

### Test Coverage

Currently testing:
- ✅ Authentication API (7 scenarios)
- ✅ Category API (4 scenarios)
- ⚠️ Memo, Comment, Room APIs (pending form-data fix)

### Deployment Process

1. Developer pushes to `main` branch
2. GitHub Actions triggers workflow
3. **Test stage**: E2E tests run on `daily_test` DB
4. If tests pass → **Deploy stage**: Production deployment
5. **Cleanup stage**: Remove old Docker images/containers
6. If tests fail → Deployment blocked, notification sent

### Docker Cleanup Strategy

To prevent disk space issues, the pipeline automatically cleans up:

- **Dangling images**: Untagged images removed
- **Old images**: Images older than 7 days (168h) not in use
- **Stopped containers**: Containers stopped for more than 24h
- **Unused volumes**: Volumes not attached to any container
- **Build cache**: Build cache older than 7 days

**Retention Policy**:
- Current image: `play-daily-backend:latest` (always kept)
- Recent images: < 7 days (kept as rollback buffer)
- Old images: > 7 days (automatically removed)

### Monitoring

- View workflow runs: GitHub → Actions tab
- Check logs for each stage
- Health check verifies deployment success

### Rollback

If deployment fails:
1. Check GitHub Actions logs
2. Revert commit if needed: `git revert HEAD`
3. Push to trigger new deployment

## Troubleshooting

**Tests failing in CI but passing locally?**
- Check GitHub Secrets are configured
- Verify test database (`daily_test`) exists
- Ensure EC2 firewall allows GitHub Actions runner IP

**Deployment blocked?**
- Check test logs in GitHub Actions
- Fix failing tests
- Push fix to trigger new workflow

**Health check failing?**
- Check backend logs: `docker logs play-daily-backend`
- Verify MySQL connection
- Check AWS credentials for S3
