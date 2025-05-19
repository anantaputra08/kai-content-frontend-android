package com.example.kai_content.api

import com.example.kai_content.models.complaint.ComplaintCategory
import com.example.kai_content.models.complaint.ComplaintRequest
import com.example.kai_content.models.complaint.ComplaintResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path

/**
 * API untuk mengelola pengaduan.
 *
 * @see ComplaintResponse
 * @see ComplaintRequest
 */
interface ComplaintApi {
     /**
      * Mengambil daftar pengaduan pengguna.
      *
      * @param token Token otorisasi untuk autentikasi pengguna.
      * @return Response yang berisi daftar pengaduan pengguna.
      * @see ComplaintResponse
      */
     @Headers("Accept: application/json")
     @GET("api/complaints/my-complaints")
     suspend fun getComplaints(
          @Header("Authorization") token: String
     ): Response<List<ComplaintResponse>>

     /**
      * Mengambil daftar pengaduan operator.
      *
      * @param token Token otorisasi untuk autentikasi pengguna.
      * @return Response yang berisi daftar pengaduan operator.
      * @see ComplaintResponse
      */
     @Headers("Accept: application/json")
     @GET("api/complaints")
     suspend fun getComplaintsOperator(
          @Header("Authorization") token: String
     ): Response<List<ComplaintResponse>>

     /**
      * Mengambil daftar kategori pengaduan.
      *
      * @param token Token otorisasi untuk autentikasi pengguna.
      * @return Response yang berisi daftar kategori pengaduan.
      * @see ComplaintCategory
      */
     @Headers("Accept: application/json")
     @GET("api/complaints/categories")
     suspend fun getCategories(
          @Header("Authorization") token: String
     ): Response<List<ComplaintCategory>>

     /**
      * Memposting pengaduan baru.
      *
      * @param token Token otorisasi untuk autentikasi pengguna.
      * @param categoryComplaintId ID kategori pengaduan.
      * @param description Deskripsi pengaduan.
      * @param attachment Lampiran pengaduan.
      * @param assignedTo ID pengguna yang ditugaskan untuk menangani pengaduan.
      * @param resolutionDate Tanggal penyelesaian pengaduan.
      * @param resolutionNotes Catatan penyelesaian pengaduan.
      * @return Response yang berisi hasil pengaduan.
      * @see ComplaintResponse
      */
     @Multipart
     @Headers("Accept: application/json")
     @POST("api/complaints")
     suspend fun createComplaint(
          @Header("Authorization") token: String,
          @Part("category_complaint_id") categoryComplaintId: RequestBody?,
          @Part("description") description: RequestBody?,
          @Part attachment: MultipartBody.Part?,
          @Part("assigned_to") assignedTo: RequestBody?,
          @Part("resolution_date") resolutionDate: RequestBody?,
          @Part("resolution_notes") resolutionNotes: RequestBody?
     ): Response<ComplaintResponse>

     /**
      * Mengupdate pengaduan yang sudah ada.
      *
      * @param token Token otorisasi untuk autentikasi pengguna.
      * @param id ID pengaduan yang ingin diupdate.
      * @param request Objek yang berisi data pengaduan yang diperbarui.
      * @return Response yang berisi hasil pembaruan pengaduan.
      * @see ComplaintRequest
      * @see ComplaintResponse
      */
     @PUT("api/complaints/{id}")
     suspend fun updateComplaint(
          @Header("Authorization") token: String,
          @Path("id") id: Int,
          @Body request: ComplaintRequest
     ): Response<ComplaintResponse>
}
