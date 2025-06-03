package com.example.kai_content.models.reaction

/**
 * Model data untuk respons suka/tidak suka.
 *
 * @param status Status dari respons.
 * @param data Data suka/tidak suka.
 * @see LikeDislikeData
 */
data class LikeDislikeResponse(
    val status: String,
    val data: LikeDislikeData?
)

/**
 * Model data untuk item suka/tidak suka.
 *
 * @param id ID item suka/tidak suka.
 * @param user_id ID pengguna yang memberikan reaksi.
 * @param content_id ID konten yang diberikan reaksi.
 * @param is_like Menunjukkan apakah item ini adalah suka (true) atau tidak (false).
 * @param is_dislike Menunjukkan apakah item ini adalah tidak suka (true) atau tidak (false).
 */
data class LikeDislikeData(
    val id: Int,
    val user_id: Int,
    val content_id: Int,
    val is_like: Boolean? = false,
    val is_dislike: Boolean? = false,
)
