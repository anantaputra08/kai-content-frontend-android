package com.example.kai_content.models.content

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * Model data untuk konten.
 *
 * @param id ID konten.
 * @param title Judul konten.
 * @param description Deskripsi konten (nullable).
 * @param filePath Jalur file konten.
 * @param thumbnailPath Jalur thumbnail konten (nullable).
 * @param type Jenis konten (nullable).
 * @param status Status konten.
 * @param viewCount Jumlah tampilan konten.
 * @param totalWatchTime Total waktu tonton konten (nullable).
 * @param rank Peringkat konten (nullable).
 * @param like Jumlah suka pada konten.
 * @param dislike Jumlah tidak suka pada konten.
 * @param videoUrl URL video konten.
 * @param thumbnailUrl URL thumbnail konten.
 * @param isLike Status suka pada konten (default: false).
 * @param isDislike Status tidak suka pada konten (default: false).
 * @param isFavorite Status favorit pada konten (default: false).
 * @param createdAt Tanggal dan waktu pembuatan konten.
 * @param updatedAt Tanggal dan waktu pembaruan konten.
 * @param categories Daftar kategori yang terkait dengan konten (nullable).
 *
 * @see Parcelize
 * @see Parcelable
 * @see SerializedName
 * @see Date
 * @see Category
 * @see CategoryPivot
 */
@Parcelize
data class Content(
    val id: Int,
    val title: String,
    val description: String?,
    @SerializedName("file_path")
    val filePath: String,
    @SerializedName("thumbnail_path")
    val thumbnailPath: String?,
    val type: String?,
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

    /**
     * Menghitung waktu yang telah berlalu sejak konten dibuat.
     *
     * @return String yang menunjukkan waktu yang telah berlalu (misalnya, "Hari ini", "Kemarin", "X hari yang lalu").
     *
     * @see Date
     */
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

/**
 * Model data untuk kategori konten.
 *
 * @param id ID kategori.
 * @param name Nama kategori.
 * @param createdAt Tanggal dan waktu pembuatan kategori.
 * @param updatedAt Tanggal dan waktu pembaruan kategori.
 * @param pivot Objek pivot yang menghubungkan konten dengan kategori (nullable).
 *
 * @see Parcelize
 * @see Parcelable
 * @see CategoryPivot
 */
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

/**
 * Model data untuk pivot kategori konten.
 *
 * @param contentId ID konten yang terhubung dengan kategori.
 * @param categoryId ID kategori yang terhubung dengan konten.
 *
 * @see Parcelize
 * @see Parcelable
 * @see SerializedName
 * @see Content
 * @see Category
 */
@Parcelize
data class CategoryPivot(
    @SerializedName("content_id")
    val contentId: Int,
    @SerializedName("category_id")
    val categoryId: Int
) : Parcelable

/**
 * Model data untuk respons konten.
 *
 * @param status Status respons.
 * @param message Pesan dari server.
 * @param data Objek konten yang diminta.
 *
 * @see Content
 */
data class ContentResponse(
    val status: String,
    val message: String,
    val data: Content
)

/**
 * Model data untuk respons daftar konten.
 *
 * @param status Status respons.
 * @param message Pesan dari server.
 * @param data Daftar objek konten.
 *
 * @see Content
 */
data class ContentsResponse(
    val status: String,
    val message: String,
    val data: List<Content>
)

/**
 * Model data untuk respons kategori konten.
 *
 * @param status Status respons.
 * @param message Pesan dari server.
 * @param data Daftar objek kategori.
 *
 * @see Category
 */
data class CategoriesResponse(
    val status: String,
    val message: String,
    val data: List<Category>
)
