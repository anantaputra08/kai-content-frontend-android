package com.example.kai_content.ui.stream

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kai_content.api.Carriage // <-- Impor data class baru
import com.example.kai_content.api.ContentInfo
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
import java.util.TimeZone
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit

class StreamViewModel : ViewModel() {
    private val apiService: StreamApi by lazy {
        RetrofitClient.instance.create(StreamApi::class.java)
    }

    // BARU: Menyimpan ID carriage saat ini agar bisa digunakan untuk polling
    private var currentCarriageId: Long? = null

    // Data untuk UI
    private val _streamStatus = MutableLiveData<StreamStatusViewModelData?>()
    val streamStatus: LiveData<StreamStatusViewModelData?> = _streamStatus

    private val _activeVoting = MutableLiveData<Voting?>()
    val activeVoting: LiveData<Voting?> = _activeVoting

    private val _carriage = MutableLiveData<Carriage?>() // BARU: LiveData untuk info carriage
    val carriage: LiveData<Carriage?> = _carriage

    private val _votingTimeLeft = MutableLiveData<String>()
    val votingTimeLeft: LiveData<String> = _votingTimeLeft

    private val _isLoading = MutableLiveData<Boolean>(true)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var votingCountdownTimer: Timer? = null
    private var periodicUpdateJob: Job? = null

    // Data class ini hanya untuk merapikan data stream yang akan ditampilkan di UI
    data class StreamStatusViewModelData(
        val displayContent: ContentInfo,
        val streamUrl: String?,
        val isLive: Boolean,
        val scheduledToStart: String?,
        val countdownSeconds: Double?
    )

    /**
     * FUNGSI BARU: Ini adalah titik masuk utama dari UI (Fragment/Activity).
     * Panggil fungsi ini sekali saat layar pertama kali dibuka.
     */
    fun loadDataForCarriage(carriageId: Long) {
        // Hanya jalankan jika ini adalah permintaan pertama atau ID carriage berubah
        if (currentCarriageId == carriageId) return
        currentCarriageId = carriageId

        // Batalkan job lama jika ada, lalu mulai polling baru
        periodicUpdateJob?.cancel()
        startPeriodicUpdates(carriageId)
    }

