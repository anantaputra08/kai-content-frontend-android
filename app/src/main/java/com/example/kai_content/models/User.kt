package com.example.kai_content.models

/**
 * Model data untuk pengguna.
 *
 * @param id ID pengguna.
 * @param name Nama pengguna.
 * @param email Alamat email pengguna.
 * @param phone Nomor telepon pengguna.
 * @param role Peran pengguna (misalnya, admin, user).
 * @param profile_picture URL lengkap foto profil pengguna.
 * @param created_at Tanggal pembuatan akun.
 * @param updated_at Tanggal pembaruan terakhir.
 */
data class User(
    val id: Int, // ID pengguna
    val name: String, // Nama pengguna
    val email: String, // Email pengguna
    val phone: String, // Nomor telepon pengguna
    val role: String,
    val profile_picture: String?, // URL lengkap foto profil
    val created_at: String, // Tanggal pembuatan akun
    val updated_at: String // Tanggal pembaruan terakhir
)
