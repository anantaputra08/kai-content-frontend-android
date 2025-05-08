package com.example.kai_content.ui.operator.content

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kai_content.api.ContentApi
import com.example.kai_content.api.RetrofitClient
import com.example.kai_content.models.content.Content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContentViewModel : ViewModel() {
    private val _contents = MutableLiveData<List<Content>>()
    val contents: LiveData<List<Content>> get() = _contents

    private val contentApi = RetrofitClient.instance.create(ContentApi::class.java)

    fun fetchContents(token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = contentApi.getContents("Bearer $token").execute()
                if (response.isSuccessful) {
                    _contents.postValue(response.body())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun searchContents(token: String, title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = contentApi.searchContents("Bearer $token", title).execute()
                if (response.isSuccessful) {
                    _contents.postValue(response.body())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
