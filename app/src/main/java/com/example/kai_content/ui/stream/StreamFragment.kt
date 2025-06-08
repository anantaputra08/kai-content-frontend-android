package com.example.kai_content.ui.stream

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.kai_content.R
import com.example.kai_content.databinding.FragmentStreamBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import java.util.concurrent.TimeUnit

class StreamFragment : Fragment() {

    private val viewModel: StreamViewModel by viewModels()
    private var _binding: FragmentStreamBinding? = null
    private val binding get() = _binding!!

    private var exoPlayer: ExoPlayer? = null
    private lateinit var votingAdapter: VotingAdapter

    // BARU: Menyimpan carriage ID yang sedang aktif
    private var currentCarriageId: Long = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStreamBinding.inflate(inflater, container, false)
        // Ambil carriageId dari arguments
        currentCarriageId = arguments?.getLong(ARG_CARRIAGE_ID) ?: 1L // Default ke 1 jika tidak ada
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupVotingRecyclerView()
        observeViewModel()

        // DIUBAH: Panggil metode baru dari ViewModel untuk memuat data awal
        viewModel.loadDataForCarriage(currentCarriageId)
    }

    private fun setupVotingRecyclerView() {
        votingAdapter = VotingAdapter(
            onVoteClick = { option ->
                viewModel.submitVote(option.id)
            }
        )
        binding.recyclerVotingVideos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = votingAdapter
        }
    }

    private fun observeViewModel() {
        // Observe stream status
        viewModel.streamStatus.observe(viewLifecycleOwner) { streamStatusData ->
            if (streamStatusData != null) {
                if (streamStatusData.isLive) {
                    // Tampilkan stream yang sedang live
                    binding.textVideoTitle.text = streamStatusData.displayContent.title
                    binding.textVideoStats.text = "LIVE NOW"
                    binding.textVideoStats.setTextColor(resources.getColor(R.color.status_rejected, null))
                    setupExoPlayer(streamStatusData.streamUrl)
                    binding.videoPlayerThumbnail.isVisible = false
                    binding.playerViewExo.isVisible = true
                } else {
                    // Tampilkan jadwal berikutnya
                    binding.textVideoTitle.text = "Berikutnya: ${streamStatusData.displayContent.title}"
                    binding.textVideoStats.text = "Mulai dalam ${formatCountdown(streamStatusData.countdownSeconds?.toLong() ?: 0L)}"
                    binding.textVideoStats.setTextColor(resources.getColor(android.R.color.darker_gray, null))
                    binding.videoPlayerThumbnail.load(streamStatusData.displayContent.thumbnail) {
                        placeholder(R.drawable.placeholder_thumbnail)
                        error(R.drawable.error_thumbnail)
                    }
                    releasePlayer()
                    binding.videoPlayerThumbnail.isVisible = true
                    binding.playerViewExo.isVisible = false
                }
            } else {
                // Tidak ada konten sama sekali
                binding.textVideoTitle.text = "Tidak Ada Konten Saat Ini"
                binding.textVideoStats.text = "Silakan cek kembali nanti."
                binding.textVideoStats.setTextColor(resources.getColor(android.R.color.darker_gray, null))
                binding.videoPlayerThumbnail.load(R.drawable.placeholder_thumbnail)
                releasePlayer()
                binding.videoPlayerThumbnail.isVisible = false
                binding.playerViewExo.isVisible = true
            }
        }

        // Observe active voting
        viewModel.activeVoting.observe(viewLifecycleOwner) { voting ->
            binding.votingSection.isVisible = voting != null
            if (voting != null) {
                binding.textVotingTitle.text = voting.title
                binding.textVotingDescription.text = voting.description
                // LOGIKA BARU: Update adapter dengan data lengkap dari objek voting
                votingAdapter.updateData(voting.options, voting.hasVoted)
            }
        }

        // BARU: Observe info carriage
        viewModel.carriage.observe(viewLifecycleOwner) { carriage ->
            // Misalnya Anda punya TextView untuk nama gerbong:
            // binding.textCarriageName.text = "Anda di: ${carriage?.name}"
        }

        // Observe voting timer
        viewModel.votingTimeLeft.observe(viewLifecycleOwner) { timeLeft ->
            binding.votingTimer.text = timeLeft
        }

        // DIHAPUS: Observer untuk hasVoted tidak lagi diperlukan karena sudah ditangani di observer activeVoting
        // viewModel.hasVoted.observe(...)

        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { errMsg ->
            errMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                // viewModel.clearError() // ViewModel belum memiliki fungsi ini, bisa ditambahkan jika perlu
            }
        }

        // DIUBAH: Gunakan satu observer untuk semua loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarStream.isVisible = isLoading
            // Sembunyikan progress bar voting jika tidak ada voting aktif
            binding.progressBarVoting.isVisible = isLoading && viewModel.activeVoting.value != null
        }
    }

    private fun setupExoPlayer(streamUrl: String?) {
        if (streamUrl.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "URL stream tidak valid", Toast.LENGTH_SHORT).show()
            return
        }
        releasePlayer()
        try {
            exoPlayer = ExoPlayer.Builder(requireContext()).build().apply {
                binding.playerViewExo.player = this
                binding.playerViewExo.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

                val dataSourceFactory = DefaultHttpDataSource.Factory()
                val hlsMediaSource = HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(Uri.parse(streamUrl)))

                setMediaSource(hlsMediaSource)
                prepare()
                playWhenReady = true

                addListener(object : Player.Listener {
                    override fun onIsLoadingChanged(isLoading: Boolean) {
                        binding.progressBarStream.isVisible = isLoading
                    }
                    override fun onPlayerError(error: PlaybackException) {
                        Toast.makeText(context, "Player Error: ${error.message}", Toast.LENGTH_LONG).show()
                        // Coba muat ulang status jika player error
                        viewModel.loadDataForCarriage(currentCarriageId)
                    }
                })
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal setup player: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.streamStatus.value?.isLive == true && exoPlayer == null) {
            setupExoPlayer(viewModel.streamStatus.value?.streamUrl)
        }
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
        _binding = null
    }

    private fun formatCountdown(seconds: Long): String {
        return String.format("%02d:%02d",
            TimeUnit.SECONDS.toMinutes(seconds),
            seconds % 60)
    }

    // BARU: Companion object untuk membuat instance Fragment dengan cara yang aman
    companion object {
        private const val ARG_CARRIAGE_ID = "carriage_id"

        fun newInstance(carriageId: Long): StreamFragment {
            val fragment = StreamFragment()
            val args = Bundle()
            args.putLong(ARG_CARRIAGE_ID, carriageId)
            fragment.arguments = args
            return fragment
        }
    }
}
