package com.example.kai_content.models.register

import com.example.kai_content.models.User

/**
 * Model data untuk respons pendaftaran.
 *
 * @param message Pesan dari server.
 * @param token Token pendaftaran.
 * @param user Data pengguna.
 * @see User
 */
data class RegisterResponse(
    val message: String,
    val token: String,
    val user: User
)
