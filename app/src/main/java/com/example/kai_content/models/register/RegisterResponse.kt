package com.example.kai_content.models.register

import com.example.kai_content.models.User

data class RegisterResponse(
    val message: String,
    val token: String,
    val user: User
)