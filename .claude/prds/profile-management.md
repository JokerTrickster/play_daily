---
name: profile-management
description: 사용자 프로필 정보 수정 기능 (이름, 비밀번호, 프로필 이미지)
status: backlog
created: 2025-10-23T02:50:28Z
---

# PRD: Profile Management

## Executive Summary

사용자가 자신의 프로필 정보를 수정할 수 있는 통합 관리 기능을 제공합니다. 단일 화면에서 이름, 비밀번호, 프로필 이미지를 변경할 수 있으며, 보안을 위해 모든 변경 사항은 현재 비밀번호 확인을 필수로 요구합니다. 기존 코드 아키텍처를 유지하면서 안전하고 직관적인 프로필 관리 경험을 제공하는 것이 목표입니다.

## Problem Statement

### 현재 상황
- 프로필 화면(`ProfileScreen.kt`)이 존재하지만 실제 프로필 수정 기능이 구현되어 있지 않음
- 사용자가 계정 정보를 변경할 수 있는 방법이 없음
- 프로필 이미지 표시 및 업로드 기능 부재

### 해결해야 할 문제
1. 사용자가 자신의 프로필 정보(이름, 비밀번호)를 변경할 수 없음
2. 프로필을 시각적으로 구분할 수 있는 이미지 기능이 없음
3. 보안을 고려한 프로필 수정 프로세스가 필요함

### 중요성
- **사용자 경험**: 개인화된 프로필 관리는 필수적인 사용자 기능
- **보안**: 비밀번호 변경 기능은 계정 보안의 핵심
- **차별화**: 프로필 이미지는 다중 사용자 환경에서 사용자 구분을 용이하게 함

## User Stories

### Primary Persona: 앱 사용자

#### User Story 1: 이름 변경
**As a** 사용자
**I want to** 내 프로필 이름을 변경하고 싶다
**So that** 더 선호하는 이름으로 표시할 수 있다

**Acceptance Criteria:**
- [ ] 현재 비밀번호를 입력하면 이름만 변경할 수 있다
- [ ] 이름 필드만 수정하고 저장하면 다른 정보는 변경되지 않는다
- [ ] 변경 즉시 앱 전체에서 새 이름이 반영된다
- [ ] 이름 변경 성공 시 확인 메시지가 표시된다

#### User Story 2: 비밀번호 변경
**As a** 사용자
**I want to** 비밀번호를 안전하게 변경하고 싶다
**So that** 계정 보안을 강화할 수 있다

**Acceptance Criteria:**
- [ ] 현재 비밀번호 입력이 필수다
- [ ] 새 비밀번호를 두 번 입력하여 일치 여부를 확인한다
- [ ] 두 새 비밀번호가 일치하지 않으면 에러 메시지를 표시한다
- [ ] 현재 비밀번호가 틀리면 에러 메시지를 표시한다
- [ ] 비밀번호 변경 후에도 로그인 세션이 유지된다
- [ ] 변경 성공 시 확인 메시지가 표시된다

#### User Story 3: 프로필 이미지 설정
**As a** 사용자
**I want to** 프로필 이미지를 설정하고 싶다
**So that** 내 프로필을 시각적으로 구분할 수 있다

**Acceptance Criteria:**
- [ ] 사진첩에서 이미지를 선택할 수 있다
- [ ] JPG, PNG 형식의 이미지를 지원한다
- [ ] 이미지 선택 즉시 미리보기가 표시된다
- [ ] 저장 버튼을 눌러야 실제 업로드 및 변경이 완료된다
- [ ] 프로필 이미지가 없을 때는 기본 이미지가 표시된다
- [ ] 업로드 성공 시 확인 메시지가 표시된다

#### User Story 4: 통합 프로필 수정
**As a** 사용자
**I want to** 한 화면에서 여러 정보를 동시에 수정하고 싶다
**So that** 편리하게 프로필을 관리할 수 있다

**Acceptance Criteria:**
- [ ] 단일 화면에서 이름, 비밀번호, 이미지를 모두 볼 수 있다
- [ ] 하나의 저장 버튼으로 모든 변경사항을 저장한다
- [ ] 현재 비밀번호는 항상 필수로 입력해야 한다
- [ ] 변경하지 않은 필드는 기존 값이 유지된다

