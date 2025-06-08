package com.example.kai_content.models.login

/**
 * Model data untuk permintaan login.
 *
 * @param email Alamat email pengguna.
 * @param password Kata sandi pengguna.
 */
data class LoginRequest(
    val email: String,
    val password: String
)
