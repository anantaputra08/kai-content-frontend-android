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

/**
 * API untuk mengelola otentikasi pengguna.
 *
 * @see LoginRequest
 * @see LoginResponse
 * @see RegisterRequest
 * @see RegisterResponse
 */
interface AuthApi {
    /**
     * Mengirim permintaan login.
     *
     * @param loginRequest Objek yang berisi informasi login pengguna.
     * @return Response yang berisi hasil login.
     * @see LoginResponse
     */
    @POST("api/login")
    fun login(
        @Body loginRequest: LoginRequest)
    : Call<LoginResponse>

    /**
     * Mengirim permintaan pendaftaran pengguna baru.
     *
     * @param registerRequest Objek yang berisi informasi pendaftaran pengguna.
     * @return Response yang berisi hasil pendaftaran.
     * @see RegisterRequest
     * @see RegisterResponse
     */
    @Headers("Accept: application/json")
    @POST("api/register")
    fun register(
        @Body registerRequest: RegisterRequest
    ): Call<RegisterResponse>

    /**
     * Mengambil informasi profil pengguna.
     *
     * @param token Token otorisasi untuk autentikasi pengguna.
     * @return Response yang berisi informasi profil pengguna.
     * @see UserResponse
     */
    @GET("api/profile")
    fun getUser(
        @Header("Authorization") token: String
    ): Call<UserResponse>

    /**
     * Mengupdate informasi profil pengguna.
     *
     * @param token Token otorisasi untuk autentikasi pengguna.
     * @param profileData Data profil yang akan diperbarui.
     * @return Response yang berisi hasil pembaruan profil.
     * @see UserResponse
     */
    @PUT("api/profile")
    fun updateProfile(
        @Header("Authorization") token: String,
        @Body profileData: Map<String, String>
    ): Call<UserResponse>

    /**
     * Mengubah kata sandi pengguna.
     *
     * @param token Token otorisasi untuk autentikasi pengguna.
     * @param passwordData Data yang berisi kata sandi lama dan baru.
     * @return Response yang berisi hasil perubahan kata sandi.
     * @see Void
     */
    @PUT("api/profile/change-password")
    fun changePassword(
        @Header("Authorization") token: String,
        @Body passwordData: Map<String, String>
    ): Call<Void>

    /**
     * Keluar dari akun pengguna.
     *
     * @param token Token otorisasi untuk autentikasi pengguna.
     * @return Response yang berisi hasil logout.
     * @see Void
     */
    @POST("api/logout")
    fun logout(
        @Header("Authorization") token: String
    ): Call<Void>
}
