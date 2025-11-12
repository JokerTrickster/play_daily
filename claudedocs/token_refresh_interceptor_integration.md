# Token Refresh Interceptor Integration Guide

## Overview
The TokenRefreshInterceptor automatically handles 401 Unauthorized errors by refreshing access tokens using the refresh token. This guide explains the implementation and how to integrate the session expired callback.

## Implementation Details

### Files Created
1. **DTOs**:
   - `/frontend/app/src/main/java/com/dailymemo/data/models/request/ReissueTokenRequest.kt`
   - `/frontend/app/src/main/java/com/dailymemo/data/models/response/ReissueTokenResponse.kt`

2. **API Service**:
   - Updated `/frontend/app/src/main/java/com/dailymemo/data/datasources/remote/api/AuthApiService.kt`
   - Added `reissueToken()` method for POST `/v0.1/auth/reissue`

3. **Interceptor**:
   - `/frontend/app/src/main/java/com/dailymemo/data/network/TokenRefreshInterceptor.kt`

4. **DI Integration**:
   - Updated `/frontend/app/src/main/java/com/dailymemo/di/AppModule.kt`

5. **Tests**:
   - `/frontend/app/src/test/java/com/dailymemo/data/network/TokenRefreshInterceptorTest.kt`

### How It Works

1. **401 Detection**: When any API call returns 401 Unauthorized, the interceptor catches it
2. **Concurrent Control**: Uses Mutex to ensure only one token refresh happens at a time
3. **Token Refresh**: Calls POST `/v0.1/auth/reissue` with current tokens
4. **Update Tokens**: On success, updates stored tokens via `AuthLocalDataSource.updateTokens()`
5. **Retry Request**: Retries the original request once with new access token
6. **Failure Handling**: On refresh failure, clears tokens and triggers session expired callback

### Features
- Mutex-based concurrency control prevents multiple concurrent refreshes
- Lazy injection of AuthApiService to avoid circular dependency
- Single retry only (prevents infinite loops)
- Auth endpoints bypass the interceptor
- Proper error handling and logging
- SessionExpiredCallback for navigation to login

## MainActivity Integration (Optional)

If you want to navigate to login screen when token refresh fails, implement the callback:

```kotlin
package com.dailymemo.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.dailymemo.data.network.TokenRefreshInterceptor
import com.dailymemo.presentation.navigation.NavGraph
import com.dailymemo.presentation.theme.DailyMemoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(), TokenRefreshInterceptor.SessionExpiredCallback {

    @Inject
    lateinit var tokenRefreshInterceptor: TokenRefreshInterceptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set session expired callback (optional)
        // tokenRefreshInterceptor.sessionExpiredCallback = this

        setContent {
            DailyMemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph()
                }
            }
        }
    }

    override fun onSessionExpired() {
        // Handle session expiration - navigate to login screen
        // This is called when token refresh fails
        runOnUiThread {
            // Example: Navigate to login screen
            // navController.navigate("login") {
            //     popUpTo(0) { inclusive = true }
            // }
        }
    }
}
```

**Note**: The callback is currently set to `null` in AppModule. You can either:
1. Keep it null and let the app continue (tokens are cleared automatically)
2. Implement the callback in MainActivity to navigate to login screen

## API Endpoint

The interceptor calls the following endpoint to refresh tokens:

**Endpoint**: `POST /v0.1/auth/reissue`

**Request Body**:
```json
{
  "access_token": "current_access_token",
  "refresh_token": "current_refresh_token"
}
```

**Response Body**:
```json
{
  "access_token": "new_access_token",
  "access_token_expired_at": 1234567890,
  "refresh_token": "new_refresh_token",
  "refresh_token_expired_at": 1234567890
}
```

## Testing

Comprehensive unit tests are included in `TokenRefreshInterceptorTest.kt`:

1. Auth endpoint bypass
2. Successful requests pass through normally
3. 401 triggers token refresh and retry
4. No tokens available clears storage
5. Failed token refresh clears tokens and triggers callback
6. Token refresh exception handling
7. Reissue endpoint bypass

Run tests:
```bash
./gradlew test --tests TokenRefreshInterceptorTest
```

## Flow Diagram

```
API Request → 401 Error
    ↓
Check if auth endpoint → Yes → Proceed normally
    ↓ No
Lock Mutex
    ↓
Get current tokens
    ↓
Call POST /v0.1/auth/reissue
    ↓
Success? → Yes → Update tokens → Retry request → Return response
    ↓ No
Clear tokens → Trigger callback → Return 401
```

## Concurrency Handling

Multiple concurrent 401 responses are handled safely:
- First request acquires Mutex lock
- Other requests wait for lock
- Only one refresh API call is made
- All waiting requests use the new token after refresh completes

## Error Scenarios

1. **No tokens available**: Clears storage, triggers callback
2. **Refresh API fails**: Clears storage, triggers callback
3. **Network exception**: Clears storage, triggers callback
4. **Refresh succeeds but retry fails**: Returns failed response (not 401)

## Logging

The interceptor includes comprehensive logging:
- Request interception
- 401 detection
- Token refresh attempts
- Success/failure outcomes
- All logs use tag `TokenRefreshInterceptor`

## Integration Checklist

- [x] DTOs created (ReissueTokenRequest, ReissueTokenResponse)
- [x] AuthApiService updated with reissueToken() method
- [x] TokenRefreshInterceptor implemented with Mutex
- [x] Interceptor integrated into OkHttpClient in AppModule
- [x] Unit tests created
- [ ] (Optional) MainActivity implements SessionExpiredCallback
- [ ] Backend implements POST /v0.1/auth/reissue endpoint
- [ ] Test with expired tokens in production

## Notes

- The interceptor is automatically applied to all API requests except auth endpoints
- Tokens are stored and retrieved via AuthLocalDataSource (DataStore)
- The refresh is attempted only once per 401 to prevent infinite loops
- Auth endpoints (`/auth/*`) bypass the interceptor entirely
- Lazy injection is used for AuthApiService to prevent circular dependency issues
