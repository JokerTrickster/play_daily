# 백엔드 배포 가이드

## 현재 상황
- 로컬에서 Comment 기능 개발 완료
- AWS 서버 (13.203.37.93)에 배포 필요

## 배포 방법

### 1. 백엔드 빌드 (로컬)
```bash
cd /Users/luxrobo/project/play_daily/backend/src
go build -o app .
```

### 2. AWS 서버 접속
```bash
ssh user@13.203.37.93
```

### 3. 백엔드 서비스 중지
```bash
# 현재 실행 중인 백엔드 프로세스 확인
ps aux | grep app

# 프로세스 종료 (PID는 위 명령어로 확인)
kill <PID>

# 또는 systemd 사용 시
sudo systemctl stop backend
```

### 4. 새 바이너리 업로드
로컬에서 실행:
```bash
cd /Users/luxrobo/project/play_daily/backend/src
scp app user@13.203.37.93:/path/to/backend/
```

### 5. 백엔드 서비스 재시작
AWS 서버에서:
```bash
cd /path/to/backend/
./app

# 또는 systemd 사용 시
sudo systemctl start backend
```

### 6. 확인
```bash
# Health check
curl http://13.203.37.93:7001/health

# 메모 목록 (comments 필드 포함 여부 확인)
curl http://13.203.37.93:7001/v0.1/memo | python3 -m json.tool

# 메모 상세 (comments 포함 여부 확인)
curl http://13.203.37.93:7001/v0.1/memo/15 | python3 -m json.tool
```

## 예상 결과

배포 후 메모 상세 API 응답에 comments 필드가 포함되어야 합니다:
```json
{
  "id": 15,
  "title": "등촌",
  "content": "...",
  "comments": [
    {
      "id": 1,
      "memo_id": 15,
      "user_id": 1,
      "user_name": "jhj485",
      "content": "맛있어요",
      "rating": 5,
      "created_at": "2025-10-22T10:00:00Z",
      "updated_at": "2025-10-22T10:00:00Z"
    }
  ],
  ...
}
```

## 트러블슈팅

### 문제: "memo not found" 에러
- 원인: 백엔드가 아직 업데이트되지 않음
- 해결: 위 배포 단계 실행

### 문제: Comments 테이블 없음
- 원인: init.sql 실행 안됨
- 해결:
  ```bash
  mysql -h localhost -u root -p daily_dev < /path/to/init.sql
  ```

### 문제: 포트 이미 사용 중
- 원인: 이전 백엔드 프로세스가 종료되지 않음
- 해결:
  ```bash
  lsof -ti:7001 | xargs kill -9
  ```
