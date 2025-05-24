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

/**
 * API untuk mengelola reaksi suka/tidak suka pada konten.
 *
 * @see LikeDislikeResponse
 */
interface LikeDislikeApi {
    /**
     * Mengambil daftar konten yang ditandai sebagai suka/tidak suka.
     *
     * @param token Token otorisasi untuk autentikasi pengguna.
     * @return Response yang berisi daftar konten suka/tidak suka.
     * @see LikeDislikeResponse
     */
    @Headers("Accept: application/json")
    @GET("api/like-dislike/check/{id}")
    suspend fun checkLikeDislike(
        @Header("Authorization") token: String,
        @Path("id") contentId: String
    ): Response<LikeDislikeResponse>


    /**
     * Mengambil daftar konten yang ditandai sebagai suka/tidak suka.
     *
     * @param token Token otorisasi untuk autentikasi pengguna.
     * @return Response yang berisi daftar konten suka/tidak suka.
     * @see Map
     */
    @Headers("Accept: application/json")
    @POST("api/like-dislike/{id}")
    suspend fun setReaction(
        @Header("Authorization") token: String,
        @Path("id") contentId: String,
        @Body requestBody: ReactionRequest
    ): Response<Map<String, Any>>
}


/**
 * Data class untuk permintaan reaksi (suka/tidak suka).
 *
 * @param reaction_type Jenis reaksi (suka/tidak suka).
 * @param action Aksi yang dilakukan (true/false).
 * @param content_id ID konten yang diberi reaksi.
 */
data class ReactionRequest(
    val reaction_type: String,
    val action: Boolean,
    val content_id: String
)
