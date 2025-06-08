package com.example.kai_content.models.login

import com.example.kai_content.models.User

/**
 * Model data untuk respons login.
 *
 * @param message Pesan dari server.
 * @param token Token login.
 * @param user Data pengguna.
 * @see User
 */
data class LoginResponse(
    val message: String, // Pesan dari server
    val token: String, // Token login
    val user: User // Data pengguna
)
