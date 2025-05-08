package com.example.kai_content.models

data class UserResponse(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val address: String?,
    val role: String,
    val profile_picture: String? // Tambahkan ini untuk URL foto profil
)