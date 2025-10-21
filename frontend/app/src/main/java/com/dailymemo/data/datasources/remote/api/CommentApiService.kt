package com.dailymemo.data.datasources.remote.api

import com.dailymemo.data.models.request.CreateCommentRequestDto
import com.dailymemo.data.models.response.CommentListResponseDto
import com.dailymemo.data.models.response.CommentResponseDto
import retrofit2.Response
import retrofit2.http.*

interface CommentApiService {

    @GET("/v0.1/memo/{memo_id}/comments")
    suspend fun getComments(
        @Path("memo_id") memoId: Long
    ): Response<CommentListResponseDto>

    @POST("/v0.1/memo/{memo_id}/comments")
    suspend fun createComment(
        @Path("memo_id") memoId: Long,
        @Body request: CreateCommentRequestDto
    ): Response<CommentResponseDto>

    @DELETE("/v0.1/comments/{comment_id}")
    suspend fun deleteComment(
        @Path("comment_id") commentId: Long
    ): Response<Unit>
}