## Requirements

### Functional Requirements

#### FR1: 프로필 정보 표시
- 현재 프로필 이미지 표시 (없을 시 기본 이미지)
- 현재 이름(nickname) 표시
- 계정 ID(account_id) 표시 (읽기 전용)

#### FR2: 이름 변경
- 이름 입력 필드
- 실시간 입력 검증 (공백 확인)
- 변경 전후 이름 비교

#### FR3: 비밀번호 변경
- 현재 비밀번호 입력 필드 (필수, 보안 입력)
- 새 비밀번호 입력 필드 (보안 입력)
- 새 비밀번호 확인 입력 필드 (보안 입력)
- 새 비밀번호 일치 여부 실시간 검증
- 비밀번호 표시/숨김 토글

#### FR4: 프로필 이미지 변경
- 이미지 선택 버튼 (사진첩 열기)
- 이미지 미리보기
- 지원 형식: JPG, PNG
- S3 업로드: `daily-dev` 버킷, `image/profile/` 경로
- UUID 기반 파일명 생성

#### FR5: 저장 및 검증
- 단일 저장 버튼
- 현재 비밀번호 검증 (백엔드)
- 변경사항만 서버로 전송
- 저장 성공/실패 피드백

#### FR6: 에러 처리
- 현재 비밀번호 불일치 에러
- 새 비밀번호 불일치 에러
- 네트워크 에러
- 이미지 업로드 실패 에러
- 무제한 재시도 허용

### Non-Functional Requirements

#### NFR1: Performance
- 이미지 업로드: 10초 이내 완료
- 프로필 정보 로드: 2초 이내
- UI 반응성: 즉각적인 입력 피드백

#### NFR2: Security
- 비밀번호 필드는 항상 마스킹 처리
- HTTPS를 통한 모든 통신
- 현재 비밀번호 검증은 서버에서만 수행
- 비밀번호는 암호화하여 전송

#### NFR3: Usability
- 기존 코드 아키텍처(Clean Architecture) 유지
- Material 3 디자인 시스템 준수
- 명확한 에러 메시지 제공
- 직관적인 UI/UX

#### NFR4: Compatibility
- 기존 users 테이블 구조 확장
- 기존 S3 업로드 로직 재사용
- 기존 API 엔드포인트 패턴 준수

## Technical Specifications

### Database Changes

**users 테이블 마이그레이션:**
```sql
ALTER TABLE users
ADD COLUMN profile_image_url VARCHAR(500) NULL COMMENT '프로필 이미지 URL' AFTER nickname,
ADD INDEX idx_profile_image_url (profile_image_url);
```

### API Endpoints

#### GET /v0.1/profile
**Description:** 현재 사용자의 프로필 정보 조회

**Response:**
```json
{
  "user_id": 1,
  "account_id": "testuser",
  "nickname": "테스터",
  "profile_image_url": "https://s3.../image/profile/uuid.jpg",
  "default_room_id": 1
}
```

#### PUT /v0.1/profile
**Description:** 프로필 정보 업데이트

**Request:**
```json
{
  "current_password": "현재비밀번호",
  "nickname": "새이름",  // optional
  "new_password": "새비밀번호",  // optional
  "profile_image_url": "https://s3.../image/profile/uuid.jpg"  // optional
}
```

**Response:**
```json
{
  "message": "프로필이 성공적으로 업데이트되었습니다",
  "user_id": 1,
  "account_id": "testuser",
  "nickname": "새이름",
  "profile_image_url": "https://s3.../image/profile/uuid.jpg"
}
```

**Error Responses:**
- `400`: 현재 비밀번호 불일치
- `400`: 필수 필드 누락
- `500`: 서버 오류

### Frontend Architecture

