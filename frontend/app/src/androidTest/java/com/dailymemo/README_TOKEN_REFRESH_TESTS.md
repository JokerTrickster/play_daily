# Token Refresh E2E Testing Guide

## Overview
This document describes the testing approach for the token refresh system, including both automated UI tests and manual integration testing.

## Test Structure

### Automated UI Tests (`TokenRefreshE2ETest.kt`)
Tests the SessionExpiredDialog component in isolation:
- Dialog displays correctly with all expected elements
- Auto-dismiss after 5 seconds triggers navigation callback
- Manual button click triggers immediate navigation
- Dialog properly hides when showDialog is false

**Running the tests:**
```bash
cd /Users/luxrobo/project/play_daily/frontend
./gradlew :app:connectedAndroidTest --tests "com.dailymemo.TokenRefreshE2ETest"
```

## Manual Integration Testing

### Prerequisites
1. Backend server running with token refresh endpoint
2. Android device or emulator connected
3. Valid test user account

### Test Scenario 1: Successful Token Refresh
**Goal:** Verify automatic token refresh when access token expires

**Steps:**
1. Login to the app with test credentials
2. Navigate to map screen
3. Using ADB or developer tools, manually expire the access token:
   ```kotlin
   // In AuthLocalDataSource, tokens are stored in DataStore
   // You can modify the expiry timestamp to simulate expiration
   ```
4. Trigger an API call (e.g., pull to refresh memo list)
5. **Expected Result:**
   - Request should succeed after automatic token refresh
   - No session expired dialog appears
   - New access token is stored in DataStore
   - User remains on current screen

### Test Scenario 2: Session Expired (Both Tokens Invalid)
**Goal:** Verify session expired dialog when refresh token also expires

**Steps:**
1. Login to the app
2. Navigate to any screen
3. Manually expire both access and refresh tokens
4. Trigger an API call
5. **Expected Result:**
   - Session expired dialog appears with message "세션이 만료되었습니다. 다시 로그인해주세요."
   - Auto-redirect message shows "5초 후 자동으로 로그인 화면으로 이동합니다"
   - After 5 seconds, app navigates to login screen
   - All tokens are cleared from storage

### Test Scenario 3: Concurrent API Calls
**Goal:** Verify mutex protection prevents multiple simultaneous token refresh attempts

**Steps:**
1. Login to the app
2. Expire access token
3. Trigger multiple API calls simultaneously (e.g., rapidly switch between screens)
4. Monitor network logs
5. **Expected Result:**
   - Only ONE token refresh API call is made
   - All concurrent requests wait for refresh to complete
   - All requests are retried with new token
   - No race conditions or duplicate refresh calls

### Test Scenario 4: Thread Safety
**Goal:** Verify callback from OkHttp thread is properly handled on main thread

**Steps:**
1. Login to the app
2. Expire both tokens
3. Make an API call
4. **Expected Result:**
   - Session expired dialog appears without crashes
   - No "CalledFromWrongThreadException" errors
   - UI updates happen smoothly on main thread

### Test Scenario 5: Configuration Changes
**Goal:** Verify dialog survives configuration changes (rotation)

**Steps:**
1. Login and expire tokens
2. Trigger session expired dialog
3. Rotate the device
4. **Expected Result:**
   - Dialog remains visible after rotation
   - Auto-dismiss timer continues
   - No memory leaks or crashes

## Debugging Tips

### Enable Detailed Logging
Add logs in `TokenRefreshInterceptor.kt` to track:
- Token expiration checks
- Refresh attempts
- Mutex lock/unlock events
- Callback invocations

### Verify Token Storage
Check DataStore contents:
```bash
adb shell run-as com.dailymemo cat /data/data/com.dailymemo/files/datastore/auth_prefs.preferences_pb
```

### Monitor Network Traffic
Use Charles Proxy or similar to verify:
- Original 401 responses
- Token refresh requests
- Retried requests with new token

## Performance Considerations

### Target Metrics
- Token refresh latency: < 500ms
- Dialog display latency: < 100ms
- Memory usage: No leaks after 10+ dialog cycles
- Thread transitions: < 50ms from OkHttp to main thread

## Known Limitations

1. **Network Dependency**: Full integration tests require backend server
2. **Time-Based Testing**: Auto-dismiss tests take 5+ seconds to complete
3. **State Persistence**: Tests don't verify token persistence across app restarts

## Future Enhancements

1. Add MockWebServer for offline testing
2. Add Espresso UI tests for full user journey
3. Add performance benchmarks for token refresh
4. Add accessibility testing with TalkBack
5. Add tests for edge cases (airplane mode, server errors, malformed responses)
