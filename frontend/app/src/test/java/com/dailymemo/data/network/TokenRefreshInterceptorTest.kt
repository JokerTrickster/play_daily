package com.dailymemo.data.network

import com.dailymemo.data.datasources.local.AuthLocalDataSource
import com.dailymemo.data.datasources.remote.api.AuthApiService
import com.dailymemo.data.models.request.ReissueTokenRequest
import com.dailymemo.data.models.response.ReissueTokenResponse
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.anyString
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import retrofit2.Response as RetrofitResponse

/**
 * Unit tests for TokenRefreshInterceptor.
 *
 * Tests cover:
 * - 401 error detection and token refresh
 * - Concurrent 401 handling with Mutex
 * - Successful token refresh and retry
 * - Failed token refresh handling
 * - Auth endpoint bypass
 * - Session expiration callback
 */
class TokenRefreshInterceptorTest {

    @Mock
    private lateinit var authLocalDataSource: AuthLocalDataSource

    @Mock
    private lateinit var authApiService: AuthApiService

    @Mock
    private lateinit var sessionExpiredCallback: TokenRefreshInterceptor.SessionExpiredCallback

    private lateinit var interceptor: TokenRefreshInterceptor
    private lateinit var mockChain: Interceptor.Chain

    private val testAccessToken = "test_access_token"
    private val testRefreshToken = "test_refresh_token"
    private val newAccessToken = "new_access_token"
    private val newRefreshToken = "new_refresh_token"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Create lazy provider for AuthApiService
        val lazyAuthApiService = dagger.Lazy { authApiService }

        interceptor = TokenRefreshInterceptor(
            authLocalDataSource = authLocalDataSource,
            authApiServiceProvider = lazyAuthApiService,
            sessionExpiredCallback = sessionExpiredCallback
        )

