package com.example.kai_content.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface ReviewApi {
    /**
     * Mengirimkan ulasan untuk konten tertentu.
     *
     * @param token Token otorisasi untuk autentikasi pengguna.
     * @param request Data ulasan yang akan dikirimkan.
     * @return Response yang berisi hasil pengiriman ulasan.
     */
    @Headers("Accept: application/json")
    @POST("api/reviews")
    suspend fun submitReview(
        @Header("Authorization") token: String,
        @Body request: ReviewRequest
    ): ReviewResponse

    /**
     * Memeriksa apakah pengguna sudah memberikan ulasan untuk konten tertentu.
     *
     * @param token Token otorisasi untuk autentikasi pengguna.
     * @param contentId ID konten yang ingin diperiksa.
     * @return Response yang berisi informasi tentang status ulasan pengguna.
     */
    @Headers("Accept: application/json")
    @GET("api/reviews/check/{content_id}")
    suspend fun checkUserReview(
        @Header("Authorization") token: String,
        @Path("content_id") contentId: String,
    ): CheckReviewResponse
}

/**
 * Data class untuk permintaan ulasan.
 *
 * @param content_id ID konten yang akan diulas.
 * @param rating Nilai rating untuk konten (nullable).
 * @param review Teks ulasan untuk konten (nullable).
 */
data class ReviewRequest(
    val content_id: Int,
    val rating: Float?, // Nullable sesuai validasi backend
    val review: String? // Nullable sesuai validasi backend
)

/**
 * Data class untuk respons pengiriman ulasan.
 *
 * @param message Pesan dari server.
 * @param review Objek ulasan yang dikirimkan.
 */
data class ReviewResponse(
    val message: String,
    val review: Review
)

/**
 * Data class untuk objek ulasan.
 *
 * @param id ID ulasan.
 * @param user_id ID pengguna yang memberikan ulasan.
 * @param content_id ID konten yang diulas.
 * @param rating Nilai rating untuk konten (nullable).
 * @param review Teks ulasan untuk konten (nullable).
 * @param created_at Tanggal dan waktu pembuatan ulasan.
 * @param updated_at Tanggal dan waktu pembaruan ulasan.
 */
data class Review(
    val id: Int,
    val user_id: Int,
    val content_id: Int,
    val rating: Float?,
    val review: String?,
    val created_at: String,
    val updated_at: String
)

/**
 * Data class untuk respons pemeriksaan ulasan.
 *
 * @param has_review Menunjukkan apakah pengguna sudah memberikan ulasan.
 * @param review Objek ulasan yang ada (nullable).
 * @param message Pesan dari server (nullable).
 */
data class CheckReviewResponse(
    val has_review: Boolean,
    val review: Review?,
    val message: String?
)
