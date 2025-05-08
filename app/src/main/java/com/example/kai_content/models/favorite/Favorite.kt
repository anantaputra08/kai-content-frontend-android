package com.example.kai_content.models.favorite

import com.example.kai_content.models.content.Content

data class FavoriteResponse(
    val status: String,
    val data: List<FavoriteItem>
)

data class FavoriteItem(
    val id: Int,
    val user_id: Int,
    val content_id: Int,
    val created_at: String,
    val updated_at: String,
    val content: Content?
)
