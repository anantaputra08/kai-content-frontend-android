package com.example.kai_content.models.profile

data class Profile(
    val name: String, // Nama pengguna
    val role: String, // Peran pengguna (misalnya "Buyer" atau "Seller")
    val profilePictureUrl: String // URL lengkap foto profil
)