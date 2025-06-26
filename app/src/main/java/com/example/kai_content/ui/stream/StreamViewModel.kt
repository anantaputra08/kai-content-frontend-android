package com.example.kai_content.ui.stream

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kai_content.api.ContentInfo
import com.example.kai_content.api.LocationInfo
import com.example.kai_content.api.RetrofitClient
import com.example.kai_content.api.StreamApi
import com.example.kai_content.api.VoteRequest
import com.example.kai_content.api.Voting
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit

class StreamViewModel : ViewModel() {
    private val apiService: StreamApi by lazy {
        RetrofitClient.instance.create(StreamApi::class.java)
    }

    // Menyimpan kedua ID yang sedang aktif
    private var currentTrainId: Long? = null
    private var currentCarriageId: Long? = null

    private val _streamStatus = MutableLiveData<StreamStatusViewModelData?>()
    val streamStatus: LiveData<StreamStatusViewModelData?> = _streamStatus

    private val _activeVoting = MutableLiveData<Voting?>()
    val activeVoting: LiveData<Voting?> = _activeVoting

    private val _statusMessage = MutableLiveData<String?>()
    val statusMessage: LiveData<String?> = _statusMessage

    // Mengganti _carriage dengan _locationInfo
    private val _locationInfo = MutableLiveData<LocationInfo?>()
    val locationInfo: LiveData<LocationInfo?> = _locationInfo

    private val _votingTimeLeft = MutableLiveData<String>()
    val votingTimeLeft: LiveData<String> = _votingTimeLeft

    private val _isLoading = MutableLiveData<Boolean>(true)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var votingCountdownTimer: Timer? = null
    private var periodicUpdateJob: Job? = null

    // Data class disederhanakan sesuai respons API terbaru
    data class StreamStatusViewModelData(
        val displayContent: ContentInfo,
        val isLive: Boolean,
    )

    /**
     * Fungsi utama untuk memuat data, sekarang menggunakan trainId dan carriageId.
     */
    fun loadDataForLocation(trainId: Long, carriageId: Long) {
        if (currentTrainId == trainId && currentCarriageId == carriageId) return
        currentTrainId = trainId
        currentCarriageId = carriageId

        periodicUpdateJob?.cancel()
        startPeriodicUpdates(trainId, carriageId)
    }

