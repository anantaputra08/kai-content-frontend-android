package com.example.kai_content.models.login

import com.example.kai_content.models.User

data class LoginResponse(
    val message: String, // Pesan dari server
    val token: String, // Token login
    val user: User // Data pengguna
)