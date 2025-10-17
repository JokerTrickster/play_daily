package com.dailymemo.data.datasources.remote.api

import com.dailymemo.data.models.request.SignInRequestDto
import com.dailymemo.data.models.request.SignUpRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("/v0.1/auth/signup")
    suspend fun signup(
        @Body request: SignUpRequestDto
    ): Response<Unit>

    @POST("/v0.1/auth/signin")
    suspend fun signIn(
        @Body request: SignInRequestDto
    ): Response<Unit>
}
