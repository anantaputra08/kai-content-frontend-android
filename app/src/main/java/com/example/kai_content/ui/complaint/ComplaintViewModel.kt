package com.example.kai_content.ui.complaint

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kai_content.api.ComplaintApi
import com.example.kai_content.api.RetrofitClient
import com.example.kai_content.models.complaint.ComplaintCategory
import com.example.kai_content.models.complaint.ComplaintRequest
import com.example.kai_content.models.complaint.ComplaintResponse
import com.example.kai_content.models.content.Category
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ComplaintViewModel : ViewModel() {

    private val _complaints = MutableLiveData<List<ComplaintResponse>>()
    val complaints: LiveData<List<ComplaintResponse>> = _complaints

    private val _categories = MutableLiveData<List<ComplaintCategory>>()
    val categories: LiveData<List<ComplaintCategory>> = _categories

    private val _isLoadingCategories = MutableLiveData<Boolean>()
    val isLoadingCategories: LiveData<Boolean> = _isLoadingCategories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isSubmitting = MutableLiveData<Boolean>()
    val isSubmitting: LiveData<Boolean> = _isSubmitting

    private val _submissionResult = MutableLiveData<Boolean>()
    val submissionResult: LiveData<Boolean> = _submissionResult

    private val apiService = RetrofitClient.instance.create(ComplaintApi::class.java)

    fun loadComplaints(token: String) { // Tambahkan parameter token
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val response = apiService.getComplaints("Bearer $token") // Sertakan token di header

                if (response.isSuccessful) {
                    val complaintsList = response.body() ?: emptyList()
                    _complaints.value = complaintsList
                } else {
                    _errorMessage.value = "Error: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load complaints: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchCategories(token: String) {
        viewModelScope.launch {
            try {
                _isLoadingCategories.value = true
                val response = apiService.getCategories("Bearer $token")

                if (response.isSuccessful) {
                    _categories.value = response.body() ?: emptyList()
                } else {
                    _errorMessage.value = "Error: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to fetch categories: ${e.message}"
            } finally {
                _isLoadingCategories.value = false
            }
        }
    }

    fun submitComplaint(context: Context, token: String, request: ComplaintRequest, imageUri: Uri?) { // Tambahkan parameter token
        viewModelScope.launch {
            try {
                _isSubmitting.value = true

                // Convert request fields to RequestBody objects
                val categoryIdRequestBody = request.category_complaint_id?.toString()
                    ?.toRequestBody("text/plain".toMediaTypeOrNull())

                val descriptionRequestBody = request.description
                    ?.toRequestBody("text/plain".toMediaTypeOrNull())

                val assignedToRequestBody = request.assigned_to?.toString()
                    ?.toRequestBody("text/plain".toMediaTypeOrNull())

                val resolutionDateRequestBody = request.resolution_date
                    ?.toRequestBody("text/plain".toMediaTypeOrNull())

                val resolutionNotesRequestBody = request.resolution_notes
                    ?.toRequestBody("text/plain".toMediaTypeOrNull())

                // Convert Uri to MultipartBody.Part
                var attachmentMultipart: MultipartBody.Part? = null
                if (imageUri != null) {
                    val file = uriToFile(context, imageUri)
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    attachmentMultipart = MultipartBody.Part.createFormData(
                        "attachment",
                        file.name,
                        requestFile
                    )
                }

                // Make API call
                val response = apiService.createComplaint(
                    "Bearer $token", // Sertakan token di header
                    categoryIdRequestBody,
                    descriptionRequestBody,
                    attachmentMultipart,
                    assignedToRequestBody,
                    resolutionDateRequestBody,
                    resolutionNotesRequestBody
                )

                if (response.isSuccessful) {
                    val newComplaint = response.body()
                    if (newComplaint != null) {
                        // Add to the existing list
                        val currentList = _complaints.value?.toMutableList() ?: mutableListOf()
                        currentList.add(0, newComplaint)
                        _complaints.value = currentList

                        _submissionResult.value = true
                    } else {
                        _errorMessage.value = "Response body is null"
                    }
                } else {
                    _errorMessage.value = "Error: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to submit complaint: ${e.message}"
            } finally {
                _isSubmitting.value = false
                // Reset submission result after a short delay
                viewModelScope.launch {
                    kotlinx.coroutines.delay(100)
                    _submissionResult.value = false
                }
            }
        }
    }

    // Helper function to convert Uri to File
    private fun uriToFile(context: Context, uri: Uri): File {
        val contentResolver = context.contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(uri)

        val fileName = getFileName(contentResolver, uri)
        val file = File(context.cacheDir, fileName)

        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        return file
    }

    // Helper function to get the filename from Uri
    private fun getFileName(contentResolver: android.content.ContentResolver, uri: Uri): String {
        var name = ""
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }

        if (name.isEmpty()) {
            name = "file_" + System.currentTimeMillis() + "." + getFileExtension(uri.toString())
        }

        return name
    }

    private fun getFileExtension(uriString: String): String {
        return uriString.substringAfterLast('.', "jpg")
    }
}
