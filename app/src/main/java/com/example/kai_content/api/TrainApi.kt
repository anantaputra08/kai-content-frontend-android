package com.example.kai_content.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET

interface TrainApi {
    /**
     * Mengambil daftar semua kereta yang tersedia dari server.
     * Endpoint: GET api/trains
     */
    @GET("api/trains")
    suspend fun getTrains(): Response<TrainResponse>
}

/**
 * Data class untuk merepresentasikan respons dari endpoint /api/trains.
 * Strukturnya harus cocok dengan JSON yang dikirim oleh server.
 */
data class TrainResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<Train>
)

/**
 * Data class untuk merepresentasikan satu objek Kereta.
 * Pastikan propertinya cocok dengan data JSON.
 */
data class Train(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("route")
    val route: String? // Opsional, bisa null jika tidak selalu ada
)
