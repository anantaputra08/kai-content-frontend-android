package com.example.kai_content.api

import com.example.kai_content.models.content.Content
import com.example.kai_content.models.favorite.FavoriteResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface FavoriteApi {

    /**
     * Mengambil daftar konten yang ditandai sebagai favorit.
     *
     * @param token Token otorisasi untuk autentikasi pengguna.
     * @return Response yang berisi daftar konten favorit.
     */
    @Headers("Accept: application/json")
    @GET("api/favorite")
    suspend fun getFavorites(
        @Header("Authorization") token: String
    ): FavoriteResponse

    /**
     * Mengambil daftar konten yang ditandai sebagai favorit.
     *
     * @param token Token otorisasi untuk autentikasi pengguna.
     * @return Response yang berisi daftar konten favorit.
     */
    @Headers("Accept: application/json")
    @POST("api/favorite/toggle/{id}")
    suspend fun toggleFavorite(
        @Header("Authorization") token: String,
        @Path("id") contentId: String
    ): Response<Map<String, Any>>

    /**
     * Cek apakah konten sudah ditandai sebagai favorit.
     *
     * @param token Token otorisasi untuk autentikasi pengguna.
     * @param contentId ID konten yang ingin diperiksa.
     */
    @Headers("Accept: application/json")
    @GET("api/favorite/check/{id}")
    suspend fun checkFavorite(
        @Header("Authorization") token: String,
        @Path("id") contentId: String
    ): Response<Map<String, Any>>
}
