package com.example.kai_content.ui.operator.content

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kai_content.api.ContentApi
import com.example.kai_content.api.RetrofitClient
import com.example.kai_content.models.content.Category
import com.example.kai_content.models.content.Content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import java.io.File

class EditContenViewModel : ViewModel() {

    private val contentApi = RetrofitClient.instance.create(ContentApi::class.java)
    private val categoryApi = RetrofitClient.instance.create(ContentApi::class.java)

    private val _contentDetails = MutableLiveData<Content>()
    val contentDetails: LiveData<Content> = _contentDetails

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    // Loading state to track API calls
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getContentDetails(
        contentId: String,
        token: String,
        onSuccess: (Content) -> Unit,
        onError: (String) -> Unit
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = contentApi.getContentById("Bearer $token", contentId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val content = response.body()!!.data
                        _contentDetails.value = content
                        onSuccess(content)
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        onError("Error: ${response.code()} - $errorBody")
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Unknown error occurred while fetching content details")
                    _isLoading.value = false
                }
            }
        }
    }

    fun getAllCategories(
        token: String,
        onSuccess: (List<Category>) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = categoryApi.getAllCategories("Bearer $token")

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val categoriesList = response.body()!!.data
                        _categories.value = categoriesList
                        onSuccess(categoriesList)
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        onError("Error: ${response.code()} - $errorBody")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Unknown error occurred while fetching categories")
                }
            }
        }
    }

    fun updateContent(
        id: String,
        title: String?,
        description: String?,
        status: String?,
        categoryIds: List<Int>,
        file: File?,
        thumbnail: File?,
        token: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Create PUT method RequestBody
                val methodPart = "PUT".toRequestBody("text/plain".toMediaTypeOrNull())

                // Create request body parts with plain text content type
                val titlePart = title?.let {
                    it.toRequestBody("text/plain".toMediaTypeOrNull())
                }
                val descriptionPart = description?.let {
                    it.toRequestBody("text/plain".toMediaTypeOrNull())
                }
                val statusPart = status?.let {
                    it.toRequestBody("text/plain".toMediaTypeOrNull())
                }

                // Create JSON array of category IDs
                val categoryArray = JSONArray(categoryIds)
                val categoriesPart = categoryArray.toString().toRequestBody("application/json".toMediaTypeOrNull())

                // Log what we're sending for debugging
                Log.d("ContentUpdate", "Sending update for ID $id with: title=$title, description=$description, status=$status, categories=$categoryArray")

                // Only create file part if file exists
                val filePart = file?.let {
                    MultipartBody.Part.createFormData(
                        "file", it.name, it.asRequestBody("video/*".toMediaTypeOrNull())
                    )
                }

                // Only create thumbnail part if thumbnail exists
                val thumbnailPart = thumbnail?.let {
                    MultipartBody.Part.createFormData(
                        "thumbnail", it.name, it.asRequestBody("image/*".toMediaTypeOrNull())
                    )
                }

                val response = contentApi.updateContent(
                    "Bearer $token",
                    id,
                    methodPart,
                    titlePart,
                    descriptionPart,
                    statusPart,
                    categoriesPart,
                    filePart,
                    thumbnailPart
                )

                // Log the response
                withContext(Dispatchers.Main) {
                    Log.d("ContentUpdate", "Update response for ID $id: ${response.code()} - ${response.body()}")

                    _isLoading.value = false
                    if (response.isSuccessful) {
                        // Wait a brief moment to ensure the update is processed
                        withContext(Dispatchers.IO) {
                            // Small delay to ensure database commit
                            kotlinx.coroutines.delay(500)
                        }

                        // Refresh content details after successful update
                        getContentDetails(id, token,
                            onSuccess = { content ->
                                Log.d("ContentUpdate", "Content refreshed after update: ID=$id, title=${content.title}, updatedAt=${content.updatedAt}")
                                onSuccess()
                            },
                            onError = { error ->
                                Log.e("ContentUpdate", "Failed to refresh content: $error")
                                // Even if refresh fails, the update might have been successful
                                onSuccess()
                            }
                        )
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Log.e("ContentUpdate", "Update failed: ${response.code()} - $errorBody")
                        onError("Error: ${response.code()} - $errorBody")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ContentUpdate", "Exception during update: ${e.message}")
                    _isLoading.value = false
                    onError(e.message ?: "Unknown error occurred during update")
                }
            }
        }
    }
}
