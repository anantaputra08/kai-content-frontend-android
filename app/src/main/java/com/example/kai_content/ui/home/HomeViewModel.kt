package com.example.kai_content.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kai_content.models.content.Content
import com.example.kai_content.repositories.ContentRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val contentRepository: ContentRepository,
    private val token: String // <- Tambah token di constructor
) : ViewModel() {

    private val _contents = MutableLiveData<List<Content>>()
    val contents: LiveData<List<Content>> = _contents

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadContents()
    }

    fun loadContents() {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val contentList = contentRepository.getContents(token) // <- Kirim token di sini
                _contents.value = contentList
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshContents() {
        loadContents()
    }
}
