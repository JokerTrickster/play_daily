# Daily Memo App - Frontend

일상 메모 앱의 Android 프론트엔드 프로젝트입니다.

## 프로젝트 구조

```
frontend/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/dailymemo/
│   │       │   ├── presentation/    # UI Layer (Compose, ViewModels)
│   │       │   │   ├── theme/       # Material 3 테마
│   │       │   │   ├── navigation/  # 네비게이션
│   │       │   │   ├── auth/        # 인증 화면
│   │       │   │   ├── map/         # 지도 화면
│   │       │   │   ├── list/        # 리스트 화면
│   │       │   │   ├── timeline/    # 타임라인 화면
│   │       │   │   ├── memory/      # 메모 화면
│   │       │   │   └── collaboration/ # 협업 화면
│   │       │   ├── domain/          # Business Logic Layer
│   │       │   │   ├── models/      # 도메인 엔티티
│   │       │   │   ├── repositories/ # 리포지토리 인터페이스
│   │       │   │   └── usecases/    # 비즈니스 로직
│   │       │   └── data/            # Data Layer
│   │       │       ├── repositories/ # 리포지토리 구현
│   │       │       ├── datasources/  # 데이터 소스
│   │       │       │   ├── local/   # 로컬 저장소
│   │       │       │   └── remote/  # API 클라이언트 (추후)
│   │       │       └── models/      # DTOs
│   │       ├── res/
│   │       │   ├── values/          # 기본 리소스
│   │       │   └── values-ko/       # 한국어 리소스
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## 기술 스택

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: Clean Architecture + MVVM
- **DI**: Hilt
- **Navigation**: Jetpack Navigation Compose
- **Async**: Kotlin Coroutines + Flow
- **Local Storage**: DataStore
- **Maps**: Kakao Maps SDK
- **Location**: Google Play Services Location
- **Image**: Coil

## 개발 환경 설정

### 필수 요구사항

- Android Studio Hedgehog 이상
- JDK 17
- Kotlin 1.9.20
- Gradle 8.0+
- Android SDK 34

### Kakao Maps API 키 설정

1. [Kakao Developers](https://developers.kakao.com/)에서 앱 생성 및 API 키 발급
2. `local.properties` 파일 생성 (git에 커밋되지 않음):

```properties
sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
kakao.api.key=YOUR_KAKAO_NATIVE_APP_KEY
```

3. `AndroidManifest.xml`에서 API 키 참조됨

### 빌드 및 실행

```bash
cd frontend
./gradlew assembleDebug
./gradlew installDebug
```

## 주요 기능

### Phase 1: 기초 및 아키텍처 (완료)
- ✅ Clean Architecture 프로젝트 구조
- ✅ Jetpack Compose + Material 3 설정
- ✅ Hilt 의존성 주입
- ✅ Navigation 구조
- ✅ 한국어 리소스

### Phase 2: 핵심 메모 기능 (예정)
- [ ] 인증 시스템 (회원가입/로그인)
- [ ] Kakao Maps 통합
- [ ] 메모 생성 (위치, 사진, 평점, 메모)
- [ ] 로컬 데이터 저장

### Phase 3: 보기 및 탐색 (예정)
- [ ] 지도 뷰 (핀, 미리보기)
- [ ] 리스트 뷰 (정렬)
- [ ] 타임라인 뷰
- [ ] 검색 및 필터

### Phase 4: 협업 (예정)
- [ ] 사용자 검색
- [ ] 초대 시스템
- [ ] 공유 메모 공간

### Phase 5: 위치 서비스 (예정)
- [ ] 위치 권한 처리
- [ ] 백그라운드 위치 추적
- [ ] 현재 위치 표시

### Phase 6: 완성도 향상 (예정)
- [ ] 에러 처리
- [ ] 로딩 상태
- [ ] 애니메이션
- [ ] 성능 최적화

## 코드 스타일

- Kotlin 공식 코드 컨벤션 준수
- Clean Architecture 레이어 분리 엄격히 준수
- Material 3 디자인 가이드라인 따름

## 라이선스

Private Project
