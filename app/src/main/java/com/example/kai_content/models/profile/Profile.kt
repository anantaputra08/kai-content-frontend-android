package com.example.kai_content.models.profile

/**
 * Model data untuk profil pengguna.
 *
 * @param name Nama pengguna.
 * @param role Peran pengguna (misalnya "Buyer" atau "Seller").
 * @param profilePictureUrl URL lengkap foto profil (nullable).
 */
data class Profile(
    val name: String, // Nama pengguna
    val role: String, // Peran pengguna (misalnya "Buyer" atau "Seller")
    val profilePictureUrl: String? // URL lengkap foto profil
)
