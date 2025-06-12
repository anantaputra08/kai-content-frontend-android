package com.example.kai_content.ui.home2

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kai_content.api.RetrofitClient
import com.example.kai_content.api.Train
import com.example.kai_content.api.TrainApi
import kotlinx.coroutines.launch

class Home2ViewModel : ViewModel() {

    private val _trains = MutableLiveData<List<Train>>()
    val trains: LiveData<List<Train>> = _trains

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val apiService: TrainApi by lazy {
        RetrofitClient.instance.create(TrainApi::class.java)
    }

    init {
        fetchTrains()
    }

    fun fetchTrains() {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d("Home2ViewModel", "Memulai fetchTrains...") // Log saat fungsi dimulai
            try {
                val response = apiService.getTrains()
                Log.d("Home2ViewModel", "Response diterima. Kode: ${response.code()}") // Log kode status

                if (response.isSuccessful) {
                    val trainData = response.body()?.data
                    _trains.postValue(trainData ?: emptyList())
                    _error.postValue(null)
                    Log.d("Home2ViewModel", "Data berhasil dimuat: ${trainData?.size ?: 0} kereta.") // Log jika sukses
                } else {
                    val errorBody = response.errorBody()?.string()
                    _error.postValue("Gagal memuat daftar kereta: ${response.message()} (Kode: ${response.code()})")
                    Log.e("Home2ViewModel", "API Error: ${response.message()}, Body: $errorBody") // Log jika gagal
                }

            } catch (e: Exception) {
                _error.postValue("Terjadi kesalahan jaringan: ${e.message}")
                Log.e("Home2ViewModel", "Exception saat fetchTrains", e) // Log jika ada exception
            } finally {
                _isLoading.value = false
            }
        }
    }
}
