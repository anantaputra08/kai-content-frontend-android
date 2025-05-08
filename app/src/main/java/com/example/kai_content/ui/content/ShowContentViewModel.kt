package com.example.kai_content.ui.content

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kai_content.api.ContentApi
import com.example.kai_content.api.FavoriteApi
import com.example.kai_content.api.LikeDislikeApi
import com.example.kai_content.api.ReactionRequest
import com.example.kai_content.api.RetrofitClient
import com.example.kai_content.models.content.Content
import com.example.kai_content.models.reaction.LikeDislikeData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShowContentViewModel : ViewModel() {

    private val _contents = MutableLiveData<List<Content>>()
    val contents: LiveData<List<Content>> = _contents

    private val _content = MutableLiveData<Content?>()
    val content: LiveData<Content?> get() = _content

    private val _likeDislikeData = MutableLiveData<LikeDislikeData?>()
    val likeDislikeData: LiveData<LikeDislikeData?> get() = _likeDislikeData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private var watchTimeJob: Job? = null
    private var currentWatchTime = 0
    private var lastContentId: String? = null

    private val api: ContentApi by lazy {
        RetrofitClient.instance.create(ContentApi::class.java)
    }
    private val favoriteApi: FavoriteApi by lazy {
        RetrofitClient.instance.create(FavoriteApi::class.java)
    }
    private val likeDislikeApi: LikeDislikeApi by lazy {
        RetrofitClient.instance.create(LikeDislikeApi::class.java)
    }

    fun getContent(token: String, contentId: String) {
        if (contentId == lastContentId && _content.value != null) {
            return // Mencegah pengambilan ulang konten yang sama
        }

        lastContentId = contentId
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.getContent("Bearer $token", contentId)
                }

                if (response.isSuccessful && response.body() != null) {
                    val contentResponse = response.body()
                    _content.value = contentResponse?.data

                    // Reset watch time tracking untuk konten baru
                    resetWatchTimeTracking()

                    // After getting content, fetch related videos
                    getRelatedVideos(token, contentId)
                } else {
                    _error.value = "Failed to load content: ${response.message()}"
                }
                _isLoading.value = false

            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private fun getRelatedVideos(token: String, contentId: String) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.getRelatedContents(token, contentId)
                }

                if (response.isSuccessful && response.body() != null) {
//                    _relatedVideos.value = response.body()?.data ?: emptyList()
                    val relatedContents = response.body()?.data ?: emptyList()
                    _contents.value = relatedContents
                    Log.d("ShowContentViewModel", "Related videos loaded successfully for content ID: $contentId")
                } else {
                    Log.e("ShowContentViewModel", "Failed to load related videos: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("ShowContentViewModel", "Error loading related videos: ${e.message}")
                // Don't show error to user, just log it
            }
        }
    }

    fun checkLikeDislike(token: String, contentId: String) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    likeDislikeApi.checkLikeDislike("Bearer $token", contentId)
                }

                if (response.isSuccessful && response.body() != null) {
                    // true is like, false is dislike
                    val isLike = response.body()?.data?.is_like ?: false
                    val isDislike = response.body()?.data?.is_dislike ?: false
                    Log.d("LikeDislike", "isLike: $isLike")

                    // Perbarui status like/dislike di UI
                    _content.value?.let { currentContent ->
                        _content.value = currentContent.copy(
                            isLike = isLike,
                            isDislike = isDislike,
                        )
                    }
                } else {
                    Log.e("LikeDislike", "Failed to check like/dislike: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("LikeDislike", "Error checking like/dislike: ${e.message}")
            }
        }
    }

    fun toggleLikeDislike(token: String, contentId: String) {

    }

    /**
     * Set reaksi (like/dislike) pada konten sesuai dengan backend PHP baru
     * @param token Token autentikasi
     * @param contentId ID konten
     * @param reactionType Jenis reaksi ("like" atau "dislike")
     * @param action Boolean - true untuk membuat/mengubah reaksi, false untuk menghapus
     */
    fun setReaction(token: String, contentId: String, reactionType: String, action: Boolean) {
        // Gunakan data class sebagai request body
        val requestBody = ReactionRequest(
            reaction_type = reactionType,
            action = action,
            content_id = contentId
        )

        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    // Menggunakan header Authorization untuk token
                    likeDislikeApi.setReaction("Bearer $token", contentId, requestBody)
                }

                if (response.isSuccessful) {
                    // Perbarui konten dengan nilai like/dislike baru
                    _content.value?.let { currentContent ->
                        val responseBody = response.body()
                        val updatedData = responseBody?.get("data") as? Map<*, *>

                        if (updatedData != null) {
                            val newLikes = (updatedData["like"] as? Number)?.toInt() ?: currentContent.like
                            val newDislikes = (updatedData["dislike"] as? Number)?.toInt() ?: currentContent.dislike
                            val isLike = updatedData["is_like"] as? Boolean ?: currentContent.isLike
                            val isDislike = updatedData["is_dislike"] as? Boolean ?: currentContent.isDislike

                            // Buat salinan konten dengan nilai yang diperbarui
                            _content.value = currentContent.copy(
                                like = newLikes,
                                dislike = newDislikes,
                                isLike = isLike,
                                isDislike = isDislike
                            )

                            Log.d("ContentViewModel", "Reaction updated: likes=$newLikes, dislikes=$newDislikes")
                            Log.d("ContentViewModel", "isLike: $isLike, isDislike: $isDislike")
                        }
                    }
                } else {
                    Log.e("ContentViewModel", "Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ContentViewModel", "Exception: ${e.message}")
            }
        }
    }

    fun checkFavorite(token: String, contentId: String) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    favoriteApi.checkFavorite("Bearer $token", contentId)
                }

                if (response.isSuccessful && response.body() != null) {
                    val isFavorite = response.body()?.get("is_favorite") as? Boolean ?: false

                    // Perbarui status favorit di UI
                    _content.value?.let { currentContent ->
                        _content.value = currentContent.copy(
                            isFavorite = isFavorite
                        )
                    }
                } else {
                    Log.e("Favorite", "Failed to check favorite: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("Favorite", "Error checking favorite: ${e.message}")
            }
        }
    }

    fun toggleFavorite(token: String, contentId: String) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    favoriteApi.toggleFavorite("Bearer $token", contentId)
                }

                if (response.isSuccessful && response.body() != null) {
                    val isFavorite = response.body()?.get("is_favorite") as? Boolean ?: false

                    // Perbarui status favorit di UI
                    _content.value?.let { currentContent ->
                        _content.value = currentContent.copy(
                            isFavorite = isFavorite
                        )
                    }

                    // Tampilkan pesan sukses
                    val message = response.body()?.get("message") as? String
                    message?.let { Log.d("Favorite", it) }
                } else {
                    Log.e("Favorite", "Failed to toggle favorite: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("Favorite", "Error toggling favorite: ${e.message}")
            }
        }
    }

    fun startWatchTimeTracking(token: String, contentId: String) {
        stopWatchTimeTracking()

        currentWatchTime = 0
        watchTimeJob = viewModelScope.launch {
            while (true) {
                delay(5000) // Mencatat setiap 5 detik
                currentWatchTime += 5

                // Laporkan setiap 30 detik
                if (currentWatchTime % 10 == 0) {
                    reportWatchTime(token, contentId, 10)
                    Log.d("WatchTime", "Reported watch time: $currentWatchTime seconds")
                }
            }
        }
    }

    fun stopWatchTimeTracking() {
        watchTimeJob?.cancel()
        watchTimeJob = null
    }

    fun resetWatchTimeTracking() {
        stopWatchTimeTracking()
        currentWatchTime = 0
    }

    private fun reportWatchTime(token: String, contentId: String, seconds: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                api.reportWatchTime("Bearer $token", contentId, seconds)
                Log.d("WatchTime", "Reported $seconds seconds for content ID: $contentId")
            } catch (e: Exception) {
                // Log saja, tidak perlu menampilkan error ke user
                e.printStackTrace()
                Log.e("WatchTime", "Failed to report watch time: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopWatchTimeTracking()
    }
}
