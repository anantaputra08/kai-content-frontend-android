package com.example.kai_content.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kai_content.api.AuthApi
import com.example.kai_content.api.RetrofitClient
import com.example.kai_content.models.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProfileViewModel : ViewModel() {
    private val _user = MutableLiveData<UserResponse>()
    val user: LiveData<UserResponse> = _user

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> get() = _updateSuccess

    private val authApi = RetrofitClient.instance.create(AuthApi::class.java)

    fun fetchUser(token: String) {
        _loading.value = true
        authApi.getUser("Bearer $token").enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                _loading.value = false
                if (response.isSuccessful) {
                    _user.value = response.body()
                } else {
                    _message.value = "Gagal memuat data profil"
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                _loading.value = false
                _message.value = "Error: ${t.message}"
            }
        })
    }

    fun updateUser(token: String, data: Map<String, String>) {
        _loading.value = true
        authApi.updateProfile("Bearer $token", data).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                _loading.value = false
                if (response.isSuccessful) {
                    _user.value = response.body()
                    _message.value = "Profil berhasil diperbarui"
                    _updateSuccess.postValue(true)
                } else {
                    _message.value = "Gagal memperbarui profil"
                    _updateSuccess.postValue(false)
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                _loading.value = false
                _message.value = "Error: ${t.message}"
                _updateSuccess.postValue(false)
            }
        })
    }
}
