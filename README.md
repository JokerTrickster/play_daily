# Play Daily

Daily Memo 앱 - 일상을 기록하고 공유하는 플랫폼

## 📚 개발 가이드

- **[기여 가이드 (CONTRIBUTING.md)](./CONTRIBUTING.md)** - ⚠️ 필독: 모든 기능 개발 시 E2E 테스트 필수
- **[CI/CD 파이프라인](./.github/workflows/README.md)** - 자동화된 테스트 및 배포
- **[E2E 테스트 가이드](./backend/src/tests/e2e/README.md)** - 백엔드 API 테스트 작성법

## ⚠️ 중요: 개발 시 필수 규칙

**모든 새로운 기능 개발 시 E2E 테스트 코드를 반드시 작성해야 합니다.**

```
새 기능 추가 = 코드 구현 + E2E 테스트 + 문서 업데이트
```

자세한 내용은 [CONTRIBUTING.md](./CONTRIBUTING.md)를 참고하세요.

## 🚀 Quick Start

### Backend

```bash
cd backend/src

# 테스트 실행
./scripts/run_tests.sh all

# 개발 서버 실행
go run main.go
```

### Frontend (Android)

```bash
cd frontend

# 빌드
./gradlew assembleDebug

# 설치 및 실행
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.dailymemo/.presentation.MainActivity
```
