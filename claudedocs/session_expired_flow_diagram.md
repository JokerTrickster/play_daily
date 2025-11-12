# Session Expired Flow Diagram

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                          User Layer                                  │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    SessionExpiredDialog.kt                           │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │  "세션 만료"                                                │    │
│  │  "세션이 만료되었습니다. 다시 로그인해주세요."               │    │
│  │  "5초 후 자동으로 로그인 화면으로 이동합니다"                 │    │
│  │  [로그인 버튼]                                             │    │
│  └────────────────────────────────────────────────────────────┘    │
│                                                                       │
│  LaunchedEffect: delay(5000) → onNavigateToLogin()                  │
└─────────────────────────────────────────────────────────────────────┘
                                  ▲
                                  │ Shows dialog
                                  │
┌─────────────────────────────────────────────────────────────────────┐
│                         MainActivity.kt                              │
│                                                                       │
│  • Implements SessionExpiredCallback                                 │
│  • Manages showSessionExpiredDialog state                            │
│  • Handler(Looper.getMainLooper()).post { show dialog }             │
│  • onDestroy() → clears callback (prevent memory leak)               │
│  • navigationHandler → NavController.navigate(Login)                 │
└─────────────────────────────────────────────────────────────────────┘
                                  ▲
                                  │ onSessionExpired()
                                  │ (from OkHttp thread)
┌─────────────────────────────────────────────────────────────────────┐
│                   TokenRefreshInterceptor.kt                         │
│                                                                       │
│  • intercept(chain): Response                                        │
│  • if (response.code == 401)                                         │
│    ├─ attemptTokenRefresh() with Mutex                               │
│    │  ├─ Success → retry original request                            │
│    │  └─ Failure → clearTokens() + onSessionExpired()                │
│  • setSessionExpiredCallback(callback)                               │
└─────────────────────────────────────────────────────────────────────┘
                                  ▲
                                  │ HTTP Interceptor Chain
┌─────────────────────────────────────────────────────────────────────┐
│                          OkHttpClient                                │
│                                                                       │
│  Interceptors:                                                       │
│  1. AuthInterceptor (add "tkn" header)                               │
│  2. TokenRefreshInterceptor (handle 401)                             │
│  3. LoggingInterceptor (debug logs)                                  │
└─────────────────────────────────────────────────────────────────────┘
                                  ▲
                                  │ API Calls
┌─────────────────────────────────────────────────────────────────────┐
│              ViewModels / Repositories / API Services                │
│                                                                       │
│  • MemoApiService.getMemoList()                                      │
│  • ProfileApiService.getProfile()                                    │
│  • RoomApiService.getRoomList()                                      │
│  • etc.                                                              │
└─────────────────────────────────────────────────────────────────────┘
```

## Token Refresh Success Flow

```
1. User on Map Screen
        │
        ▼
2. Access Token Expires (natural expiry after 1 hour)
        │
        ▼
3. User triggers API call (e.g., refresh memos)
        │
        ▼
4. API returns 401 Unauthorized
        │
        ▼
5. TokenRefreshInterceptor detects 401
        │
        ▼
6. Mutex.lock() - prevent concurrent refresh
        │
        ▼
7. Call /v0.1/auth/reissue with access + refresh tokens
        │
        ▼
8. Backend validates refresh token
        │
        ▼
9. Backend returns new access + refresh tokens
        │
        ▼
10. AuthLocalDataSource.updateTokens()
        │
        ▼
11. Mutex.unlock()
        │
        ▼
12. Retry original request with new access token
        │
        ▼
13. Request succeeds - user sees no interruption ✓
```

## Session Expired Flow (Both Tokens Invalid)

```
1. User on Map Screen
        │
        ▼
2. Both tokens expire OR refresh token becomes invalid
        │
        ▼
3. User triggers API call
        │
        ▼
4. API returns 401 Unauthorized
        │
        ▼
5. TokenRefreshInterceptor detects 401
        │
        ▼
6. Mutex.lock()
        │
        ▼
7. Call /v0.1/auth/reissue
        │
        ▼
8. Backend validates refresh token → FAILS
        │
        ▼
9. Reissue API returns 401 (refresh token invalid)
        │
        ▼
10. attemptTokenRefresh() returns false
        │
        ▼
11. AuthLocalDataSource.clearTokens()
        │
        ▼
12. sessionExpiredCallback?.onSessionExpired()
        │  (invoked on OkHttp worker thread)
        │
        ▼
