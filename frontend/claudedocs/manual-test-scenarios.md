# Manual Test Scenarios - Issue #11 Polish & Error Handling

## Overview
Comprehensive manual testing scenarios for validating error handling, Korean localization, loading states, and overall polish.

**Target**: 20+ scenarios covering all user flows with error conditions

---

## 1. Loading States & Animations (5 scenarios)

### 1.1 Map Search Loading
- **Steps**:
  1. Open map screen
  2. Enter search query (min 2 characters)
  3. Click search button
- **Expected**:
  - Search button shows CircularProgressIndicator
  - Button is disabled during search
  - Results load within 2 seconds
  - Shimmer placeholders appear if slow network

### 1.2 Memo List Loading
- **Steps**:
  1. Navigate to memo list
  2. Observe initial load
- **Expected**:
  - MemoListItemSkeleton appears (3 items)
  - Shimmer animation runs smoothly (800ms cycle)
  - Transitions to actual data seamlessly

### 1.3 Timeline Loading
- **Steps**:
  1. Navigate to timeline view
  2. Observe initial load
- **Expected**:
  - TimelineSkeletonDateHeader appears
  - TimelineSkeletonItem placeholders (2 groups × 3 items)
  - Smooth transition to real data

### 1.4 Image Upload Compression
- **Steps**:
  1. Create new memo
  2. Select large image (>2MB)
  3. Observe compression
- **Expected**:
  - Image scales to max 1920×1920
  - Compressed to <500KB
  - Quality remains acceptable
  - No memory leaks (check via Android Profiler)

### 1.5 Navigation Animations
- **Steps**:
  1. Navigate between screens (Map → List → Timeline → Detail)
  2. Use back navigation
- **Expected**:
  - Smooth 300ms slide animations
  - Proper enter/exit transitions
  - No animation jank or stuttering

---

## 2. Error Handling - Network Errors (6 scenarios)

### 2.1 Network Unavailable - Map Search
- **Setup**: Enable airplane mode
- **Steps**:
  1. Open map screen
  2. Enter search query
  3. Click search
- **Expected**:
  - ErrorDisplay component shows
  - Korean message: "네트워크에 연결되어 있지 않습니다"
  - "다시 시도" button appears
  - Clicking retry attempts search again

### 2.2 Network Unavailable - Memo List
- **Setup**: Enable airplane mode
- **Steps**: Navigate to memo list
- **Expected**:
  - ErrorDisplay with network error message (Korean)
  - Retry button functional
  - No app crash

### 2.3 Server Error (500)
- **Setup**: Backend returns 500 error
- **Steps**: Perform any API operation
- **Expected**:
  - ErrorDisplay shows "서버 오류가 발생했습니다"
  - Retry option available
  - Error logged to console

### 2.4 Timeout Error
- **Setup**: Simulate slow network (Chrome DevTools throttling)
- **Steps**: Load memo list
- **Expected**:
  - Loading state for reasonable time (max 10s)
  - Timeout error after 10s
  - Korean timeout message displayed

### 2.5 Unauthorized (401)
- **Setup**: Invalid/expired token
- **Steps**: Try to create memo
- **Expected**:
  - ErrorDisplay shows "인증이 필요합니다"
  - Suggests re-login
  - No sensitive data exposed

### 2.6 Not Found (404)
- **Setup**: Request non-existent memo ID
- **Steps**: Navigate to memo detail with invalid ID
- **Expected**:
  - ErrorDisplay shows "요청한 메모를 찾을 수 없습니다"
  - Back navigation available

---

## 3. Error Handling - Validation Errors (4 scenarios)

### 3.1 Empty Title
- **Steps**:
  1. Create new memo
  2. Leave title empty
  3. Try to save
- **Expected**:
  - Validation error: "제목을 입력해주세요"
  - Save button disabled or shows error
  - Form remains editable

### 3.2 Invalid Location
- **Steps**:
  1. Create memo
  2. Select invalid coordinates
- **Expected**:
  - Error message: "위치 정보가 올바르지 않습니다"
  - Can retry location selection