**New/Modified Files:**
```
frontend/app/src/main/java/com/dailymemo/
├── presentation/profile/
│   ├── ProfileScreen.kt (수정)
│   ├── ProfileViewModel.kt (수정)
│   └── ProfileUiState.kt (신규)
├── domain/usecases/profile/
│   ├── GetProfileUseCase.kt (신규)
│   └── UpdateProfileUseCase.kt (신규)
├── domain/models/
│   └── Profile.kt (신규)
└── data/repository/
    └── ProfileRepository.kt (신규)
```

### Backend Architecture

**New/Modified Files:**
```
backend/src/
├── features/profile/
│   ├── handler/
│   │   ├── index.go (신규)
│   │   ├── getProfileHandler.go (신규)
│   │   └── updateProfileHandler.go (신규)
│   ├── usecase/
│   │   ├── getProfileUseCase.go (신규)
│   │   └── updateProfileUseCase.go (신규)
│   ├── repository/
│   │   ├── getProfileRepository.go (신규)
│   │   └── updateProfileRepository.go (신규)
│   └── model/
│       ├── request/
│       │   └── updateProfile.go (신규)
│       └── response/
│           └── profile.go (신규)
└── common/db/mysql/
    ├── gormDB.go (수정 - User 모델에 ProfileImageURL 추가)
    └── migration_add_profile_image.sql (신규)
```

## Success Criteria

### Quantitative Metrics
- 프로필 업데이트 API 응답 시간: 평균 500ms 이하
- 이미지 업로드 성공률: 95% 이상
- 프로필 업데이트 성공률: 98% 이상

### Qualitative Metrics
- 사용자가 3번의 클릭 이내에 프로필 수정 완료 가능
- 에러 메시지가 명확하고 해결 방법 제시
- 기존 코드 패턴과 일관성 유지

### User Acceptance
- [ ] 사용자가 이름을 성공적으로 변경할 수 있다
- [ ] 사용자가 비밀번호를 안전하게 변경할 수 있다
- [ ] 사용자가 프로필 이미지를 설정할 수 있다
- [ ] 모든 에러 상황에서 적절한 피드백을 받는다

## Constraints & Assumptions

### Technical Constraints
- 기존 Clean Architecture 패턴 준수 필수
- 기존 S3 업로드 로직(`storage.S3.UploadFile`) 재사용
- 기존 API 엔드포인트 패턴(`/v0.1/`) 유지
- Material 3 디자인 시스템 사용

### Business Constraints
- 이메일 변경 기능은 제외
- 프로필 이미지 삭제 기능은 제외
- 비밀번호 정책(최소 길이, 특수문자 등) 없음

### Assumptions
- 사용자는 이미 로그인된 상태
- JWT 토큰을 통한 사용자 인증
- S3 버킷(`daily-dev`)은 이미 설정되어 있음
- 프로필 이미지 크기 제한 없음 (클라이언트 메모리 범위 내)

## Out of Scope

명시적으로 이 PRD에 포함되지 **않는** 항목들:

### Phase 1에서 제외
- ❌ 이메일 변경 기능
- ❌ 프로필 이미지 삭제 기능
- ❌ 이미지 크롭/편집 기능
- ❌ 프로필 공개/비공개 설정
- ❌ 2단계 인증 (2FA)
- ❌ 비밀번호 정책 강제
- ❌ 비밀번호 변경 이력 추적
- ❌ 계정 삭제 기능
- ❌ 다중 프로필 이미지
- ❌ 프로필 테마 설정

## Dependencies

### External Dependencies
- **S3 Storage**: 이미지 업로드를 위한 AWS S3 (`daily-dev` 버킷)
- **Image Picker**: Android 사진첩 접근 권한

### Internal Dependencies
- **인증 시스템**: JWT 토큰 기반 사용자 인증
- **기존 Storage 모듈**: `common/storage/s3.go`
- **기존 User 모델**: `common/db/mysql/gormDB.go`

### Team Dependencies
- **Backend 팀**: API 엔드포인트 개발
- **Frontend 팀**: UI/UX 구현
- **DevOps 팀**: S3 버킷 권한 확인

## Implementation Phases

### Phase 1: Backend Foundation (2-3일)
1. DB 마이그레이션 (profile_image_url 추가)
2. Profile 도메인 모델 생성
3. GET /v0.1/profile API 구현
4. PUT /v0.1/profile API 구현
5. 비밀번호 검증 로직
6. 단위 테스트 작성

