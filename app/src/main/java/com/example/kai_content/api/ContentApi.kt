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

interface ContentApi {
    @Headers("Accept: application/json")
    @GET("api/contents")
    fun getContents(@Header("Authorization") token: String): Call<List<Content>>

//    @Headers("Accept: application/json")
//    @GET("api/contents/{id}")
//    fun getContent(
//        @Header("Authorization") token: String,
//        @Path("id") id: String): Call<Content>
    /**
     * Mengambil data konten berdasarkan ID
     */
    @Headers("Accept: application/json")
    @GET("api/content/{id}")
    suspend fun getContent(
        @Header("Authorization") token: String,
        @Path("id") contentId: String
    ): Response<ContentResponse>

    /**
     * Mengambil daftar konten berdasarkan kategori
     * @Query("category") adalah parameter query untuk kategori
     *
     * @Header("Authorization") adalah token otorisasi
     * @GET("api/contents") adalah endpoint untuk mengambil konten
     * @Response<ContentsResponse> adalah tipe data yang dikembalikan
     * @param token adalah token otorisasi
     * @param contentId adalah ID konten
     *
     * @return Response<ContentsResponse> adalah respons dari server dikelompokan dengan kategori
     */
    @Headers("Accept: application/json")
    @GET("api/content/{id}/related")
    suspend fun getRelatedContents(
        @Header("Authorization") token: String,
        @Path("id") contentId: String
    ): Response<ContentsResponse>

    /**
     * Melaporan durasi tonton untuk konten
     * @Field("watch_time") adalah durasi tonton dalam detik
     * @Path("id") adalah ID konten
     * @Header("Authorization") adalah token otorisasi
     * @POST("api/content/{id}/watch-time") adalah endpoint untuk melaporkan durasi tonton
     * @FormUrlEncoded digunakan untuk mengirim data dalam format URL-encoded
     * @Response<Map<String, Any>> adalah tipe data yang dikembalikan
     * @param token adalah token otorisasi
     * @param contentId adalah ID konten
     * @param watchTime adalah durasi tonton dalam detik
     */
    @POST("api/content/{id}/watch-time")
    @FormUrlEncoded
    suspend fun reportWatchTime(
        @Header("Authorization") token: String,
        @Path("id") contentId: String,
        @Field("watch_time") watchTime: Int
    ): Response<Map<String, Any>>

    /**
     * Mengirim reaksi untuk konten (like/dislike)
     */
//    @POST("api/content/{id}/reaction")
//    @FormUrlEncoded
//    suspend fun setReaction(
//        @Header("Authorization") token: String,
//        @Path("id") contentId: String,
//        @Field("reaction") reaction: String, // "like" atau "dislike"
//        @Field("value") value: Boolean // true untuk add, false untuk remove
//    ): Response<Map<String, Any>>

    @Headers("Accept: application/json")
    @GET("api/contents/search")
    fun searchContents(
        @Header("Authorization") token: String,
        @Query("title") title: String
    ): Call<List<Content>>

    @Headers("Accept: application/json")
    @GET("api/contents/details/{id}")
    suspend fun getContentById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ContentResponse>

    @Headers("Accept: application/json")
    @Multipart
    @POST("api/contents/{id}")  // Change to POST if your backend uses _method for method spoofing
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

    @Headers("Accept: application/json")
    @GET("api/categories")
    suspend fun getAllCategories(
        @Header("Authorization") token: String
    ): Response<CategoriesResponse>

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
//        @Part categoryIds: List<MultipartBody.Part> = emptyList()
        @Part("categories") categories: RequestBody?,
    ): Response<ContentResponse>


}
