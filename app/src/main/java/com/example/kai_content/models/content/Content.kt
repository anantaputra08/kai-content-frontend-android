package com.example.kai_content.models.content

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Content(
    val id: Int,
    val title: String,
    val description: String?,
    @SerializedName("file_path")
    val filePath: String,
    @SerializedName("thumbnail_path")
    val thumbnailPath: String?,
    val type: String?, // karena bisa null
    val status: String,
    @SerializedName("view_count")
    val viewCount: Int,
    @SerializedName("total_watch_time")
    val totalWatchTime: Int?,
    val rank: Int?,
    val like: Int,
    val dislike: Int,
    @SerializedName("file_url")
    val videoUrl: String,
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String,
    val isLike : Boolean? = false,
    val isDislike : Boolean? = false,
    val isFavorite: Boolean = false,
    @SerializedName("created_at")
    val createdAt: Date,
    @SerializedName("updated_at")
    val updatedAt: Date,
    val categories: List<Category>?
) : Parcelable {

    fun getTimeAgo(): String {
        val now = Date()
        val diffInMillis = now.time - createdAt.time
        val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)

        return when {
            diffInDays < 1 -> "Today"
            diffInDays < 2 -> "Yesterday"
            diffInDays < 7 -> "$diffInDays days ago"
            diffInDays < 30 -> "${diffInDays / 7} weeks ago"
            else -> "${diffInDays / 30} months ago"
        }
    }
}

@Parcelize
data class Category(
    val id: Int,
    val name: String,
    @SerializedName("created_at")
    val createdAt: Date,
    @SerializedName("updated_at")
    val updatedAt: Date,
    val pivot: CategoryPivot?
) : Parcelable

@Parcelize
data class CategoryPivot(
    @SerializedName("content_id")
    val contentId: Int,
    @SerializedName("category_id")
    val categoryId: Int
) : Parcelable

data class ContentResponse(
    val status: String,
    val message: String,
    val data: Content
)

data class ContentsResponse(
    val status: String,
    val message: String,
    val data: List<Content>
)

data class CategoriesResponse(
    val status: String,
    val message: String,
    val data: List<Category>
)
