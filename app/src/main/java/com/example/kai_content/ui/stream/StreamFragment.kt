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
    private var currentCarriageId: Long = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStreamBinding.inflate(inflater, container, false)
        currentCarriageId = arguments?.getLong(ARG_CARRIAGE_ID) ?: 1
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupVotingRecyclerView()
        observeViewModel()

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
        viewModel.streamStatus.observe(viewLifecycleOwner) { streamStatusData ->
            if (streamStatusData != null) {
                if (streamStatusData.isLive) {
                    binding.textVideoTitle.text = streamStatusData.displayContent.title
                    binding.textVideoStats.text = "LIVE NOW"
                    binding.textVideoStats.setTextColor(resources.getColor(R.color.status_rejected, null))
                    setupExoPlayer(streamStatusData.streamUrl)
                    binding.videoPlayerThumbnail.isVisible = false
                    binding.playerViewExo.isVisible = true

                } else {
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
                votingAdapter.updateData(voting.options, voting.hasVoted)
            }
        }

        // Observe info carriage
        viewModel.carriage.observe(viewLifecycleOwner) { carriage ->
            // binding.textCarriageName.text = "Anda di: ${carriage?.name}"
        }

        // Observe voting timer
        viewModel.votingTimeLeft.observe(viewLifecycleOwner) { timeLeft ->
            binding.votingTimer.text = timeLeft
        }

        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { errMsg ->
            errMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                // viewModel.clearError()
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarStream.isVisible = isLoading
            // Show/hide voting progress bar based on loading state
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
                        // Attempt to reload data for the current carriage
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

    // Factory method to create a new instance of this fragment
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
