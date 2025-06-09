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

    private var currentCarriageId: Long? = null

    private val _streamStatus = MutableLiveData<StreamStatusViewModelData?>()
    val streamStatus: LiveData<StreamStatusViewModelData?> = _streamStatus

    private val _activeVoting = MutableLiveData<Voting?>()
    val activeVoting: LiveData<Voting?> = _activeVoting

    private val _carriage = MutableLiveData<Carriage?>()
    val carriage: LiveData<Carriage?> = _carriage

    private val _votingTimeLeft = MutableLiveData<String>()
    val votingTimeLeft: LiveData<String> = _votingTimeLeft

    private val _isLoading = MutableLiveData<Boolean>(true)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var votingCountdownTimer: Timer? = null
    private var periodicUpdateJob: Job? = null

    data class StreamStatusViewModelData(
        val displayContent: ContentInfo,
        val streamUrl: String?,
        val isLive: Boolean,
        val scheduledToStart: String?,
        val countdownSeconds: Double?
    )

    // Load data for a specific carriage ID
    fun loadDataForCarriage(carriageId: Long) {
        if (currentCarriageId == carriageId) return
        currentCarriageId = carriageId

        periodicUpdateJob?.cancel()
        startPeriodicUpdates(carriageId)
    }

    // Fetch the current status of the stream for the given carriage ID
    private fun fetchStatus(carriageId: Long) {
        viewModelScope.launch {
            try {
                val response = apiService.getStreamStatus(carriageId)
                if (response.isSuccessful) {
                    val status = response.body()
                    _error.value = null

                    // Set the current carriage ID
                    _carriage.value = status?.carriage

                    // Update the stream status
                    handleStreamResponse(status?.nowPlaying)

                    // Update the active voting
                    handleVotingResponse(status?.activeVoting)

                } else {
                    _error.value = "Gagal memuat status: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Kesalahan jaringan: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Handle the stream response and update the LiveData
    private fun handleStreamResponse(nowPlaying: com.example.kai_content.api.StreamResponse?) {
        val newStatus: StreamStatusViewModelData?

        if (nowPlaying?.isLive == true) {
            newStatus = StreamStatusViewModelData(
                displayContent = nowPlaying.content,
                streamUrl = nowPlaying.streamUrl,
                isLive = true,
                scheduledToStart = null,
                countdownSeconds = null
            )
        } else if (nowPlaying?.nextContent != null) {
            val nextContent = nowPlaying.nextContent
            newStatus = StreamStatusViewModelData(
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
            newStatus = null
        }

        if (_streamStatus.value != newStatus) {
            _streamStatus.value = newStatus
        }
    }

    // Handle the active voting state
    private fun handleVotingResponse(newVoting: Voting?) {
        val oldVoting = _activeVoting.value

        if (newVoting == null) {
            if (oldVoting != null) {
                _activeVoting.value = null
                votingCountdownTimer?.cancel()
                votingCountdownTimer = null
                _votingTimeLeft.value = ""
            }
            return
        }

        // Update voting time left
        if (oldVoting?.id != newVoting.id) {
            _activeVoting.value = newVoting
            startVotingCountdown(newVoting.endTime)
        }

        // Update voting data
        else if (oldVoting.totalVotes != newVoting.totalVotes) {
            _activeVoting.value = newVoting
        }
    }

    // Submit a vote for the given option ID
    fun submitVote(optionId: Long) {
        val carriageId = currentCarriageId ?: run {
            _error.value = "Carriage ID tidak ditemukan."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.submitVote(VoteRequest(optionId))
                if (!response.isSuccessful) {
                    _error.value = response.errorBody()?.string() ?: "Gagal melakukan vote"
                }

                fetchStatus(carriageId)
            } catch (e: Exception) {
                _error.value = "Kesalahan jaringan saat vote: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Start periodic updates every 15 seconds
    private fun startPeriodicUpdates(carriageId: Long) {
        periodicUpdateJob = viewModelScope.launch {
            while (true) {
                fetchStatus(carriageId)
                // Post every 15 seconds
                delay(15_000)
            }
        }
    }

    // Start the voting countdown timer based on the end time string
    private fun startVotingCountdown(endTimeString: String) {
        votingCountdownTimer?.cancel()
        votingCountdownTimer = Timer()

        try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
            val parsableDateString = endTimeString.replace("Z", "+0000").replace("+07:00", "+0700")
            val endTime = format.parse(parsableDateString) ?: return

            votingCountdownTimer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    val timeDiff = endTime.time - Date().time
                    if (timeDiff <= 0) {
                        _votingTimeLeft.postValue("Selesai")
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
            votingCountdownTimer = null
            e.printStackTrace()
        }
    }

    // Clear resources when ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        votingCountdownTimer?.cancel()
        periodicUpdateJob?.cancel()
    }
}
