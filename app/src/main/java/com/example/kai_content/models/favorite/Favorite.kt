package com.example.kai_content.models.favorite

import com.example.kai_content.models.content.Content

/**
 * Model data untuk respons favorit.
 *
 * @param status Status respons.
 * @param data Daftar item favorit.
 * @see FavoriteItem
 */
data class FavoriteResponse(
    val status: String,
    val data: List<FavoriteItem>
)

/**
 * Model data untuk item favorit.
 *
 * @param id ID item favorit.
 * @param user_id ID pengguna yang menandai item sebagai favorit.
 * @param content_id ID konten yang ditandai sebagai favorit.
 * @param created_at Tanggal dan waktu pembuatan item favorit.
 * @param updated_at Tanggal dan waktu pembaruan item favorit.
 * @param content Konten yang ditandai sebagai favorit (nullable).
 * @see Content
 */
data class FavoriteItem(
    val id: Int,
    val user_id: Int,
    val content_id: Int,
    val created_at: String,
    val updated_at: String,
    val content: Content?
)