### Phase 2: Frontend UI (2-3일)
1. ProfileUiState 및 모델 정의
2. ProfileViewModel 로직 구현
3. ProfileScreen UI 구현
   - 프로필 이미지 섹션
   - 이름 입력 필드
   - 비밀번호 변경 섹션
4. 이미지 선택 및 미리보기
5. 폼 검증 로직

### Phase 3: Integration (1-2일)
1. Repository 레이어 구현
2. UseCase 구현
3. API 연동
4. S3 이미지 업로드 통합
5. 에러 처리 및 피드백

### Phase 4: Testing & Polish (1일)
1. E2E 테스트
2. 에러 시나리오 테스트
3. UI/UX 개선
4. 성능 최적화

**총 예상 기간**: 6-9일

## Risks & Mitigation

### Risk 1: S3 업로드 실패
**Impact**: High
**Probability**: Medium
**Mitigation**:
- 재시도 로직 구현
- 명확한 에러 메시지 제공
- 로컬 이미지 캐싱으로 임시 저장

### Risk 2: 비밀번호 검증 보안
**Impact**: Critical
**Probability**: Low
**Mitigation**:
- 서버 측에서만 비밀번호 검증
- HTTPS 필수
- 비밀번호 암호화 전송

### Risk 3: 기존 코드와의 충돌
**Impact**: Medium
**Probability**: Low
**Mitigation**:
- 기존 패턴 철저히 분석 후 구현
- 코드 리뷰 프로세스 강화
- 단위 테스트로 회귀 방지

## Validation & Testing

### Test Cases

#### TC1: 이름만 변경
1. 현재 비밀번호 입력
2. 이름 필드만 수정
3. 저장 버튼 클릭
4. **Expected**: 이름만 변경, 다른 정보 유지

#### TC2: 비밀번호만 변경
1. 현재 비밀번호 입력
2. 새 비밀번호, 확인 입력 (일치)
3. 저장 버튼 클릭
4. **Expected**: 비밀번호 변경, 세션 유지

#### TC3: 프로필 이미지만 변경
1. 이미지 선택 버튼 클릭
2. 사진첩에서 이미지 선택
3. 현재 비밀번호 입력
4. 저장 버튼 클릭
5. **Expected**: 이미지 업로드 후 URL 저장

#### TC4: 모두 동시 변경
1. 이름, 비밀번호, 이미지 모두 수정
2. 현재 비밀번호 입력
3. 저장 버튼 클릭
4. **Expected**: 모든 변경사항 반영

#### TC5: 현재 비밀번호 오류
1. 잘못된 현재 비밀번호 입력
2. 저장 버튼 클릭
3. **Expected**: 에러 메시지, 변경 안됨

#### TC6: 새 비밀번호 불일치
1. 새 비밀번호와 확인이 다름
2. 저장 버튼 클릭
3. **Expected**: 에러 메시지, 저장 차단

## Open Questions

1. **프로필 이미지 기본값**: 어떤 기본 이미지를 사용할까요? (앱 내 리소스 or S3)
2. **이미지 최적화**: 업로드 전 클라이언트에서 리사이징/압축이 필요한가요?
3. **변경 이력**: 프로필 변경 이력을 로깅해야 하나요?
4. **알림**: 비밀번호 변경 시 이메일 알림이 필요한가요?
5. **동시성**: 여러 기기에서 동시 프로필 수정 시 처리 방법은?

## References

### Existing Code Patterns
- Memo 생성 시 이미지 업로드: `CreateMemoUseCase.kt`
- S3 업로드 로직: `backend/src/common/storage/s3.go`
- 인증 처리: `backend/src/features/auth/`
- Clean Architecture: `backend/src/features/memo/`

### Design System
- Material 3 Components
- 기존 ProfileScreen.kt 레이아웃 패턴

### External Documentation
- Android Image Picker: [ActivityResultContracts.PickVisualMedia](https://developer.android.com/training/data-storage/shared/photopicker)
- AWS S3 Upload: 기존 구현 참조
