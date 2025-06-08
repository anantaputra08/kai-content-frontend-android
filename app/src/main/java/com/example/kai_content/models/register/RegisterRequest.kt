package com.example.kai_content.models.register

/**
 * Model data untuk permintaan pendaftaran.
 *
 * @param name Nama pengguna.
 * @param email Alamat email pengguna.
 * @param password Kata sandi pengguna.
 * @param password_confirmation Konfirmasi kata sandi pengguna.
 * @param phone Nomor telepon pengguna.
 */
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val password_confirmation: String,
    val phone: String
)
