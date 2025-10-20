# Backend Deployment Guide

## GitHub Actions CI/CD

이 프로젝트는 GitHub Actions를 사용하여 자동 배포됩니다.

### 필수 GitHub Secrets 설정

Repository Settings > Secrets and variables > Actions에서 다음 secrets를 설정해야 합니다:

#### Database
- `DB_USER`: MySQL 사용자명 (예: root)
- `DB_PASSWORD`: MySQL 비밀번호

#### JWT
- `JWT_SECRET`: JWT 서명용 비밀 키

#### AWS S3
- `AWS_REGION`: AWS 리전 (예: ap-northeast-2)
- `AWS_ACCESS_KEY_ID`: AWS Access Key ID
- `AWS_SECRET_ACCESS_KEY`: AWS Secret Access Key
- `S3_BUCKET_NAME`: S3 버킷 이름 (예: daily-memo-dev)

### Self-hosted Runner 설정

배포 서버에서 GitHub Actions self-hosted runner를 설정해야 합니다.

1. GitHub Repository > Settings > Actions > Runners
2. "New self-hosted runner" 클릭
3. 서버에서 제공된 명령어 실행

### 배포 프로세스

1. `main` 브랜치에 `backend/` 디렉토리 변경사항 push
2. GitHub Actions 자동 트리거
3. Self-hosted runner에서 실행:
   - 코드 체크아웃
   - 기존 컨테이너 중지 및 제거
   - Docker 이미지 빌드
   - 새 컨테이너 실행 (포트 7001)
   - Health check 수행

### 로컬 Docker 빌드 및 실행

```bash
# 이미지 빌드
cd backend
docker build -t play-daily-backend:latest .

# 컨테이너 실행
docker run -d \
  --name play-daily-backend \
  --network host \
  --restart unless-stopped \
  --env-file .env.production \
  -v $(pwd)/logs:/root/logs \
  -v $(pwd)/uploads:/root/uploads \
  play-daily-backend:latest

# 로그 확인
docker logs -f play-daily-backend

# 컨테이너 중지
docker stop play-daily-backend
docker rm play-daily-backend
```

### 환경 설정

배포 시 다음 설정이 자동으로 적용됩니다:
- DB_HOST: localhost (같은 서버의 MySQL 사용)
- PORT: 7001
- ENV: production
- ALLOWED_ORIGINS: * (모든 도메인 허용)

### Health Check

배포 완료 후 health check 엔드포인트:
```bash
curl http://localhost:7001/health
```

### 트러블슈팅

#### 컨테이너가 시작되지 않는 경우
```bash
docker logs play-daily-backend
```

#### 포트 충돌
```bash
# 7001 포트 사용 확인
lsof -i :7001

# 기존 프로세스 종료
kill -9 <PID>
```

#### 데이터베이스 연결 실패
- DB_HOST가 localhost로 설정되어 있는지 확인
- MySQL이 실행 중인지 확인: `systemctl status mysql`
- MySQL 포트 3306이 열려있는지 확인

#### 이미지 재빌드
```bash
# 캐시 없이 재빌드
docker build --no-cache -t play-daily-backend:latest .
```
