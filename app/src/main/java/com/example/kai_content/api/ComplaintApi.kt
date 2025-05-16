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

interface ComplaintApi {
     /*
     * Endpoint untuk mendapatkan daftar pengaduan
     */
     @Headers("Accept: application/json")
     @GET("api/complaints/my-complaints")
     suspend fun getComplaints(@Header("Authorization") token: String): Response<List<ComplaintResponse>>

     /*
     * Endpoint untuk mendapatkan daftar pengaduan untuk operator
     */
     @Headers("Accept: application/json")
     @GET("api/complaints")
     suspend fun getComplaintsOperator(@Header("Authorization") token: String): Response<List<ComplaintResponse>>

     /*
        * Endpoint untuk mendapatkan kategori pengaduan
      */
     @Headers("Accept: application/json")
     @GET("api/complaints/categories")
     suspend fun getCategories(@Header("Authorization") token: String): Response<List<ComplaintCategory>>

     /*
     * Endpoint untuk mendapatkan detail pengaduan
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

     /*
     * Endpoint untuk mengupdate pengaduan
     */
     @PUT("api/complaints/{id}")
     suspend fun updateComplaint(
          @Header("Authorization") token: String,
          @Path("id") id: Int,
          @Body request: ComplaintRequest
     ): Response<ComplaintResponse>
}