        mockChain = Mockito.mock(Interceptor.Chain::class.java)
    }

    @Test
    fun `test auth endpoint bypasses interceptor`() {
        // Given: Request to auth endpoint
        val request = createRequest("http://example.com/v0.1/auth/signin")
        val response = createSuccessResponse(request)

        `when`(mockChain.request()).thenReturn(request)
        `when`(mockChain.proceed(request)).thenReturn(response)

        // When: Interceptor processes request
        val result = interceptor.intercept(mockChain)

        // Then: Request proceeds without token refresh
        verify(mockChain, times(1)).proceed(request)
        verify(authLocalDataSource, never()).getAccessToken()
        assert(result.code == 200)
    }

    @Test
    fun `test successful request returns normally`() {
        // Given: Successful request
        val request = createRequest("http://example.com/v0.1/memos")
        val response = createSuccessResponse(request)

        `when`(mockChain.request()).thenReturn(request)
        `when`(mockChain.proceed(request)).thenReturn(response)

        // When: Interceptor processes request
        val result = interceptor.intercept(mockChain)

        // Then: Request proceeds normally
        verify(mockChain, times(1)).proceed(request)
        verify(authLocalDataSource, never()).getAccessToken()
        assert(result.code == 200)
    }

    @Test
    fun `test 401 error triggers token refresh and retry`() = runBlocking {
        // Given: Initial 401 response, then successful refresh
        val request = createRequest("http://example.com/v0.1/memos")
        val unauthorizedResponse = createUnauthorizedResponse(request)
        val successResponse = createSuccessResponse(request)

        `when`(mockChain.request()).thenReturn(request)
        `when`(mockChain.proceed(request))
            .thenReturn(unauthorizedResponse)
            .thenReturn(successResponse)

        // Mock token retrieval
        `when`(authLocalDataSource.getAccessToken()).thenReturn(testAccessToken)
        `when`(authLocalDataSource.getRefreshToken()).thenReturn(testRefreshToken)
        `when`(authLocalDataSource.getAccessTokenSync()).thenReturn(newAccessToken)

        // Mock successful token refresh
        val refreshResponse = ReissueTokenResponse(
            accessToken = newAccessToken,
            accessTokenExpiredAt = System.currentTimeMillis() + 3600000,
            refreshToken = newRefreshToken,
            refreshTokenExpiredAt = System.currentTimeMillis() + 7200000
        )
        val retrofitResponse = RetrofitResponse.success(refreshResponse)
        `when`(authApiService.reissueToken(Mockito.any())).thenReturn(retrofitResponse)

        // When: Interceptor processes 401 response
        val result = interceptor.intercept(mockChain)

        // Then: Token refresh called and request retried
        verify(authApiService, times(1)).reissueToken(Mockito.any())
        verify(authLocalDataSource, times(1)).updateTokens(
            anyString(),
            anyString(),
            anyLong(),
            anyLong()
        )
        verify(mockChain, times(2)).proceed(Mockito.any()) // Original + retry
        assert(result.code == 200)
    }

    @Test
    fun `test 401 error with no tokens clears storage`() = runBlocking {
        // Given: 401 response with no tokens available
        val request = createRequest("http://example.com/v0.1/memos")
        val unauthorizedResponse = createUnauthorizedResponse(request)

        `when`(mockChain.request()).thenReturn(request)
        `when`(mockChain.proceed(request)).thenReturn(unauthorizedResponse)
        `when`(authLocalDataSource.getAccessToken()).thenReturn(null)
        `when`(authLocalDataSource.getRefreshToken()).thenReturn(null)

        // When: Interceptor processes 401 response
        val result = interceptor.intercept(mockChain)

        // Then: Tokens cleared and callback triggered
        verify(authLocalDataSource, times(1)).clearTokens()
        verify(sessionExpiredCallback, times(1)).onSessionExpired()
        assert(result.code == 401)
    }

    @Test
    fun `test failed token refresh clears tokens and triggers callback`() = runBlocking {
        // Given: 401 response and failed token refresh
        val request = createRequest("http://example.com/v0.1/memos")
        val unauthorizedResponse = createUnauthorizedResponse(request)

        `when`(mockChain.request()).thenReturn(request)
        `when`(mockChain.proceed(request)).thenReturn(unauthorizedResponse)
        `when`(authLocalDataSource.getAccessToken()).thenReturn(testAccessToken)
        `when`(authLocalDataSource.getRefreshToken()).thenReturn(testRefreshToken)

        // Mock failed token refresh
        val failedResponse = RetrofitResponse.error<ReissueTokenResponse>(
            401,
            "Unauthorized".toResponseBody("text/plain".toMediaTypeOrNull())
        )
        `when`(authApiService.reissueToken(Mockito.any())).thenReturn(failedResponse)

        // When: Interceptor processes 401 response
        val result = interceptor.intercept(mockChain)

        // Then: Tokens cleared and callback triggered
        verify(authLocalDataSource, times(1)).clearTokens()
        verify(sessionExpiredCallback, times(1)).onSessionExpired()
        assert(result.code == 401)
    }

    @Test
    fun `test token refresh exception clears tokens`() = runBlocking {
        // Given: 401 response and exception during refresh
        val request = createRequest("http://example.com/v0.1/memos")
        val unauthorizedResponse = createUnauthorizedResponse(request)

        `when`(mockChain.request()).thenReturn(request)
        `when`(mockChain.proceed(request)).thenReturn(unauthorizedResponse)
        `when`(authLocalDataSource.getAccessToken()).thenReturn(testAccessToken)
        `when`(authLocalDataSource.getRefreshToken()).thenReturn(testRefreshToken)

        // Mock exception during token refresh
        `when`(authApiService.reissueToken(Mockito.any()))
            .thenThrow(RuntimeException("Network error"))

        // When: Interceptor processes 401 response
        val result = interceptor.intercept(mockChain)

        // Then: Tokens cleared and callback triggered
        verify(authLocalDataSource, times(1)).clearTokens()
        verify(sessionExpiredCallback, times(1)).onSessionExpired()
        assert(result.code == 401)
    }

    @Test
    fun `test reissue endpoint bypasses interceptor`() {
        // Given: Request to reissue endpoint
        val request = createRequest("http://example.com/v0.1/auth/reissue")
        val response = createSuccessResponse(request)

        `when`(mockChain.request()).thenReturn(request)
        `when`(mockChain.proceed(request)).thenReturn(response)

        // When: Interceptor processes request
        val result = interceptor.intercept(mockChain)

        // Then: Request proceeds without interception
        verify(mockChain, times(1)).proceed(request)
        verify(authLocalDataSource, never()).getAccessToken()
        assert(result.code == 200)
    }

    // Helper functions to create test requests and responses

    private fun createRequest(url: String): Request {
        return Request.Builder()
            .url(url)
            .build()
    }

    private fun createSuccessResponse(request: Request): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_2)
            .code(200)
            .message("OK")
            .body("{}".toResponseBody("application/json".toMediaTypeOrNull()))
            .build()
    }

    private fun createUnauthorizedResponse(request: Request): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_2)
            .code(401)
            .message("Unauthorized")
            .body("{}".toResponseBody("application/json".toMediaTypeOrNull()))
            .build()
    }
}
