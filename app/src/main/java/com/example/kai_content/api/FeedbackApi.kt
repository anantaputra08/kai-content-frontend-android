package com.example.kai_content.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

data class FeedbackRequest(
    val rating: Float?, // Nullable sesuai validasi backend
    val review: String? // Nullable sesuai validasi backend
)

data class FeedbackResponse(
    val message: String,
    val feedback: Feedback
)

data class Feedback(
    val id: Int,
    val user_id: Int,
    val rating: Float?,
    val review: String?,
    val created_at: String,
    val updated_at: String
)

data class CheckFeedbackResponse(
    val has_feedback: Boolean,
    val feedback: Feedback?,
    val message: String?
)

interface FeedbackApi {
    @Headers("Accept: application/json")
    @POST("api/feedbacks")
    fun submitFeedback(
        @Header("Authorization") token: String,
        @Body request: FeedbackRequest
    ): Call<FeedbackResponse>

    @Headers("Accept: application/json")
    @GET("api/feedbacks/check")
    fun checkUserFeedback(
        @Header("Authorization") token: String
    ): Call<CheckFeedbackResponse>
}