13. MainActivity.onSessionExpired()
        │  Handler(Looper.getMainLooper()).post {
        │
        ▼
14. showSessionExpiredDialog = true
        │  (on Main/UI thread)
        │
        ▼
15. SessionExpiredDialog composable renders
        │
        ├─ User sees dialog for 5 seconds
        │  ├─ Option A: User clicks "로그인" button
        │  │     → onNavigateToLogin() immediately
        │  │
        │  └─ Option B: Auto-dismiss after 5 seconds
        │        → LaunchedEffect triggers onNavigateToLogin()
        │
        ▼
16. NavController.navigate(Screen.Auth.Login.route)
        │  popUpTo(0) { inclusive = true }
        │
        ▼
17. User on Login Screen
        │
        ▼
18. User re-authenticates
        │
        ▼
19. New tokens stored ✓
```

## Thread Safety Model

```
┌─────────────────────────────────────────────────────────────────────┐
│                      OkHttp Worker Thread                            │
│                                                                       │
│  TokenRefreshInterceptor.intercept()                                 │
│         │                                                            │
│         ├─ 401 detected                                              │
│         ├─ attemptTokenRefresh()                                     │
│         └─ if failed:                                                │
│               clearTokens()                                          │
│               sessionExpiredCallback?.onSessionExpired()             │
│                     │                                                │
│                     │ (callback invoked on worker thread)            │
└─────────────────────┼────────────────────────────────────────────────┘
                      │
                      │ Thread Transition
                      ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         Main Thread                                  │
│                                                                       │
│  Handler(Looper.getMainLooper()).post {                              │
│      showSessionExpiredDialog = true  ← mutableStateOf              │
│  }                                                                   │
│         │                                                            │
│         ▼                                                            │
│  Compose Recomposition                                               │
│         │                                                            │
│         ▼                                                            │
│  SessionExpiredDialog renders                                        │
└─────────────────────────────────────────────────────────────────────┘
```

## Concurrency Control (Mutex)

```
Multiple API Calls Hit 401 Simultaneously:

Thread 1        Thread 2        Thread 3        Thread 4
   │               │               │               │
   ├─ 401 detected │               │               │
   ├─ Mutex.lock() ✓               │               │
   ├─ refreshing...│               │               │
   │               ├─ 401 detected │               │
   │               ├─ Mutex.lock() │               │
   │               │  (waiting...)  │               │
   │               │               ├─ 401 detected │
   │               │               ├─ Mutex.lock() │
   │               │               │  (waiting...)  │
   │               │               │               ├─ 401 detected
   │               │               │               ├─ Mutex.lock()
   │               │               │               │  (waiting...)
   ├─ refresh done │               │               │
   ├─ Mutex.unlock()               │               │
   ├─ retry request✓               │               │
   │               ├─ Mutex.lock() ✓               │
   │               ├─ token already refreshed       │
   │               ├─ Mutex.unlock()               │
   │               ├─ retry request✓               │
   │               │               ├─ Mutex.lock() ✓
   │               │               ├─ already done  │
   │               │               ├─ Mutex.unlock()
   │               │               ├─ retry ✓       │
   │               │               │               ├─ Mutex.lock() ✓
   │               │               │               ├─ already done
   │               │               │               ├─ Mutex.unlock()
   │               │               │               ├─ retry ✓

Result: Only ONE actual token refresh API call to backend
        All other threads reuse the refreshed token
```

## Memory Management

```
Activity Lifecycle:

onCreate()
    ├─ tokenRefreshInterceptor.setSessionExpiredCallback(this)
    │  (MainActivity registers itself as callback)
    │
    ▼
onResume()
    │ App is active, callback is live
    │
    ▼
onPause()
    │ App going to background, callback still live
    │
    ▼
onDestroy()
    └─ tokenRefreshInterceptor.setSessionExpiredCallback(null)
       (Clear callback to prevent memory leak)

Memory Leak Prevention:
• Callback is null when Activity is destroyed
• No strong reference from Interceptor (singleton) to Activity
• Handler.post ensures no leaks from background threads
```

## Testing Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Unit Tests (Fast)                             │
│                                                                       │
│  TokenRefreshE2ETest.kt                                              │
│  ├─ testSessionExpiredDialogDisplaysCorrectly()                      │
│  ├─ testAutoNavigationAfterTimeout()                                 │
│  ├─ testManualNavigationViaLoginButton()                             │
│  └─ testDialogNotShownWhenShowDialogIsFalse()                        │
│                                                                       │
│  Uses: createComposeRule() for isolated component testing            │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                   Integration Tests (Manual)                         │
│                                                                       │
│  README_TOKEN_REFRESH_TESTS.md scenarios:                            │
│  ├─ Scenario 1: Successful token refresh                             │
│  ├─ Scenario 2: Session expired (both tokens invalid)                │
│  ├─ Scenario 3: Concurrent API calls (mutex test)                    │
│  ├─ Scenario 4: Thread safety verification                           │
│  └─ Scenario 5: Configuration changes (rotation)                     │
│                                                                       │
│  Requires: Backend server + real network                             │
└─────────────────────────────────────────────────────────────────────┘
```
