package com.example.kai_content.api

import com.example.kai_content.models.reaction.LikeDislikeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface LikeDislikeApi {
    @Headers("Accept: application/json")
    @GET("api/like-dislike/check/{id}")
    suspend fun checkLikeDislike(
        @Header("Authorization") token: String,
        @Path("id") contentId: String
    ): Response<LikeDislikeResponse>

    @Headers("Accept: application/json")
//    @FormUrlEncoded
    @POST("api/like-dislike/{id}")
    suspend fun setReaction(
        @Header("Authorization") token: String,
        @Path("id") contentId: String,
        @Body requestBody: ReactionRequest
    ): Response<Map<String, Any>>
}

data class ReactionRequest(
    val reaction_type: String,
    val action: Boolean,
    val content_id: String
)
