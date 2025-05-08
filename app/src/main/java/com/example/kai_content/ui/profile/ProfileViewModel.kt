package com.example.kai_content.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kai_content.api.AuthApi
import com.example.kai_content.api.RetrofitClient
import com.example.kai_content.models.UserResponse
import com.example.kai_content.models.profile.Profile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileViewModel : ViewModel() {
    private val _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _userType = MutableLiveData<String>()
    val userType: LiveData<String> = _userType

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _profile = MutableLiveData<Profile>()
    val profile: LiveData<Profile> = _profile

    private val authApi = RetrofitClient.instance.create(AuthApi::class.java)

    fun fetchUserData(token: String) {
        _loading.value = true
        _error.value = null

        authApi.getUser("Bearer $token").enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                _loading.value = false
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        _name.value = user.name
                        _email.value = user.email
//                        _userType.value = user.type // Assuming `type` exists in `UserResponse`
                        _profile.value = Profile(
                            name = user.name,
                            role = user.role, // Atur sesuai data
                            profilePictureUrl = user.profile_picture ?: ""
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _error.value = "Failed to fetch user data: $errorBody"
                    Log.e("ProfileViewModel", errorBody ?: "Unknown error")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                _loading.value = false
                _error.value = "Error: ${t.message}"
                Log.e("ProfileViewModel", "Error: ${t.message}")
            }
        })
    }
}