package com.example.kai_content.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kai_content.repositories.ContentRepository

class HomeViewModelFactory(
    private val contentRepository: ContentRepository,
    private val token: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(contentRepository, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
