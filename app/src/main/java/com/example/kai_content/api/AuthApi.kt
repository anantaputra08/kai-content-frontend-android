package com.example.kai_content.api

import com.example.kai_content.models.login.LoginRequest
import com.example.kai_content.models.login.LoginResponse
import com.example.kai_content.models.register.RegisterRequest
import com.example.kai_content.models.register.RegisterResponse
import com.example.kai_content.models.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthApi {
    // Endpoint untuk login
    @POST("api/login")
    fun login(
        @Body loginRequest: LoginRequest)
    : Call<LoginResponse>

    // Endpoint untuk register
    @Headers("Accept: application/json")
    @POST("api/register")
    fun register(
        @Body registerRequest: RegisterRequest
    ): Call<RegisterResponse>

    // Endpoint untuk mendapatkan data pengguna
    @GET("api/profile")
    fun getUser(
        @Header("Authorization") token: String
    ): Call<UserResponse>

    // Endpoint untuk mengupdate data pengguna
    @PUT("api/profile")
    fun updateProfile(
        @Header("Authorization") token: String,
        @Body profileData: Map<String, String>
    ): Call<UserResponse>

    // Endpoint untuk mengganti password
    @PUT("api/profile/change-password")
    fun changePassword(
        @Header("Authorization") token: String,
        @Body passwordData: Map<String, String>
    ): Call<Void>

    // Endpoint untuk logout
    @POST("api/logout")
    fun logout(
        @Header("Authorization") token: String
    ): Call<Void>
}