### 3.3 Image Too Large (Pre-compression)
- **Steps**:
  1. Select extremely large image (>10MB)
  2. Observe compression
- **Expected**:
  - ImageCompressor handles gracefully
  - Shows compression progress if >2s
  - Final size <500KB
  - No OutOfMemoryError

### 3.4 Invalid Rating
- **Steps**:
  1. Try to set rating outside 0-5 range
- **Expected**:
  - Client-side validation prevents
  - If bypassed, server rejects with validation error

---

## 4. Korean Localization (3 scenarios)

### 4.1 All UI Strings
- **Steps**: Navigate through all screens
- **Expected**:
  - All labels, buttons, placeholders in Korean
  - No English fallbacks visible
  - Proper spacing and formatting

### 4.2 Error Messages
- **Steps**: Trigger various errors (network, validation, server)
- **Expected**:
  - All error messages in Korean
  - Natural phrasing (not machine-translated)
  - Appropriate formality level

### 4.3 Date/Time Formatting
- **Steps**: View timeline, memo details
- **Expected**:
  - Dates: "yyyy년 MM월 dd일 (E)" format
  - Relative: "오늘", "어제"
  - Times: "HH:mm" 24-hour format

---

## 5. Performance & Polish (4 scenarios)

### 5.1 App Startup Time
- **Setup**: Cold start (force stop app first)
- **Steps**: Launch app
- **Expected**:
  - Splash screen → Main screen < 2 seconds
  - No ANR (Application Not Responding)
  - Smooth initial render

### 5.2 List Scroll Performance
- **Setup**: Load 100+ memos
- **Steps**: Scroll rapidly through list
- **Expected**:
  - Maintains 60fps (use GPU profiling)
  - No dropped frames
  - Smooth image loading with Coil

### 5.3 Memory Usage
- **Setup**: Use app for 10 minutes with various operations
- **Steps**:
  1. Create 20+ memos
  2. Navigate between screens
  3. Search multiple times
  4. Upload images
- **Expected**:
  - Memory usage stable (no continuous growth)
  - No memory leaks detected by LeakCanary
  - Garbage collection doesn't cause jank

### 5.4 Release Build ProGuard
- **Setup**: Build release APK with ProGuard
- **Steps**:
  1. `./gradlew assembleRelease`
  2. Install APK
  3. Test all features
- **Expected**:
  - APK size < 50MB
  - All features functional
  - No runtime crashes from over-obfuscation
  - Crash logs still readable (line numbers preserved)

---

## 6. Edge Cases (2 scenarios)

### 6.1 Rapid Button Clicks
- **Steps**: Rapidly click search/save buttons
- **Expected**:
  - Debouncing prevents duplicate requests
  - Loading state protects against race conditions
  - No duplicate data created

### 6.2 Screen Rotation
- **Steps**: Rotate device during various operations
- **Expected**:
  - State preserved (using ViewModel)
  - Loading states persist correctly
  - No data loss
  - Layouts adapt responsively

---

## Testing Checklist

- [ ] All 5 loading state scenarios passed
- [ ] All 6 network error scenarios passed
- [ ] All 4 validation error scenarios passed
- [ ] All 3 localization scenarios passed
- [ ] All 4 performance scenarios passed
- [ ] All 2 edge case scenarios passed

**Total**: 24 scenarios

---

## Tools Required

1. **Android Studio Profiler**: Memory, CPU, GPU profiling
2. **LeakCanary**: Memory leak detection
3. **Chrome DevTools**: Network throttling simulation
4. **ADB**: Airplane mode toggling, network simulation
5. **GPU Profiling**: `adb shell setprop debug.hwui.profile visual_bars`

---

## Acceptance Criteria

✅ **Pass**: All 24 scenarios execute without crashes
✅ **Pass**: All error messages in Korean
✅ **Pass**: All loading states smooth and responsive
✅ **Pass**: Performance targets met (startup <2s, 60fps scrolling)
✅ **Pass**: Release build functional with ProGuard
✅ **Pass**: No memory leaks detected

**Issue #11 Complete When**: All criteria met + documented in GitHub issue
