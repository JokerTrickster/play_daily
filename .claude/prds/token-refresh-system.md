---
name: token-refresh-system
description: Automatic token refresh system with rotation, secure storage, and error recovery
status: backlog
created: 2025-11-11T08:39:50Z
---

# PRD: Token Refresh System

## Executive Summary

Implement a comprehensive JWT token refresh system to maintain user sessions without requiring re-authentication. This system will automatically refresh expired access tokens using refresh tokens, implement secure token rotation, handle concurrent requests gracefully, and provide clear user feedback when re-authentication is required.

### Value Proposition
- **Improved UX**: Users stay logged in without interruption during active sessions
- **Enhanced Security**: Token rotation prevents replay attacks and limits token lifetime
- **Reliability**: Automatic retry logic handles network failures gracefully
- **Transparency**: Clear session expiry notifications guide users

---

## Problem Statement

### Current Pain Points
1. **No automatic token refresh**: When access tokens expire (24 hours), users must manually log in again
2. **Stateless tokens**: No server-side token storage means no ability to revoke compromised tokens
3. **Poor error handling**: 401 errors immediately fail requests without attempting token refresh
4. **Security gaps**: Long-lived tokens without rotation increase security risk
5. **User friction**: Unexpected logouts disrupt user workflows

### Why Now?
- User retention is critical for app adoption
- Security best practices require token rotation
- Production readiness requires robust session management
- User feedback indicates frustration with frequent re-logins

---

## User Stories

### Primary Personas

**1. Active User (Daily User)**
- As a daily user, I want my session to remain active while I use the app, so I don't have to re-enter credentials
- As a daily user, I want clear notification when my session expires, so I understand why I'm logged out

**2. Security-Conscious User**
- As a security-conscious user, I want my tokens to be stored securely, so my account can't be compromised
- As a security-conscious user, I want old tokens invalidated after refresh, so stolen tokens become useless quickly

**3. Mobile User (Network Variability)**
- As a mobile user, I want the app to retry failed requests automatically, so temporary network issues don't disrupt my experience
- As a mobile user, I want pending operations to complete after token refresh, so I don't lose my work

---

## Requirements

### Functional Requirements

#### Backend (Go Server)

**FR-1: Token Storage & Management**
- Create `tokens` table with columns:
  - `id` (primary key)
  - `user_id` (foreign key to users table, indexed)
  - `access_token` (varchar(500), indexed)
  - `refresh_token` (varchar(500), indexed, unique)
  - `access_token_expires_at` (timestamp)
  - `refresh_token_expires_at` (timestamp)
  - `created_at` (timestamp)
  - `updated_at` (timestamp)
  - `revoked_at` (nullable timestamp, for manual revocation)
- Store tokens in database upon login/signup
- Clean up expired tokens periodically (optional background job)

**FR-2: Token Reissue Endpoint**
- Endpoint: `POST /v0.1/auth/reissue`
- Request body:
  ```json
  {
    "access_token": "string",
    "refresh_token": "string"
  }
  ```
- Response (200 OK):
  ```json
  {
    "access_token": "string",
    "refresh_token": "string",
    "access_token_expired_at": int64,
    "refresh_token_expired_at": int64
  }
  ```
- Error responses:
  - 401 Unauthorized (refresh token expired/invalid)
  - 400 Bad Request (missing required fields)

**FR-3: Token Rotation Logic**
- When reissue endpoint is called:
  1. Verify refresh token signature and expiration
  2. Check refresh token exists in database and not revoked
  3. If refresh token expired → return 401 error
  4. If valid → generate new access token AND new refresh token
  5. Update database: set old refresh token's `revoked_at` to current timestamp
  6. Insert new token pair into database
  7. Return new tokens to client

**FR-4: Token Expiration Configuration**
- Access token: 24 hours (1 day)
- Refresh token: 720 hours (30 days)
- Update `common/jwt.go` constants:
  ```go
  AccessTokenExpiredTime  = 24          // hours (1 day)
  RefreshTokenExpiredTime = 24 * 30     // hours (30 days)
  ```

**FR-5: Enhanced Middleware**
- Update `middleware/tokenChecker.go` to:
  1. Extract access token from `tkn` header
  2. Verify token signature
  3. Check if token exists in database and not revoked
  4. If token invalid/expired → return 401 with clear error message
  5. Continue request processing if valid

#### Frontend (Android App)

**FR-6: Token Refresh Interceptor**
- Implement Retrofit interceptor to:
  1. Detect 401 Unauthorized responses
  2. Attempt token refresh using stored refresh token
  3. Retry original request with new access token
  4. Fail if refresh token also expired

