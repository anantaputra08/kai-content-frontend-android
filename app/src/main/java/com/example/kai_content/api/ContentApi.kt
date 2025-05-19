package com.example.kai_content.api

import com.example.kai_content.models.content.CategoriesResponse
import com.example.kai_content.models.content.Content
import com.example.kai_content.models.content.ContentResponse
import com.example.kai_content.models.content.ContentsResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API untuk mengelola konten.
 *
 * @see Content
 * @see ContentResponse
 * @see ContentsResponse
 */
interface ContentApi {
    /**
     * Mengambil daftar konten.
     *
     * @param token Token otorisasi untuk autentikasi pengguna.
     * @return Response yang berisi daftar konten.
     * @see Content
     */
    @Headers("Accept: application/json")
    @GET("api/contents")
    fun getContents(@Header("Authorization") token: String): Call<List<Content>>

    /**
     * Mengambil konten berdasarkan ID.
     *
     * @param token Token otorisasi untuk autentikasi pengguna.
     * @param contentId ID konten yang ingin diambil.
     * @return Response yang berisi detail konten.
     * @see ContentResponse
     */
    @Headers("Accept: application/json")
    @GET("api/content/{id}")
    suspend fun getContent(
        @Header("Authorization") token: String,
        @Path("id") contentId: String
    ): Response<ContentResponse>

    /**
     * Mengambil konten terkait berdasarkan ID konten.
     *
     * @param token Token otorisasi untuk autentikasi pengguna.
     * @param contentId ID konten yang ingin diambil.
     * @return Response yang berisi daftar konten terkait.
     * @see ContentsResponse
     */
    @Headers("Accept: application/json")
    @GET("api/content/{id}/related")
    suspend fun getRelatedContents(
        @Header("Authorization") token: String,
        @Path("id") contentId: String
    ): Response<ContentsResponse>

    /**
     * Mengirim laporan waktu tonton untuk konten tertentu.
     *
     * @param token Token otorisasi untuk autentikasi pengguna.
     * @param contentId ID konten yang ingin dilaporkan.
     * @param watchTime Waktu tonton dalam detik.
     * @return Response yang berisi hasil laporan.
     * @see Map<String, Any>
     */
    @POST("api/content/{id}/watch-time")
    @FormUrlEncoded
    suspend fun reportWatchTime(
        @Header("Authorization") token: String,
        @Path("id") contentId: String,
        @Field("watch_time") watchTime: Int
    ): Response<Map<String, Any>>

    /**
     * Mengambil daftar konten berdasarkan search.
     *
     * @param token Token otorisasi untuk autentikasi pengguna.
     * @param title Judul konten yang ingin dicari.
     * @return Response yang berisi daftar konten yang sesuai dengan pencarian.
     * @see List
     * @see Content
     */
    @Headers("Accept: application/json")
    @GET("api/contents/search")
    fun searchContents(
        @Header("Authorization") token: String,
        @Query("title") title: String
    ): Call<List<Content>>

    /**
     * Mengambil detail konten berdasarkan ID.
     *
     * @param token Token otorisasi untuk autentikasi pengguna.
     * @param id ID konten yang ingin diambil.
     * @return Response yang berisi detail konten.
     * @see ContentResponse
     */
    @Headers("Accept: application/json")
    @GET("api/contents/details/{id}")
    suspend fun getContentById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ContentResponse>

    /**
     * Mengupdate konten berdasarkan ID.
     *
     * @param token Token otorisasi untuk autentikasi pengguna.
     * @param id ID konten yang ingin diupdate.
     * @param method Metode HTTP yang digunakan (PUT).
     * @param title Judul konten yang ingin diupdate.
     * @param description Deskripsi konten yang ingin diupdate.
     * @param status Status konten yang ingin diupdate.
     * @param categories Kategori konten yang ingin diupdate.
     * @param file File konten yang ingin diupdate (nullable).
     * @param thumbnail Thumbnail konten yang ingin diupdate (nullable).
     * @return Response yang berisi hasil update konten.
     *
     * @see Any
     */
    @Headers("Accept: application/json")
    @Multipart
    @POST("api/contents/{id}")
    suspend fun updateContent(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Part("_method") method: RequestBody,
        @Part("title") title: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("status") status: RequestBody?,
        @Part("categories") categories: RequestBody?,
        @Part file: MultipartBody.Part?,
        @Part thumbnail: MultipartBody.Part?
    ): Response<Any>

    /**
     * Mengambil semua kategori konten.
     *
     * @param token Token otorisasi untuk autentikasi pengguna.
     * @return Response yang berisi daftar kategori konten.
     * @see CategoriesResponse
     */
    @Headers("Accept: application/json")
    @GET("api/categories")
    suspend fun getAllCategories(
        @Header("Authorization") token: String
    ): Response<CategoriesResponse>

    /**
     * Membuat konten baru.
     *
     * @param token Token otorisasi untuk autentikasi pengguna.
     * @param title Judul konten yang ingin dibuat.
     * @param description Deskripsi konten yang ingin dibuat (nullable).
     * @param status Status konten yang ingin dibuat.
     * @param file File konten yang ingin dibuat.
     * @param thumbnail Thumbnail konten yang ingin dibuat (nullable).
     * @param categories Kategori konten yang ingin dibuat (nullable).
     * @return Response yang berisi hasil pembuatan konten.
     * @see ContentResponse
     */
    @Multipart
    @Headers("Accept: application/json")
    @POST("api/contents")
    suspend fun createContent(
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("status") status: RequestBody,
        @Part file: MultipartBody.Part,
        @Part thumbnail: MultipartBody.Part?,
        @Part("categories") categories: RequestBody?,
    ): Response<ContentResponse>
}
