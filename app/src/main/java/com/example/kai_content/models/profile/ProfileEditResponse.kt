package com.example.kai_content.models.profile

import com.example.kai_content.models.User

data class ProfileEditResponse(
    val message: String, // Pesan dari server
    val user: User // Data pengguna
)