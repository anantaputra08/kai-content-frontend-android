package com.example.kai_content.ui.complaint

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ComplaintViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ComplaintViewModel::class.java)) {
            return ComplaintViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