**FR-7: Token Storage (Enhanced)**
- Continue using DataStore (already secure)
- Store additional fields:
  - `access_token_expired_at` (Long)
  - `refresh_token_expired_at` (Long)
- Add methods:
  - `getRefreshToken(): String?`
  - `updateTokens(accessToken, refreshToken, expirations)`

**FR-8: Automatic Retry Logic**
- On API call failure with 401:
  1. Lock shared refresh state (prevent concurrent refreshes)
  2. Call `POST /v0.1/auth/reissue` with current tokens
  3. If refresh succeeds:
     - Update stored tokens
     - Retry original request once
  4. If refresh fails (refresh token expired):
     - Clear all tokens
     - Show "Session expired" dialog
     - Navigate to login screen
  5. Unlock shared refresh state

**FR-9: Session Expiry Dialog**
- Show dialog when refresh token expires:
  - Title: "Session Expired"
  - Message: "Your session has expired. Please log in again."
  - Button: "Log In" (navigates to login screen)
- Auto-dismiss after 5 seconds with navigation

**FR-10: Background Token Refresh (Optional)**
- Proactively refresh access token 5 minutes before expiration
- Only when app is in foreground
- Reduces mid-operation failures

### Non-Functional Requirements

**NFR-1: Performance**
- Token refresh should complete within 500ms (P95)
- Database queries should use indexes on `user_id`, `refresh_token`, `access_token`
- Token reissue endpoint should handle 100 requests/second

**NFR-2: Security**
- Use HTTPS for all token transmission (already enforced)
- Refresh tokens are single-use (invalidated after successful refresh)
- No token information logged in production
- Database stores hashed tokens (optional enhancement)

**NFR-3: Reliability**
- Implement exponential backoff for refresh attempts (1s, 2s, 4s max)
- Handle concurrent refresh attempts (single refresh for multiple 401s)
- Graceful degradation: if token refresh fails, clear tokens and prompt login

**NFR-4: Scalability**
- Token storage should support millions of users
- Implement token cleanup job to delete expired tokens (runs daily)
- Use database connection pooling efficiently

**NFR-5: Observability**
- Log token refresh attempts (success/failure rates)
- Monitor 401 error rates
- Track token expiration patterns
- Alert on unusual token refresh spikes

---

## Success Criteria

### Quantitative Metrics
1. **User Retention**: <5% session drop-off rate due to token expiration
2. **API Success Rate**: 401 errors reduced by >95% after implementation
3. **Performance**: Token refresh latency <500ms (P95)
4. **Security**: Zero successful token replay attacks detected

### Qualitative Outcomes
1. Users report smooth, uninterrupted app experience
2. No user complaints about unexpected logouts
3. Security audit passes token management review
4. Development team can revoke tokens when needed

---

## Constraints & Assumptions

### Constraints
1. **Database Schema**: Must maintain backward compatibility with existing users table
2. **API Versioning**: Reissue endpoint uses existing `/v0.1/` path structure
3. **Android Min SDK**: Must work on Android API 21+ (current app requirement)
4. **Network**: Assume intermittent connectivity (mobile environment)

### Assumptions
1. User device clocks are reasonably accurate (within 5 minutes)
2. DataStore is secure enough for token storage (no need for Android Keystore initially)
3. Users typically have <30 concurrent sessions
4. Refresh token rotation is acceptable UX (no persistent "remember me")

---

## Out of Scope

### Explicitly NOT Building
1. **Biometric Authentication**: Fingerprint/Face ID for token refresh
2. **Multi-Device Session Management**: View/revoke sessions from other devices
3. **Token Migration Tool**: Migrate existing users from stateless to stateful tokens
4. **Admin Dashboard**: View/revoke user tokens manually
5. **Device Fingerprinting**: Bind tokens to specific devices
6. **IP-Based Validation**: Validate requests from same IP as token issuance
7. **OAuth2 Integration**: Third-party login providers (Google, Apple, etc.)
8. **Token Encryption**: Store encrypted tokens in database (plain storage acceptable initially)

---

## Dependencies

### External Dependencies
1. **Database**: MySQL/PostgreSQL (already in use)
2. **JWT Library**: `github.com/golang-jwt/jwt` (already integrated)
3. **Android DataStore**: `androidx.datastore:datastore-preferences` (already integrated)
4. **Retrofit**: HTTP client for Android (already integrated)

### Internal Dependencies
1. **Auth Team**: Must coordinate token generation changes
2. **Backend Team**: Deploy database migration before API changes
3. **Mobile Team**: Update API client to handle new reissue endpoint
4. **DevOps Team**: Monitor error rates during rollout