    /**
     * Mengambil status dari API menggunakan kedua ID.
     */
    private fun fetchStatus(trainId: Long, carriageId: Long) {
        // Tidak menampilkan loading pada pembaruan periodik agar UI lebih halus
        if (periodicUpdateJob == null) {
            _isLoading.value = true
        }

        viewModelScope.launch {
            try {
                // Memanggil endpoint API yang sudah diperbarui
                val response = apiService.getStatusForLocation(trainId, carriageId)

                if (response.isSuccessful) {
                    val status = response.body()
                    _error.value = null

                    // Memperbarui info lokasi (kereta dan gerbong)
                    _locationInfo.value = status?.locationInfo
                    // Cek apakah ada pesan status perjalanan dari server
                    if (status?.message != null) {
                        // Jika ada, tampilkan pesan itu dan kosongkan data lain
                        _statusMessage.value = status.message
                        _streamStatus.value = null
                        _activeVoting.value = null
                        votingCountdownTimer?.cancel()
                        _votingTimeLeft.postValue("")
                    } else {
                        // Jika tidak ada pesan, proses seperti biasa
                        _statusMessage.value = null
                        handleStreamResponse(status?.nowPlaying)
                        handleVotingResponse(status?.activeVoting)
                    }
                } else {
                    _error.value = "Gagal memuat status: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Kesalahan jaringan: ${e.message}"
                Log.e("StreamViewModel", "Network error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Memproses data 'now_playing' dari API.
     */
    private fun handleStreamResponse(nowPlaying: com.example.kai_content.api.StreamResponse?) {
        val newStatus = if (nowPlaying?.content != null) {
            StreamStatusViewModelData(
                displayContent = nowPlaying.content,
                isLive = nowPlaying.isLive
            )
        } else {
            null
        }

        if (_streamStatus.value != newStatus) {
            _streamStatus.value = newStatus
        }
    }

    /**
     * Memproses data 'active_voting' dari API.
     */
    private fun handleVotingResponse(newVoting: Voting?) {
        val votingJustEnded = _activeVoting.value != null && newVoting == null
        // Jika ID voting berubah (voting baru dimulai atau selesai)
        if (_activeVoting.value?.id != newVoting?.id) {
            _activeVoting.value = newVoting
            if (newVoting != null) {
                startVotingCountdown(newVoting.endTime)
            } else {
                // Hentikan timer jika tidak ada voting
                votingCountdownTimer?.cancel()
                _votingTimeLeft.postValue("")
            }
        }
        // Jika ID sama, cek apakah ada perubahan jumlah suara atau status 'hasVoted'
        else if (newVoting != null && (_activeVoting.value?.totalVotes != newVoting.totalVotes || _activeVoting.value?.hasVoted != newVoting.hasVoted)) {
            _activeVoting.value = newVoting
        }
        // 2. Jika terdeteksi voting baru saja berakhir, jadwalkan satu kali fetch tambahan
        //    setelah jeda singkat. Ini tidak akan mengganggu polling 15 detik utama.
        if (votingJustEnded) {
            viewModelScope.launch {
                delay(500L) // Tunggu 0.5 detik untuk memberi jeda
                Log.d("StreamViewModel", "Voting just ended. Triggering an extra fetch.")
                // Ambil ID dari properti class dan panggil fetchStatus
                currentTrainId?.let { trainId ->
                    currentCarriageId?.let { carriageId ->
                        fetchStatus(trainId, carriageId)
                    }
                }
            }
        }
    }

    /**
     * Mengirim vote, lalu memuat ulang status.
     */
    fun submitVote(optionId: Long) {
        val trainId = currentTrainId ?: return
        val carriageId = currentCarriageId ?: return

        viewModelScope.launch {
            try {
                apiService.submitVote(VoteRequest(optionId))
                // Langsung muat ulang status untuk menampilkan hasil vote terbaru
                fetchStatus(trainId, carriageId)
            } catch (e: Exception) {
                _error.value = "Kesalahan jaringan saat vote: ${e.message}"
            }
        }
    }

    /**
     * Memulai pembaruan data secara periodik.
     */
    private fun startPeriodicUpdates(trainId: Long, carriageId: Long) {
        periodicUpdateJob = viewModelScope.launch {
            while (true) {
                fetchStatus(trainId, carriageId)
                delay(15_000) // Update setiap 15 detik
            }
        }
    }

    /**
     * Memulai timer hitung mundur untuk sesi voting.
     * Logika ini dikembalikan menggunakan SimpleDateFormat agar kompatibel dengan API level 24.
     */
    private fun startVotingCountdown(endTimeString: String) {
        votingCountdownTimer?.cancel()
        votingCountdownTimer = Timer()

        try {
            // 1. Ubah format string waktu agar bisa diparsing
            val parsableDateString = if (endTimeString.length > 6 && endTimeString[endTimeString.length - 3] == ':') {
                endTimeString.substring(0, endTimeString.length - 3) + endTimeString.substring(endTimeString.length - 2)
            } else {
                endTimeString.replace("Z", "+0000") // Juga handle format 'Z'
            }

            // 2. Tentukan format yang benar berdasarkan ada atau tidaknya mikrodetik
            val format = if (parsableDateString.contains(".")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ", Locale.US)
            } else {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
            }

            val endTime = format.parse(parsableDateString) ?: return

            votingCountdownTimer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    val timeDiff = endTime.time - Date().time
                    if (timeDiff <= 0) {
                        _votingTimeLeft.postValue("Selesai")

                        Log.d("StreamViewModel", "Voting countdown finished. Triggering fetch.")
                        // Jalankan fetchStatus di scope ViewModel
                        viewModelScope.launch {
                            currentTrainId?.let { trainId ->
                                currentCarriageId?.let { carriageId ->
                                    fetchStatus(trainId, carriageId)
                                }
                            }
                        }

                        this.cancel()
                    } else {
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff)
                        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeDiff) % 60
                        _votingTimeLeft.postValue(String.format("%02d:%02d", minutes, seconds))
                    }
                }
            }, 0, 1000)

        } catch (e: Exception) {
            Log.e("StreamViewModel", "Gagal memproses waktu voting: '${endTimeString}'", e)
            _votingTimeLeft.postValue("Error")
        }
    }

    override fun onCleared() {
        super.onCleared()
        votingCountdownTimer?.cancel()
        periodicUpdateJob?.cancel()
    }
}
