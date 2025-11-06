## 📋 Changes

<!-- 무엇을 변경했는지 간단히 설명해주세요 -->

## 🎯 Purpose

<!-- 왜 이 변경이 필요한지 설명해주세요 -->
<!-- 관련 이슈가 있다면 링크해주세요: Closes #123 -->

## 🧪 Testing

### E2E 테스트 체크리스트 (필수)

- [ ] **E2E 테스트 작성 완료** (새 기능/API 변경 시 필수)
- [ ] **로컬에서 모든 테스트 통과 확인**
  ```bash
  cd backend/src
  ./scripts/run_tests.sh all
  ```
- [ ] **새로운 테스트 시나리오 추가** (해당 시)
  - [ ] 성공 케이스
  - [ ] 실패 케이스 (400, 401, 403, 404 등)
  - [ ] 엣지 케이스

### 테스트 실행 결과

<details>
<summary>테스트 결과 (클릭하여 펼치기)</summary>

```
# 여기에 테스트 실행 결과를 붙여넣으세요
# 예:
# === RUN   TestNewFeature
# === RUN   TestNewFeature/1._성공_케이스
# === RUN   TestNewFeature/2._실패_케이스
# --- PASS: TestNewFeature (0.25s)
# PASS
```

</details>

## 📝 Additional Notes

### 데이터베이스 변경사항

- [ ] **스키마 변경 없음**
- [ ] **스키마 변경 있음** (아래 내용 작성)
  - 마이그레이션 파일: `backend/src/common/db/mysql/migration_xxx.sql`
  - CI/CD 스키마 업데이트 스크립트 추가 완료
  - 변경 내용:
    ```sql
    -- 여기에 스키마 변경 SQL 작성
    ```

### API 문서

- [ ] **API 변경 없음**
- [ ] **API 변경 있음**
  - [ ] Swagger 문서 업데이트 (`swag init` 실행)
  - [ ] README 또는 API 문서 업데이트

### 체크리스트

- [ ] 코드 리뷰 준비 완료
- [ ] 커밋 메시지가 명확함
- [ ] 불필요한 코드/주석 제거
- [ ] 로그 출력 정리 (개발용 console.log 등 제거)

## 🚨 Breaking Changes

<!-- Breaking change가 있다면 설명해주세요 -->

- [ ] **Breaking change 없음**
- [ ] **Breaking change 있음** (아래 내용 작성)
  - 영향 받는 부분:
  - 마이그레이션 가이드:

## 📸 Screenshots (선택)

<!-- UI 변경사항이 있다면 스크린샷을 추가해주세요 -->

## 🔗 Related Issues

<!-- 관련 이슈가 있다면 링크해주세요 -->

Closes #
Related to #

---

## ⚠️ 리뷰어 확인사항

- [ ] E2E 테스트 포함 확인
- [ ] 테스트 시나리오의 적절성
- [ ] 코드 품질 및 가독성
- [ ] 에러 처리 적절성
- [ ] Breaking change 검토

---

**📚 참고 문서**: [CONTRIBUTING.md](../CONTRIBUTING.md)