### Deployment Order
1. Deploy database migration (create `tokens` table)
2. Update backend token generation to store in database
3. Deploy reissue endpoint
4. Release Android app update with token refresh logic
5. Monitor metrics for 1 week
6. Remove legacy code if successful

---

## Implementation Phases

### Phase 1: Backend Token Storage (1 week)
- [ ] Create database migration for `tokens` table
- [ ] Update `common/jwt.go` to store tokens in database
- [ ] Update `signInAuthUseCase` and `signUpAuthUseCase` to store tokens
- [ ] Update middleware to check database for token validity
- [ ] Write unit tests for token storage logic

### Phase 2: Backend Reissue Endpoint (1 week)
- [ ] Create `reissueAuthHandler.go` in `features/auth/handler/`
- [ ] Create `reissueAuthUseCase.go` in `features/auth/usecase/`
- [ ] Create `reissueAuthRepository.go` in `features/auth/repository/`
- [ ] Implement token rotation logic (invalidate old, generate new)
- [ ] Add endpoint to router
- [ ] Write integration tests for reissue flow

### Phase 3: Frontend Token Refresh (1.5 weeks)
- [ ] Update `AuthLocalDataSource.kt` with token expiration fields
- [ ] Create `TokenRefreshInterceptor.kt` for Retrofit
- [ ] Implement retry logic in interceptor
- [ ] Create `SessionExpiredDialog.kt` composable
- [ ] Update `AuthRepositoryImpl.kt` with refresh method
- [ ] Test with expired tokens

### Phase 4: Testing & Monitoring (1 week)
- [ ] End-to-end testing: token refresh flow
- [ ] Load testing: 100+ concurrent refresh requests
- [ ] Security testing: token replay attacks
- [ ] Monitor error rates in production
- [ ] User acceptance testing

### Phase 5: Documentation & Cleanup (0.5 weeks)
- [ ] Update API documentation
- [ ] Add token refresh flow diagram
- [ ] Document error codes and handling
- [ ] Clean up temporary debug logs

**Total Timeline**: ~5 weeks

---

## Technical Architecture

### Database Schema

```sql
CREATE TABLE tokens (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  access_token VARCHAR(500) NOT NULL,
  refresh_token VARCHAR(500) NOT NULL UNIQUE,
  access_token_expires_at TIMESTAMP NOT NULL,
  refresh_token_expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  revoked_at TIMESTAMP NULL DEFAULT NULL,

  INDEX idx_user_id (user_id),
  INDEX idx_access_token (access_token),
  INDEX idx_refresh_token (refresh_token),
  INDEX idx_expires_at (access_token_expires_at, refresh_token_expires_at),

  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### API Specification

**Endpoint**: `POST /v0.1/auth/reissue`

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Success Response (200 OK)**:
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "access_token_expired_at": 1699999999,
  "refresh_token_expired_at": 1702591999
}
```

**Error Responses**:

```json
// 401 Unauthorized (refresh token expired)
{
  "error": "refresh_token_expired",
  "message": "Refresh token has expired. Please log in again."
}

// 401 Unauthorized (invalid refresh token)
{
  "error": "invalid_refresh_token",
  "message": "Invalid or revoked refresh token."
}

// 400 Bad Request
{
  "error": "missing_fields",
  "message": "access_token and refresh_token are required."
}
```

### Sequence Diagram

```
User -> App: Make API request
App -> Backend: API call with access_token
Backend -> Backend: Verify access_token
Backend --> App: 401 Unauthorized (token expired)

App -> App: Detect 401 error
App -> Backend: POST /v0.1/auth/reissue
Backend -> Backend: Verify refresh_token
Backend -> Database: Check refresh_token exists
Database --> Backend: Token found, not revoked
Backend -> Backend: Generate new tokens
Backend -> Database: Revoke old refresh_token
Backend -> Database: Store new tokens
Backend --> App: 200 OK (new tokens)

App -> App: Update stored tokens
App -> Backend: Retry original API call
Backend --> App: 200 OK (success)
App --> User: Display result
```

### Android Token Refresh Flow

