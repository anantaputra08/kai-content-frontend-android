package com.example.kai_content.ui.favorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kai_content.api.FavoriteApi
import com.example.kai_content.api.RetrofitClient
import com.example.kai_content.models.content.Content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListFavoriteViewModel(

) : ViewModel() {
    private val _favorites = MutableLiveData<List<Content>>()
    val favorites: LiveData<List<Content>> get() = _favorites

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val favoriteApi: FavoriteApi by lazy {
        RetrofitClient.instance.create(FavoriteApi::class.java)
    }

    fun fetchFavorites(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = favoriteApi.getFavorites("Bearer $token")
                val contentList = response.data.mapNotNull { it.content }
                _favorites.postValue(contentList)
            } catch (e: Exception) {
                _errorMessage.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }


}
