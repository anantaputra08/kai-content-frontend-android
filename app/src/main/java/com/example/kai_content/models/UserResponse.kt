package com.example.kai_content.models

/**
 * Model data untuk respons pengguna.
 *
 * @param id ID pengguna.
 * @param name Nama pengguna.
 * @param email Alamat email pengguna.
 * @param phone Nomor telepon pengguna (opsional).
 * @param address Alamat pengguna (opsional).
 * @param role Peran pengguna.
 * @param profile_picture URL foto profil pengguna (opsional).
 */
data class UserResponse(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val address: String?,
    val role: String,
    val profile_picture: String? // Tambahkan ini untuk URL foto profil
)
