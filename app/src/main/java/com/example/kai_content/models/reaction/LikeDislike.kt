package com.example.kai_content.models.reaction

data class LikeDislikeResponse(
    val status: String,
    val data: LikeDislikeData?
)

data class LikeDislikeData(
    val id: Int,
    val user_id: Int,
    val content_id: Int,
    val is_like: Boolean? = false,
    val is_dislike: Boolean? = false,
)
