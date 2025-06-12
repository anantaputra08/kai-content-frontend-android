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

class StreamFragment : Fragment() {

    private val viewModel: StreamViewModel by viewModels()
    private var _binding: FragmentStreamBinding? = null
    private val binding get() = _binding!!
    private var exoPlayer: ExoPlayer? = null
    private lateinit var votingAdapter: VotingAdapter

    private var currentTrainId: Long = -1
    private var currentCarriageId: Long = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStreamBinding.inflate(inflater, container, false)
        // Retrieve both IDs from arguments
        arguments?.let {
            currentTrainId = it.getLong(ARG_TRAIN_ID)
            currentCarriageId = it.getLong(ARG_CARRIAGE_ID)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupVotingRecyclerView()
        observeViewModel()

        if (currentTrainId != -1L && currentCarriageId != -1L) {
            viewModel.loadDataForLocation(currentTrainId, currentCarriageId)
        } else {
            Toast.makeText(requireContext(), "ID Kereta atau Gerbong tidak valid.", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupVotingRecyclerView() {
        votingAdapter = VotingAdapter { option -> viewModel.submitVote(option.id) }
        binding.recyclerVotingVideos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = votingAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.streamStatus.observe(viewLifecycleOwner) { streamStatusData ->
            if (streamStatusData != null && streamStatusData.isLive) {
                binding.textVideoTitle.text = streamStatusData.displayContent.title
                binding.textVideoStats.text = "LIVE NOW"
                binding.textVideoStats.setTextColor(resources.getColor(R.color.status_rejected, null))
                setupExoPlayer(streamStatusData.displayContent.streamUrl)
                binding.videoPlayerThumbnail.isVisible = false
                binding.playerViewExo.isVisible = true
            } else {
                binding.textVideoTitle.text = streamStatusData?.displayContent?.title ?: "Tidak Ada Konten Saat Ini"
                binding.textVideoStats.text = if (streamStatusData != null) "Akan Datang" else "Silakan cek kembali nanti."
                binding.textVideoStats.setTextColor(resources.getColor(android.R.color.darker_gray, null))
                binding.videoPlayerThumbnail.load(streamStatusData?.displayContent?.thumbnail ?: R.drawable.placeholder_thumbnail)
                releasePlayer()
                binding.videoPlayerThumbnail.isVisible = true
                binding.playerViewExo.isVisible = false
            }
        }

        viewModel.activeVoting.observe(viewLifecycleOwner) { voting ->
            binding.votingSection.isVisible = voting != null
            if (voting != null) {
                binding.textVotingTitle.text = voting.title
                binding.textVotingDescription.text = voting.description
                votingAdapter.updateData(voting.options, voting.hasVoted)
            }
        }

        viewModel.locationInfo.observe(viewLifecycleOwner) { location ->
            binding.textLocationInfo.text = if(location != null) "Anda di: ${location.trainName} - ${location.carriageName}" else "Memuat lokasi..."
        }

        viewModel.votingTimeLeft.observe(viewLifecycleOwner) { timeLeft ->
            binding.votingTimer.text = timeLeft
        }

        viewModel.error.observe(viewLifecycleOwner) { errMsg ->
            errMsg?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show() }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarStream.isVisible = isLoading
        }
    }

    private fun setupExoPlayer(streamUrl: String?) {
        if (streamUrl.isNullOrEmpty()) return
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
                        binding.progressBarStream.isVisible = isLoading && isPlaying
                    }
                    override fun onPlayerError(error: PlaybackException) {
                        Toast.makeText(context, "Player Error: ${error.message}", Toast.LENGTH_LONG).show()
                        viewModel.loadDataForLocation(currentTrainId, currentCarriageId)
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
            setupExoPlayer(viewModel.streamStatus.value?.displayContent?.streamUrl)
        }
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_TRAIN_ID = "train_id"
        const val ARG_CARRIAGE_ID = "carriage_id"
    }
}
