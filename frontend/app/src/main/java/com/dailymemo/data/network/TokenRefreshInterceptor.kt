package com.dailymemo.data.network

import android.util.Log
import com.dailymemo.data.datasources.local.AuthLocalDataSource
import com.dailymemo.data.datasources.remote.api.AuthApiService
import com.dailymemo.data.models.request.ReissueTokenRequest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * OkHttp interceptor that handles automatic token refresh on 401 errors.
 *
 * Features:
 * - Detects 401 Unauthorized responses
 * - Mutex-based concurrency control to prevent multiple concurrent refresh attempts
 * - Automatically refreshes access token using refresh token
 * - Retries the original request once with new token
 * - Clears tokens and triggers navigation on refresh failure
 */
class TokenRefreshInterceptor @Inject constructor(
    private val authLocalDataSource: AuthLocalDataSource,
    private val authApiServiceProvider: dagger.Lazy<AuthApiService>,
    sessionExpiredCallback: SessionExpiredCallback?
) : Interceptor {

    companion object {
        private const val TAG = "TokenRefreshInterceptor"
        private const val HEADER_AUTH = "tkn"
        private const val REISSUE_ENDPOINT = "/v0.1/auth/reissue"
    }

    private val refreshMutex = Mutex()
    private var isRefreshing = false
    private var sessionExpiredCallback: SessionExpiredCallback? = sessionExpiredCallback

    /**
     * Set or update the session expired callback
     * Used by MainActivity to register itself as the callback handler
     */
    fun setSessionExpiredCallback(callback: SessionExpiredCallback?) {
        this.sessionExpiredCallback = callback
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip interceptor for auth endpoints and reissue endpoint
        if (originalRequest.url.encodedPath.contains("/auth/")) {
            Log.d(TAG, "Skipping token refresh for auth endpoint: ${originalRequest.url}")
            return chain.proceed(originalRequest)
        }

        // Proceed with original request
        val originalResponse = chain.proceed(originalRequest)

        // Check if response is 401 Unauthorized
        if (originalResponse.code == 401) {
            Log.w(TAG, "Received 401 for: ${originalRequest.url}")
            originalResponse.close()

            // Attempt to refresh token
            val refreshSuccess = runBlocking {
                refreshMutex.withLock {
                    attemptTokenRefresh()
                }
            }

            if (refreshSuccess) {
                Log.d(TAG, "Token refresh successful, retrying request")
                // Retry the original request with new token
                val newAccessToken = authLocalDataSource.getAccessTokenSync()
                if (newAccessToken != null) {
                    val newRequest = originalRequest.newBuilder()
                        .header(HEADER_AUTH, newAccessToken)
                        .build()
                    return chain.proceed(newRequest)
                }
            }

            Log.e(TAG, "Token refresh failed, session expired")
            // Clear tokens and trigger navigation on failure
            runBlocking {
                authLocalDataSource.clearTokens()
            }
            sessionExpiredCallback?.onSessionExpired()
        }

        return originalResponse
    }

    /**
     * Attempts to refresh the access token using the refresh token.
     * This method is protected by a Mutex to prevent concurrent refresh attempts.
     *
     * @return true if refresh was successful, false otherwise
     */
    private suspend fun attemptTokenRefresh(): Boolean {
        return try {
            Log.d(TAG, "Attempting token refresh")

            val accessToken = authLocalDataSource.getAccessToken()
            val refreshToken = authLocalDataSource.getRefreshToken()

            if (accessToken == null || refreshToken == null) {
                Log.e(TAG, "No tokens available for refresh")
                return false
            }

            // Lazy inject AuthApiService to avoid circular dependency
            val authApiService = authApiServiceProvider.get()
            val request = ReissueTokenRequest(
                accessToken = accessToken,
                refreshToken = refreshToken
            )

            val response = authApiService.reissueToken(request)

            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!
                Log.d(TAG, "Token refresh API successful")

                // Update stored tokens
                authLocalDataSource.updateTokens(
                    accessToken = tokenResponse.accessToken,
                    refreshToken = tokenResponse.refreshToken,
                    accessTokenExpiresAt = tokenResponse.accessTokenExpiredAt,
                    refreshTokenExpiresAt = tokenResponse.refreshTokenExpiredAt
                )

                Log.d(TAG, "Tokens updated successfully")
                true
            } else {
                Log.e(TAG, "Token refresh failed: ${response.code()} - ${response.message()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during token refresh", e)
            false
        }
    }

    /**
     * Callback interface for handling session expiration.
     * Implement this to navigate to login screen when token refresh fails.
     */
    interface SessionExpiredCallback {
        fun onSessionExpired()
    }
}