    /**
     * DIUBAH: Fungsi ini sekarang private dan mengambil data lengkap dalam satu panggilan.
     */
    private fun fetchStatus(carriageId: Long) {
        viewModelScope.launch {
            try {
                // Panggil API dengan carriageId
                val response = apiService.getStreamStatus(carriageId)
                if (response.isSuccessful) {
                    val status = response.body()
                    _error.value = null // Hapus error lama jika sukses

                    // 1. Update info Carriage
                    _carriage.value = status?.carriage

                    // 2. Update status Stream (Live atau Jadwal Berikutnya)
                    handleStreamResponse(status?.nowPlaying)

                    // 3. Update status Voting
                    // LOGIKA BARU: Langsung gunakan data voting dari status, jangan panggil API lain
                    handleVotingResponse(status?.activeVoting)

                } else {
                    _error.value = "Gagal memuat status: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Kesalahan jaringan: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false // Sembunyikan loading indicator setelah fetch pertama selesai
            }
        }
    }

    private fun handleStreamResponse(nowPlaying: com.example.kai_content.api.StreamResponse?) {
        if (nowPlaying?.isLive == true) {
            _streamStatus.value = StreamStatusViewModelData(
                displayContent = nowPlaying.content,
                streamUrl = nowPlaying.streamUrl,
                isLive = true,
                scheduledToStart = null,
                countdownSeconds = null
            )
        } else if (nowPlaying?.nextContent != null) {
            val nextContent = nowPlaying.nextContent
            _streamStatus.value = StreamStatusViewModelData(
                displayContent = ContentInfo(
                    id = nextContent.id,
                    title = nextContent.title,
                    thumbnail = nextContent.thumbnail,
                    description = nextContent.description,
                    durationSeconds = nextContent.durationSeconds
                ),
                streamUrl = null,
                isLive = false,
                scheduledToStart = nextContent.scheduledTime,
                countdownSeconds = nextContent.countdownSeconds
            )
        } else {
            _streamStatus.value = null
        }
    }

    private fun handleVotingResponse(newVoting: Voting?) {
        val oldVoting = _activeVoting.value

        // Kasus 1: Voting telah berakhir (data baru null, data lama ada)
        if (newVoting == null) {
            if (oldVoting != null) {
                _activeVoting.value = null
                votingCountdownTimer?.cancel()
                votingCountdownTimer = null
                _votingTimeLeft.value = "" // Kosongkan teks timer
            }
            return // Selesai, tidak ada yang perlu dilakukan lagi
        }

        // --- Dari sini, kita tahu newVoting tidak null ---

        // Kasus 2: Sesi voting BARU telah dimulai (ID-nya berbeda dari yang lama)
        // Ini adalah logika kunci untuk me-reset timer.
        if (oldVoting?.id != newVoting.id) {
            _activeVoting.value = newVoting // Update UI dengan data voting baru
            startVotingCountdown(newVoting.endTime) // Paksa mulai countdown baru
        }
        // Kasus 3: Ini adalah sesi voting yang sama, tapi ada update (misal: jumlah vote bertambah)
        else if (oldVoting.totalVotes != newVoting.totalVotes) {
            _activeVoting.value = newVoting // Cukup update datanya, jangan sentuh timer
        }
    }

    /**
     * DIHAPUS: Fungsi ini tidak lagi dibutuhkan karena getStreamStatus sudah lengkap.
     */
    // private fun fetchDetailedActiveVoting(votingId: Long) { ... }

    fun submitVote(optionId: Long) {
        val carriageId = currentCarriageId ?: run {
            _error.value = "Carriage ID tidak ditemukan."
            return
        }
        // ... (Fungsi submitVote tidak perlu banyak perubahan, tapi pastikan loading state ditangani)
        // ... (Kode submitVote yang Anda miliki sebelumnya sudah cukup baik)
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.submitVote(VoteRequest(optionId))
                if (!response.isSuccessful) {
                    _error.value = response.errorBody()?.string() ?: "Gagal melakukan vote"
                }
                // Setelah vote, langsung panggil fetchStatus untuk dapat data terbaru
                fetchStatus(carriageId)
            } catch (e: Exception) {
                _error.value = "Kesalahan jaringan saat vote: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun startPeriodicUpdates(carriageId: Long) {
        periodicUpdateJob = viewModelScope.launch {
            while (true) {
                fetchStatus(carriageId)
                delay(15_000) // Poll setiap 15 detik
            }
        }
    }

    private fun startVotingCountdown(endTimeString: String) {
        // Selalu batalkan timer lama sebelum membuat yang baru untuk memastikan kebersihan state
        votingCountdownTimer?.cancel()
        votingCountdownTimer = Timer()

        try {
            // Menggunakan parser yang lebih robust untuk format ISO 8601 dari Laravel
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
            // Mengganti format timezone agar bisa di-parse
            val parsableDateString = endTimeString.replace("Z", "+0000").replace("+07:00", "+0700")
            val endTime = format.parse(parsableDateString) ?: return

            votingCountdownTimer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    val timeDiff = endTime.time - Date().time
                    if (timeDiff <= 0) {
                        _votingTimeLeft.postValue("Selesai")
                        // PENTING: Bersihkan timer setelah selesai
                        cancel()
                        votingCountdownTimer = null
                    } else {
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff)
                        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeDiff) % 60
                        _votingTimeLeft.postValue(String.format("%02d:%02d", minutes, seconds))
                    }
                }
            }, 0, 1000)
        } catch (e: Exception) {
            _error.postValue("Gagal memproses waktu voting: ${e.message}")
            votingCountdownTimer?.cancel()
            votingCountdownTimer = null // Pastikan dibersihkan jika ada error
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        votingCountdownTimer?.cancel()
        periodicUpdateJob?.cancel()
    }
}
