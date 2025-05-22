package com.example.kai_content.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
/**
 * Data class untuk permintaan umpan balik.
 *
 * @param rating Nilai rating untuk konten (nullable).
 * @param review Teks ulasan untuk konten (nullable).
 * @see FeedbackResponse
 */
data class FeedbackRequest(
    val rating: Float?,
    val review: String?
)

/**
 * Data class untuk respons pengiriman umpan balik.
 *
 * @param message Pesan dari server.
 * @param feedback Objek umpan balik yang dikirimkan.
 */
data class FeedbackResponse(
    val message: String,
    val feedback: Feedback
)

/**
 * Data class untuk objek umpan balik.
 *
 * @param id ID umpan balik.
 * @param user_id ID pengguna yang memberikan umpan balik.
 * @param rating Nilai rating untuk konten (nullable).
 * @param review Teks ulasan untuk konten (nullable).
 * @param created_at Tanggal dan waktu pembuatan umpan balik.
 * @param updated_at Tanggal dan waktu pembaruan umpan balik.
 */
data class Feedback(
    val id: Int,
    val user_id: Int,
    val rating: Float?,
    val review: String?,
    val created_at: String,
    val updated_at: String
)

/**
 * Data class untuk respons pemeriksaan umpan balik.
 *
 * @param has_feedback Menunjukkan apakah pengguna sudah memberikan umpan balik.
 * @param feedback Objek umpan balik yang ada (nullable).
 * @param message Pesan dari server (nullable).
 */
data class CheckFeedbackResponse(
    val has_feedback: Boolean,
    val feedback: Feedback?,
    val message: String?
)

/**
 * API untuk mengelola umpan balik dari pengguna.
 *
 * @see FeedbackRequest
 * @see FeedbackResponse
 * @see CheckFeedbackResponse
 */
interface FeedbackApi {
    /**
     * Mengirimkan umpan balik dari pengguna.
     *
     * @param token Token otorisasi untuk autentikasi pengguna.
     * @param request Objek yang berisi data umpan balik.
     * @return Respons dari server yang berisi informasi tentang umpan balik yang dikirimkan.
     * @see FeedbackResponse
     */
    @Headers("Accept: application/json")
    @POST("api/feedbacks")
    fun submitFeedback(
        @Header("Authorization") token: String,
        @Body request: FeedbackRequest
    ): Call<FeedbackResponse>

    /**
     * Memeriksa apakah pengguna sudah memberikan umpan balik.
     *
     * @param token Token otorisasi untuk autentikasi pengguna.
     * @return Respons dari server yang berisi informasi tentang status umpan balik pengguna.
     * @see CheckFeedbackResponse
     */
    @Headers("Accept: application/json")
    @GET("api/feedbacks/check")
    fun checkUserFeedback(
        @Header("Authorization") token: String
    ): Call<CheckFeedbackResponse>
}
