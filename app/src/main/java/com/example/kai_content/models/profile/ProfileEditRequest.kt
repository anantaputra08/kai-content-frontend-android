package com.example.kai_content.models.profile

data class ProfileEditRequest (
    val name: String,
    val email: String,
    val password: String,
    val profile_picture: String
)