```kotlin
class TokenRefreshInterceptor @Inject constructor(
    private val authLocalDataSource: AuthLocalDataSource,
    private val authApiService: AuthApiService
) : Interceptor {

    private val refreshLock = Mutex()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // If 401 and not already refreshing
        if (response.code == 401 && !request.url.encodedPath.contains("reissue")) {
            refreshLock.withLock {
                // Get stored tokens
                val accessToken = authLocalDataSource.getAccessToken()
                val refreshToken = authLocalDataSource.getRefreshToken()

                if (refreshToken != null) {
                    try {
                        // Call reissue endpoint
                        val newTokens = authApiService.reissueToken(
                            ReissueRequest(accessToken, refreshToken)
                        )

                        // Update stored tokens
                        authLocalDataSource.updateTokens(
                            newTokens.accessToken,
                            newTokens.refreshToken,
                            newTokens.accessTokenExpiredAt,
                            newTokens.refreshTokenExpiredAt
                        )

                        // Retry original request with new token
                        val newRequest = request.newBuilder()
                            .header("tkn", newTokens.accessToken)
                            .build()

                        return chain.proceed(newRequest)

                    } catch (e: Exception) {
                        // Refresh failed - clear tokens and logout
                        authLocalDataSource.clearTokens()
                        // Show session expired dialog
                        // Navigate to login
                    }
                }
            }
        }

        return response
    }
}
```

---

## Risk Assessment

### High Risk
1. **Concurrent Refresh**: Multiple 401s triggering parallel refresh attempts
   - **Mitigation**: Use mutex/lock to ensure single refresh operation

2. **Token Replay Attack**: Stolen refresh token used before revocation
   - **Mitigation**: Immediate revocation upon successful refresh, short refresh window

### Medium Risk
1. **Database Performance**: Token table grows unbounded
   - **Mitigation**: Background cleanup job, indexed queries, TTL-based deletion

2. **Clock Skew**: Client/server time mismatch causes premature expiration
   - **Mitigation**: Use server-issued expiration timestamps, 5-minute grace period

### Low Risk
1. **DataStore Encryption**: Tokens stored in plaintext on device
   - **Mitigation**: DataStore is already encrypted by Android OS, acceptable for MVP

---

## Testing Strategy

### Unit Tests (Backend)
- Token generation and storage
- Token validation logic
- Refresh token rotation
- Token revocation

### Unit Tests (Frontend)
- Token storage/retrieval
- Expiration time calculation
- Token refresh interceptor logic

### Integration Tests
- Full login → API call → token refresh → API retry flow
- Concurrent 401 handling
- Refresh token expiration handling

### E2E Tests
- Login → use app for 24 hours → automatic refresh
- Login → use app for 30 days → session expired dialog
- Login → turn off network → token refresh failure → error handling

### Performance Tests
- 100 concurrent refresh requests
- Token refresh latency under load
- Database query performance with 1M tokens

### Security Tests
- Attempt to reuse revoked refresh token
- Attempt to forge refresh token
- Attempt concurrent refresh with same token

---

## Monitoring & Alerts

### Metrics to Track
1. Token refresh success rate (target: >99%)
2. Token refresh latency (P50, P95, P99)
3. 401 error rate (should decrease post-deployment)
4. Session expiry rate
5. Refresh token rotation failures

### Alerts
1. Token refresh success rate <95% (warning)
2. Token refresh latency >1s P95 (warning)
3. Refresh token rotation failures >10/min (critical)
4. Database connection pool exhaustion (critical)

### Dashboards
- Real-time token refresh metrics
- Historical session duration
- Error rate trends
- Token storage growth

---

## Rollout Plan

### Phase 1: Internal Testing (1 week)
- Deploy to staging environment
- Test with internal team
- Monitor metrics
- Fix critical bugs

### Phase 2: Beta Release (1 week)
- Release to 10% of users
- Monitor error rates
- Collect user feedback
- Adjust token expiration times if needed

### Phase 3: Full Rollout (1 week)
- Release to 100% of users
- Monitor closely for 48 hours
- Document learnings
- Plan future enhancements

### Rollback Plan
- If refresh success rate <90%: rollback immediately
- If critical bugs found: disable reissue endpoint, force re-login
- Keep legacy authentication working during transition

---

## Future Enhancements

1. **Multi-Device Session Management**: View and revoke tokens from other devices
2. **Biometric Re-authentication**: Use fingerprint/face for token refresh
3. **Token Encryption**: Store encrypted tokens in database
4. **Advanced Monitoring**: ML-based anomaly detection for token patterns
5. **Token Versioning**: Support multiple token formats for gradual migration
6. **Device Binding**: Bind tokens to device ID for additional security

---

## Appendix

### Related Documentation
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [Android Secure Storage](https://developer.android.com/training/articles/keystore)
- [OWASP Token Handling](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)

### Team Contacts
- Backend Lead: [Name]
- Mobile Lead: [Name]
- Security Engineer: [Name]
- Product Manager: [Name]

### Open Questions
1. Should we implement automatic background token refresh?
2. What should be the maximum number of active sessions per user?
3. Should we notify users when logging in from a new device?

---

**Document Version**: 1.0
**Last Updated**: 2025-11-11
**Status**: Ready for Review
