package com.example.kai_content.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface ReviewApi {
    @Headers("Accept: application/json")
    @POST("api/reviews")
    suspend fun submitReview(
        @Header("Authorization") token: String,
        @Body request: ReviewRequest
    ): ReviewResponse

    @Headers("Accept: application/json")
    @GET("api/reviews/check/{content_id}")
    suspend fun checkUserReview(
        @Header("Authorization") token: String,
        @Path("content_id") contentId: String,
    ): CheckReviewResponse
}

data class ReviewRequest(
    val content_id: Int,
    val rating: Float?, // Nullable sesuai validasi backend
    val review: String? // Nullable sesuai validasi backend
)

data class ReviewResponse(
    val message: String,
    val review: Review
)

data class Review(
    val id: Int,
    val user_id: Int,
    val content_id: Int,
    val rating: Float?,
    val review: String?,
    val created_at: String,
    val updated_at: String
)

data class CheckReviewResponse(
    val has_review: Boolean,
    val review: Review?,
    val message: String?
)
