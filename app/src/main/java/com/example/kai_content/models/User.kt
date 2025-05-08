package com.example.kai_content.models

data class User(
    val id: Int, // ID pengguna
    val name: String, // Nama pengguna
    val email: String, // Email pengguna
    val phone: String, // Nomor telepon pengguna
    val role: String,
    val created_at: String, // Tanggal pembuatan akun
    val updated_at: String // Tanggal pembaruan terakhir
)
