package com.example.kai_content.ui.operator.content

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kai_content.api.ContentApi
import com.example.kai_content.api.RetrofitClient
import com.example.kai_content.models.content.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import java.io.File
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.buffer
import okio.sink
import java.io.IOException

class CreateContentViewModel : ViewModel() {

    private val contentApi = RetrofitClient.instance.create(ContentApi::class.java)
    private val categoryApi = RetrofitClient.instance.create(ContentApi::class.java)

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    // Loading state to track API calls
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _uploadProgress = MutableLiveData<Int>()
    val uploadProgress: LiveData<Int> = _uploadProgress

    // Status upload untuk memberikan informasi tahap
    private val _uploadStatus = MutableLiveData<String>()
    val uploadStatus: LiveData<String> = _uploadStatus

    // Progress tracking variables
    private var totalBytesToUpload: Long = 0
    private var totalBytesUploaded: Long = 0
    private var videoFileSize: Long = 0
    private var thumbnailFileSize: Long = 0
    private var videoUploaded: Boolean = false

    // Fungsi untuk mengatur progres upload
    fun updateProgress(progress: Int) {
        _uploadProgress.postValue(progress)
    }

    fun getAllCategories(
        token: String,
        onSuccess: (List<Category>) -> Unit,
        onError: (String) -> Unit
    ) {
        // Existing code, no changes needed
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

    fun createContent(
        token: String,
        title: String,
        description: String?,
        status: String,
        file: File,
        thumbnail: File?,
        categoryIds: List<Int>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _isLoading.postValue(true)
        _uploadProgress.postValue(0) // Reset progress at start
        _uploadStatus.postValue("Mempersiapkan Upload")

        // Reset tracking variables
        videoFileSize = file.length()
        thumbnailFileSize = thumbnail?.length() ?: 0
        totalBytesToUpload = videoFileSize + thumbnailFileSize
        totalBytesUploaded = 0
        videoUploaded = false

        _uploadStatus.postValue("Mengupload Video")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Create request body parts with plain text content type
                val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
                val descriptionPart = description?.toRequestBody("text/plain".toMediaTypeOrNull())
                val statusPart = status.toRequestBody("text/plain".toMediaTypeOrNull())

                // Create file part for video with progress tracking
                val fileRequestBody = file.asRequestBody("video/*".toMediaTypeOrNull())
                val progressRequestBody = ProgressRequestBody(fileRequestBody, "VIDEO") { bytesWritten, contentLength ->
                    updateUploadProgress(bytesWritten, "VIDEO")
                }

                val filePart = MultipartBody.Part.createFormData(
                    "file", file.name, progressRequestBody
                )

                // Create thumbnail part if exists
                val thumbnailPart = thumbnail?.let {
                    val thumbnailRequestBody = it.asRequestBody("image/*".toMediaTypeOrNull())
                    val thumbnailProgressRequestBody = ProgressRequestBody(thumbnailRequestBody, "THUMBNAIL") { bytesWritten, contentLength ->
                        updateUploadProgress(bytesWritten, "THUMBNAIL")
                    }

                    MultipartBody.Part.createFormData(
                        "thumbnail", it.name, thumbnailProgressRequestBody
                    )
                }

                // Log what we're sending for debugging
                Log.d("ContentCreate", "Sending content with: title=$title, description=$description, status=$status")

                // Handle categories using JSON array
                val categoryArray = JSONArray(categoryIds)
                val categoriesPart = categoryArray.toString().toRequestBody("application/json".toMediaTypeOrNull())

                // Make API call with progress tracked request body
                _uploadStatus.postValue("Mengirim Data ke Server")
                val response = contentApi.createContent(
                    "Bearer $token",
                    titlePart,
                    descriptionPart,
                    statusPart,
                    filePart,
                    thumbnailPart,
                    categoriesPart
                )

                withContext(Dispatchers.Main) {
                    _isLoading.postValue(false)
                    _uploadProgress.postValue(100) // Ensure we reach 100% at completion
                    _uploadStatus.postValue("Upload Selesai")

                    if (response.isSuccessful) {
                        Log.d("ContentCreate", "Content created successfully: ${response.body()}")
                        onSuccess()
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Log.e("ContentCreate", "Failed to create content: $errorBody")
                        onError("Error: ${response.code()} - $errorBody")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isLoading.postValue(false)
                    _uploadStatus.postValue("Upload Gagal")
                    Log.e("ContentCreate", "Exception during content creation: ${e.message}")
                    onError(e.message ?: "Unknown error occurred during creation")
                }
            }
        }
    }

    private fun updateUploadProgress(bytesWritten: Long, fileType: String) {
        if (fileType == "VIDEO") {
            if (!videoUploaded && bytesWritten >= videoFileSize) {
                videoUploaded = true
                _uploadStatus.postValue("Uploading Thumbnail")
            }
            totalBytesUploaded = bytesWritten
        } else if (fileType == "THUMBNAIL") {
            totalBytesUploaded = videoFileSize + bytesWritten
            _uploadStatus.postValue("Uploading video")
        }

        val progress = if (totalBytesToUpload > 0) {
            (100 * totalBytesUploaded / totalBytesToUpload).toInt()
        } else {
            0
        }

        _uploadProgress.postValue(progress)

        // Log progress for debugging
        Log.d("UploadProgress", "Progress: $progress% ($totalBytesUploaded/$totalBytesToUpload bytes)")
    }

    // Progress tracking RequestBody wrapper
    inner class ProgressRequestBody(
        private val delegate: RequestBody,
        private val fileType: String,
        private val progressListener: (bytesWritten: Long, contentLength: Long) -> Unit
    ) : RequestBody() {
        override fun contentType() = delegate.contentType()

        override fun contentLength(): Long {
            try {
                return delegate.contentLength()
            } catch (e: IOException) {
                return -1
            }
        }

        @Throws(IOException::class)
        override fun writeTo(sink: BufferedSink) {
            val contentLength = contentLength()
            val forwardingSink = object : ForwardingSink(sink) {
                private var bytesWritten = 0L

                @Throws(IOException::class)
                override fun write(source: Buffer, byteCount: Long) {
                    super.write(source, byteCount)
                    bytesWritten += byteCount
                    progressListener(bytesWritten, contentLength)
                }
            }

            val bufferedSink = forwardingSink.buffer()
            delegate.writeTo(bufferedSink)
            bufferedSink.flush()
        }
    }
}
