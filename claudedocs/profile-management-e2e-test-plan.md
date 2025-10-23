# Profile Management E2E Test Plan

**Epic:** Profile Management (#29)
**Task:** #38 - End-to-End Testing
**Date:** 2025-10-23
**Status:** Integration Complete - Ready for Testing

## Test Environment

### Backend
- **URL:** http://13.203.37.93:7001
- **Status:** Running
- **Deployment Status:** ⚠️ Needs redeployment with latest profile endpoints
- **Required Changes:** Deploy commits 498d33a, 34ac7c9, 7cb04ea

### Frontend
- **Build Type:** Debug APK
- **Target Device:** Real Android device or Emulator
- **Required:** Android 8.0+ (API 26+)

### Prerequisites
- [ ] Backend profile endpoints deployed (`/v0.1/profile` GET and PUT)
- [ ] Test user account created in database
- [ ] Android device/emulator available
- [ ] Network connectivity to backend server

## Implementation Status

### ✅ Completed Components

#### Backend (Tasks #30-33)
- [x] Database migration - `profile_image_url` column added to users table
- [x] GET /v0.1/profile endpoint (GetProfileHandler)
- [x] PUT /v0.1/profile endpoint (UpdateProfileHandler)
- [x] Clean Architecture structure (Repository → UseCase → Handler)
- [x] Password verification (plain text)
- [x] Atomic updates with GORM transactions

#### Frontend (Tasks #34-37)
- [x] Profile domain model
- [x] ProfileUiState sealed class
- [x] UpdateProfileRequest and ProfileResponse DTOs
- [x] ProfileRepository and ProfileRepositoryImpl
- [x] GetProfileUseCase and UpdateProfileUseCase
- [x] ProfileViewModel with state management
- [x] ProfileEditScreen UI
- [x] Image picker integration (ActivityResultContracts)
- [x] Form validation logic
- [x] Loading/error/success feedback

#### Integration (Task #38)
- [x] Navigation routing (Screen.Profile.Edit)
- [x] ProfileEditScreen registered in NavGraph
- [x] ProfileScreen → ProfileEditScreen navigation
- [x] ProfileApiService DI registration
- [x] ProfileRepository DI registration
- [x] Bearer token auto-injection (OkHttpClient)

### ⚠️ Known Limitations

1. **S3 Image Upload**: Not implemented
   - ProfileViewModel has placeholder `uploadImageToS3()` function
   - Image compression logic ready but unused
   - Image selection works, but URL is not sent to backend
   - **Workaround:** Test without image upload for now

2. **Backend Deployment**: Profile endpoints not deployed yet
   - Backend code committed but not running on server
   - **Action Required:** Redeploy backend service

## Test Cases (From PRD)

### TC1: Name-only Change ✅ Ready

**Objective:** Verify nickname update without password change

**Prerequisites:**
- User logged in
- Navigate to Profile screen → Tap "프로필 수정"

**Steps:**
1. Enter current password in "현재 비밀번호" field
2. Modify "이름" field (e.g., change "홍길동" to "김철수")
3. Leave password fields empty
4. Tap "저장" button
5. Observe loading indicator
6. Verify success snackbar appears
7. Navigate back to profile screen

**Expected Results:**
- ✓ Nickname updated in profile header
- ✓ Password unchanged (can still login with old password)
- ✓ Session maintained (no re-login required)
- ✓ API response time < 500ms

**Test Data:**
```json
Request:
{
  "current_password": "a",
  "nickname": "김철수",
  "new_password": null,
  "profile_image_url": null
}

Expected Response:
{
  "user_id": 1,
  "account_id": "a",
  "nickname": "김철수",
  "profile_image_url": null,
  "default_room_id": null
}
```

---

### TC2: Password-only Change ✅ Ready

**Objective:** Verify password update without nickname change

**Steps:**
1. Enter current password
2. Enter new password (e.g., "newpass123")
3. Enter confirm password (matching)
4. Keep nickname unchanged
5. Tap "저장"

**Expected Results:**
- ✓ Password updated in database
- ✓ Can login with new password
- ✓ Cannot login with old password
- ✓ Nickname unchanged
- ✓ Session maintained (JWT token still valid)

**Validation:**
- Frontend: ViewModel validates password match
- Backend: Verifies current password before update
- After update: Try login with new password (should succeed)

---

### TC3: Profile Image-only Change ⚠️ Limited

**Objective:** Verify image selection and upload (partial)

**Current Limitation:** S3 upload not implemented

**Steps (UI Only):**
1. Tap profile image avatar
2. Select image from gallery (JPG or PNG)
3. Verify image preview shows in UI
4. Enter current password
5. Tap "저장"

**Expected Results (Current):**
- ✓ Image picker launches
- ✓ Selected image displays in circular avatar
- ✓ Image removed button appears
- ✗ Image NOT uploaded to S3
- ✗ profile_image_url NOT saved to database

**Expected Results (After S3 Implementation):**
- Image uploaded to S3 bucket `daily-dev/image/profile/`
- Filename: `{uuid}_{timestamp}.jpg`
- URL returned and saved to database
- Image displays from S3 URL on profile screen

---

### TC4: Simultaneous Update ✅ Ready

**Objective:** Verify all fields update atomically

**Steps:**
1. Change nickname
2. Change password (current + new + confirm)
3. ~~Select new profile image~~ (skip for now)
4. Tap "저장"

**Expected Results:**
- ✓ All changes applied in single transaction
- ✓ Success snackbar shows
- ✓ Navigate back automatically
- ✓ All fields reflect new values

**Edge Cases:**
- If API fails, no partial updates
- GORM transaction ensures atomicity

---

### TC5: Incorrect Current Password ✅ Ready

**Objective:** Verify error handling for wrong password

**Steps:**
1. Enter incorrect current password (e.g., "wrongpass")
2. Change nickname or password
3. Tap "저장"

**Expected Results:**
- ✓ API returns 400 error
- ✓ Error snackbar displays "현재 비밀번호가 올바르지 않습니다"
- ✓ No changes saved to database
- ✓ Form fields retain entered values
- ✓ User can correct and retry

**Backend Validation:**
```go
if user.Password != currentPassword {
    return fmt.Errorf("incorrect current password")
}
```

---

### TC6: New Password Mismatch ✅ Ready

**Objective:** Verify client-side password validation

**Steps:**
1. Enter correct current password
2. Enter new password (e.g., "newpass123")
3. Enter different confirm password (e.g., "newpass456")
4. Tap "저장"

**Expected Results:**
- ✓ ViewModel validation fails before API call
- ✓ Error snackbar displays "새 비밀번호가 일치하지 않습니다"
- ✓ No API request sent
- ✓ Loading state not triggered

**ViewModel Validation:**
```kotlin
if (newPassword != null && newPassword != confirmPassword) {
    return Result.failure(Exception("새 비밀번호가 일치하지 않습니다"))
}
```

---

## Additional Test Scenarios

### Edge Cases

#### Empty Fields Validation
- Current password required → Error: "현재 비밀번호를 입력해주세요"
- New password < 6 chars → Error: "새 비밀번호는 최소 6자 이상이어야 합니다"
- No changes made → Error: "변경된 내용이 없습니다"

#### Special Characters
- Nickname with emojis (e.g., "김철수 😊")
- Nickname with spaces
- Password with special chars (!@#$%^&*)

#### Large Image Upload (Future)
- Image > 10MB → Should compress
- Compression target: max 1024x1024, 80% quality
- Upload timeout: 10 seconds

#### Network Errors
- Backend offline → "네트워크 연결을 확인해주세요"
- Timeout → Retry or error handling
- 401 Unauthorized → "로그인이 필요합니다"

### Performance Benchmarks

| Operation | Target | Test Method |
|-----------|--------|-------------|
| Profile GET | < 200ms | Network inspector |
| Profile PUT | < 500ms | Network inspector (excluding S3) |
| S3 Upload | < 10s | For typical 2-5MB images |
| UI Response | < 100ms | Immediate feedback on tap |

### Security Validation

- [ ] Bearer token included in Authorization header
- [ ] Current password required for all updates
- [ ] Password sent over HTTPS only (production)
- [ ] Token refresh if expired
- [ ] No password in logs or error messages

## Test Execution Plan

### Phase 1: Backend Deployment ⚠️ Pending
```bash
# SSH to server
ssh user@13.203.37.93

# Pull latest code
cd /path/to/play_daily/backend
git pull origin main

# Rebuild and restart container
docker-compose down
docker-compose up -d --build

# Verify profile endpoints
curl -X GET http://localhost:7001/v0.1/profile \
  -H "Authorization: Bearer {token}"
```

### Phase 2: Frontend APK Build
```bash
cd frontend
./gradlew assembleDebug

# APK location: app/build/outputs/apk/debug/app-debug.apk
```

### Phase 3: Device Installation
```bash
# Connect device via ADB
adb devices

# Install APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n com.dailymemo/.MainActivity
```

### Phase 4: Test Execution

**Manual Testing Order:**
1. Login with test account (account_id: "a", password: "a")
2. Navigate to "내 정보" tab
3. Tap "프로필 수정" menu item
4. Execute TC5 (wrong password) → Verify error
5. Execute TC6 (password mismatch) → Verify error
6. Execute TC1 (nickname only) → Verify success
7. Execute TC2 (password only) → Verify success
8. Logout and re-login with new password → Verify success
9. Execute TC4 (simultaneous) → Verify success
10. ~~TC3 (image upload)~~ → Skip (S3 not implemented)

### Phase 5: Results Documentation

**Screenshot Checklist:**
- [ ] Profile Edit screen (initial state)
- [ ] Error snackbar (wrong password)
- [ ] Error snackbar (password mismatch)
- [ ] Success snackbar
- [ ] Updated profile header (after nickname change)
- [ ] Loading indicator
- [ ] Image picker dialog

**Performance Measurements:**
- [ ] API response times (use network inspector)
- [ ] UI responsiveness
- [ ] Memory usage (Android Profiler)

## Test Results Template

```markdown
## Test Execution Results

**Date:** {YYYY-MM-DD}
**Tester:** {Name}
**Device:** {Model, Android Version}
**Build:** {Git commit hash}

### TC1: Name-only Change
- Status: ✅ Pass / ❌ Fail
- Response Time: {ms}
- Notes: {Any observations}

### TC2: Password-only Change
- Status: ✅ Pass / ❌ Fail
- Response Time: {ms}
- Notes: {Any observations}

### TC3: Profile Image-only Change
- Status: ⚠️ Skipped (S3 not implemented)
- Notes: UI works, upload pending

### TC4: Simultaneous Update
- Status: ✅ Pass / ❌ Fail
- Response Time: {ms}
- Notes: {Any observations}

### TC5: Incorrect Current Password
- Status: ✅ Pass / ❌ Fail
- Error Message: {Actual error displayed}
- Notes: {Any observations}

### TC6: New Password Mismatch
- Status: ✅ Pass / ❌ Fail
- Error Message: {Actual error displayed}
- Notes: {Any observations}

### Issues Found
1. {Issue description}
2. {Issue description}

### Performance
- Average API response time: {ms}
- UI responsiveness: {Excellent/Good/Poor}
- Memory usage: {MB}
```

## Definition of Done

- [ ] All 6 test cases executed
- [ ] At least TC1, TC2, TC4, TC5, TC6 pass (TC3 partial)
- [ ] Performance benchmarks met
- [ ] No critical bugs found
- [ ] Test results documented with screenshots
- [ ] Backend logs reviewed (no errors)
- [ ] Feature ready for user acceptance testing

## Next Steps

1. **Immediate:**
   - Deploy backend with profile endpoints
   - Build and install frontend APK
   - Execute manual test cases

2. **Future Enhancements:**
   - Implement S3 image upload
   - Add automated UI tests (Espresso/UI Automator)
   - Add API integration tests
   - Performance profiling

3. **User Acceptance Testing:**
   - Share APK with stakeholders
   - Collect feedback
   - Fix any issues
   - Prepare for production release
