package com.example.kai_content.ui.content

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kai_content.R
import com.example.kai_content.models.content.Content
import com.example.kai_content.ui.home.ContentAdapter
import com.example.kai_content.ui.operator.content.DetailContentDialogFragment
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.progressindicator.CircularProgressIndicator

class ShowContentFragment : Fragment() {

    companion object {
        fun newInstance() = ShowContentFragment()
        private const val ARG_CONTENT_ID = "content_id"

        fun newInstance(contentId: String): ShowContentFragment {
            val fragment = ShowContentFragment()
            val args = Bundle()
            args.putString(ARG_CONTENT_ID, contentId)
            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel: ShowContentViewModel by viewModels()
    private var player: ExoPlayer? = null
    private lateinit var contentAdapter: ContentAdapter

    // UI elements
    private lateinit var videoContainer: View
    private lateinit var playerView: PlayerView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var textVideoTitle: TextView
    private lateinit var textVideoStats: TextView
    private lateinit var btnLike: View
    private lateinit var likeIcon: ImageView
    private lateinit var textLikeCount: TextView
    private lateinit var btnDislike: View
    private lateinit var dislikeIcon: ImageView
    private lateinit var textDislikeCount: TextView
    private lateinit var btnFavorite: View
    private lateinit var favoriteText: TextView
    private lateinit var btnReview: View
    private lateinit var textReview: TextView
    private lateinit var recyclerRelatedVideos: RecyclerView
    private lateinit var progressIndicator: CircularProgressIndicator
    private var isLike = false
    private var isDisliked = false
    private var likeCount = 0

    // Mini player elements
    private lateinit var miniPlayerContainer: View
    private lateinit var miniThumbnail: ImageView
    private lateinit var miniTitle: TextView
    private lateinit var miniChannel: TextView
    private lateinit var miniPlayPause: ImageButton
    private lateinit var miniClose: ImageButton

    // State variables for player
    private var playWhenReady = true
    private var currentPosition = 0L
    private var isVideoInitialized = false
    private var contentId: String = ""
    private var token: String = ""
    private var isTrackingWatchTime = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get token from SharedPreferences directly
        val sharedPreferences =
            requireContext().getSharedPreferences("AppPreferences", android.content.Context.MODE_PRIVATE)
        token = sharedPreferences.getString("auth_token", "") ?: ""

        // Get content ID from navigation arguments
        contentId = arguments?.getString("content_id") ?: ""

        if (contentId.isEmpty()) {
            Toast.makeText(context, "Content ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "Token not found. Please login again.", Toast.LENGTH_LONG).show()
            return
        }

        if (contentId.isNotEmpty() && token.isNotEmpty()) {
            viewModel.getContent("Bearer $token", contentId)
            viewModel.checkLikeDislike("Bearer $token", contentId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_show_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        initializeViews(view)

        // Initialize related videos adapter
        setupRelatedVideosRecyclerView()

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressIndicator.isVisible = isLoading
        }

        // Observe error state
        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                Log.e("ShowContentFragment", "Error: $it")
            }
        }

        // Observe content data
        viewModel.content.observe(viewLifecycleOwner) { content ->
            content?.let {
                updateUI(it)
                initializePlayer(it.videoUrl)
            }
        }

        // Observe related videos
        viewModel.contents.observe(viewLifecycleOwner) { contents ->
            contentAdapter.submitList(contents)
        }

        // Observe like/dislike state
//        viewModel.likeDislikeData.observe(viewLifecycleOwner) { data ->
//            isLike = data?.is_like ?: false
//            updateLikeDislikeUI() // ubah tampilan sesuai status like
//        }

        // Cek status favorit
        if (contentId.isNotEmpty()) {
            viewModel.checkFavorite("Bearer $token", contentId)
            viewModel.checkLikeDislike("Bearer $token", contentId)

        }

//        setupLikeDislikeButtons()
    }

    private fun initializeViews(view: View) {
        // Main player views
        videoContainer = view.findViewById(R.id.video_container)

        // Add progress indicator
        progressIndicator = CircularProgressIndicator(requireContext()).apply {
            id = View.generateViewId()
            isIndeterminate = true
            visibility = View.GONE
        }

        // Use FrameLayout.LayoutParams since videoContainer is a FrameLayout
        (videoContainer as ViewGroup).addView(progressIndicator, android.widget.FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = android.view.Gravity.CENTER
        })

        // Replace ImageView with PlayerView
        val originalVideoPlayer = view.findViewById<ImageView>(R.id.video_player)
        val parentLayout = originalVideoPlayer.parent as ViewGroup
        val playerViewIndex = parentLayout.indexOfChild(originalVideoPlayer)

        // Create PlayerView programmatically
        playerView = PlayerView(requireContext())
        playerView.id = R.id.video_player
        playerView.layoutParams = originalVideoPlayer.layoutParams
        playerView.useController = true

        // Replace ImageView with PlayerView
        parentLayout.removeView(originalVideoPlayer)
        parentLayout.addView(playerView, playerViewIndex)

        btnPlayPause = view.findViewById(R.id.btn_play_pause)
        textVideoTitle = view.findViewById(R.id.text_video_title)
        textVideoStats = view.findViewById(R.id.text_video_stats)

        // Action buttons
        btnLike = view.findViewById(R.id.btn_like)
        btnDislike = view.findViewById(R.id.btn_dislike)
        btnFavorite = view.findViewById(R.id.btn_favorite)
        favoriteText = view.findViewById(R.id.favorite_text)

        likeIcon = btnLike.findViewById(R.id.like_icon)
        textLikeCount = btnLike.findViewById(R.id.like_count)
        dislikeIcon = btnDislike.findViewById(R.id.dislike_icon)
        textDislikeCount = btnDislike.findViewById(R.id.dislike_count)

        btnReview = view.findViewById(R.id.btn_review)
        textReview = view.findViewById(R.id.review_text)

        // Recycler view for related videos
        recyclerRelatedVideos = view.findViewById(R.id.recycler_related_videos)

        // Mini player
        miniPlayerContainer = view.findViewById(R.id.mini_player_container)
        miniThumbnail = view.findViewById(R.id.mini_thumbnail)
        miniTitle = view.findViewById(R.id.mini_title)
        miniChannel = view.findViewById(R.id.mini_channel)
        miniPlayPause = view.findViewById(R.id.mini_play_pause)
        miniClose = view.findViewById(R.id.mini_close)

        // Set up button click listeners
        setupClickListeners()
    }

    private fun setupRelatedVideosRecyclerView() {
        contentAdapter = ContentAdapter { relatedContent ->
            // When a related video is clicked
            playRelatedContent(relatedContent)
        }

        recyclerRelatedVideos.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = contentAdapter
        }
    }

    private fun playRelatedContent(content: Content) {
        // Save current state before playing new content
        releasePlayer()

        // Navigate to the same fragment but with new content ID
        val bundle = bundleOf("content_id" to content.id.toString())
        findNavController().navigate(R.id.action_showContentFragment_self, bundle)
    }

    private fun setupClickListeners() {
        btnPlayPause.setOnClickListener {
            togglePlayback()
        }

        btnLike.setOnClickListener {
            if (contentId.isNotEmpty()) {
                handleLikeClick()
            }
        }

        btnDislike.setOnClickListener {
            if (contentId.isNotEmpty()) {
                handleDislikeClick()
            }
        }

        btnFavorite.setOnClickListener {
            if (contentId.isNotEmpty()) {
                viewModel.toggleFavorite("Bearer $token", contentId)
            }
        }

        btnReview.setOnClickListener {
            if (contentId.isNotEmpty()) {
                val reviewDialog = ReviewContentDialogFragment.newInstance(contentId)
                reviewDialog.show(requireActivity().supportFragmentManager, "ReviewContentDialog")
            }
        }

        // Mini player controls
        miniPlayPause.setOnClickListener {
            togglePlayback()
        }

        miniClose.setOnClickListener {
            releasePlayer()
            // Optionally navigate back
            requireActivity().onBackPressed()
        }
    }

    fun handleLikeClick() {
        val wasLiked = isLike

        if (wasLiked) {
            // Toggle like OFF
            isLike = false
            viewModel.setReaction(token, contentId, "like", false)
        } else {
            // Toggle like ON
            isLike = true

            // Jika sebelumnya dislike aktif, matikan dislike
            if (isDisliked) {
                isDisliked = false
            }

            viewModel.setReaction(token, contentId, "like", true)
        }

        Log.d("LikeDislike", "on handleLikeClick isLike: $isLike, isDisliked: $isDisliked")

        // Update UI
        updateLikeDislikeUI()
    }

    /**
     * Menangani klik pada tombol dislike
     */
    fun handleDislikeClick() {
        val wasDisliked = isDisliked

        if (wasDisliked) {
            // Toggle dislike OFF
            isDisliked = false
            viewModel.setReaction(token, contentId, "dislike", false)
        } else {
            // Toggle dislike ON
            isDisliked = true

            // Jika sebelumnya like aktif, matikan like
            if (isLike) {
                isLike = false
            }

            viewModel.setReaction(token, contentId, "dislike", true)
        }

        Log.d("LikeDislike", "on handleDislikeClick isLike: $isLike, isDisliked: $isDisliked")

        // Update UI
        updateLikeDislikeUI()
    }

    private fun updateLikeDislikeUI() {
        Log.d("LikeDislike", "on fun updateLikeDislike isLike: $isLike, isDisliked: $isDisliked")
        if (isLike) {
            likeIcon.setImageResource(R.drawable.ic_thumb_up_filled)
            likeIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary_color))
            textLikeCount.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_color))
        } else {
            likeIcon.setImageResource(R.drawable.ic_thumb_up_outline)
            likeIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.secondary_text))
            textLikeCount.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary_text))
        }

        // Update like count text
        textLikeCount.text = formatCount(likeCount)

        // Update dislike button
        if (isDisliked) {
            dislikeIcon.setImageResource(R.drawable.ic_thumb_down_filled)
            dislikeIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary_color))
        } else {
            dislikeIcon.setImageResource(R.drawable.ic_thumb_down_outline)
            dislikeIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.secondary_text))
        }
    }

    // Helper function to format count like YouTube (1.2K, 15K, etc.)
    private fun formatCount(count: Int): String {
        return when {
            count < 1000 -> count.toString()
            count < 10000 -> String.format("%.1fK", count / 1000f).replace(".0K", "K")
            count < 1000000 -> "${count / 1000}K"
            else -> String.format("%.1fM", count / 1000000f).replace(".0M", "M")
        }
    }

    private fun View.animateClick() {
        this.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                this.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    private fun updateUI(content: Content) {
        // Set title and stats
        textVideoTitle.text = content.title
        textVideoStats.text = "${content.viewCount} views • ${content.getTimeAgo()}"
        // Format the like count
        likeCount = content.like
        textLikeCount.text = formatCount(likeCount)

        // Set initial state of buttons
        isLike = content.isLike ?: false
        isDisliked = content.isDislike ?: false
        updateLikeDislikeUI()

        // Set favorite button state
        val favoriteImageView = btnFavorite.findViewById<ImageView>(R.id.favorite_icon)
        val favoriteTextView = favoriteText.findViewById<TextView>(R.id.favorite_text)

        if (content.isFavorite) {
            favoriteImageView.setColorFilter(ContextCompat.getColor(context, R.color.primary_color), PorterDuff.Mode.SRC_IN)
            favoriteTextView.text = getString(R.string.remove_from_favorites)
        } else {
            favoriteImageView.setColorFilter(ContextCompat.getColor(context, R.color.gray), PorterDuff.Mode.SRC_IN)
            favoriteTextView.text = getString(R.string.add_to_favorites)
        }

        // Update mini player info
        miniTitle.text = content.title
        miniChannel.text = content.categories?.firstOrNull()?.name ?: ""

        // Load thumbnail for mini player
        content.thumbnailUrl.let { url ->
            Glide.with(requireContext())
                .load(url)
                .placeholder(R.drawable.ic_launcher_background)
                .into(miniThumbnail)
        }
    }

    private fun initializePlayer(videoUrl: String) {
        if (isVideoInitialized) return

        context?.let { ctx ->
            player = ExoPlayer.Builder(ctx).build().apply {
                playerView.player = this

                // Create media item from URL
                val mediaItem = MediaItem.fromUri(videoUrl)
                setMediaItem(mediaItem)

                // Restore state if needed
                playWhenReady = this@ShowContentFragment.playWhenReady
                seekTo(currentPosition)
                prepare()

                // Add listener for player state changes
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        updatePlayPauseButton(playbackState)

                        // Start tracking watch time when video starts playing
                        if (playbackState == Player.STATE_READY && isPlaying && !isTrackingWatchTime) {
                            startWatchTimeTracking()
                        } else if (playbackState == Player.STATE_ENDED) {
                            stopWatchTimeTracking()
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        updatePlayPauseButton(player?.playbackState ?: Player.STATE_IDLE)

                        if (isPlaying) {
                            startWatchTimeTracking()
                        } else {
                            stopWatchTimeTracking()
                        }
                    }
                })
            }

            isVideoInitialized = true
            updatePlayPauseButton(Player.STATE_BUFFERING)
        }
    }

    private fun updatePlayPauseButton(playbackState: Int) {
        val isPlaying = player?.isPlaying ?: false

        // Update main player button
        btnPlayPause.setImageResource(
            if (isPlaying) android.R.drawable.ic_media_pause
            else android.R.drawable.ic_media_play
        )

        // Update mini player button
        miniPlayPause.setImageResource(
            if (isPlaying) android.R.drawable.ic_media_pause
            else android.R.drawable.ic_media_play
        )

        // Show/hide play button based on state
        if (playbackState == Player.STATE_BUFFERING ||
            playbackState == Player.STATE_READY && isPlaying) {
            btnPlayPause.visibility = View.GONE
        } else {
            btnPlayPause.visibility = View.VISIBLE
        }
    }

    private fun togglePlayback() {
        player?.let {
            if (it.isPlaying) {
                it.pause()
                stopWatchTimeTracking()
            } else {
                it.play()
                startWatchTimeTracking()
            }
        }
    }

    private fun startWatchTimeTracking() {
        if (!isTrackingWatchTime && contentId.isNotEmpty()) {
            viewModel.startWatchTimeTracking("Bearer $token", contentId)
            isTrackingWatchTime = true
        }
    }

    private fun stopWatchTimeTracking() {
        if (isTrackingWatchTime) {
            viewModel.stopWatchTimeTracking()
            isTrackingWatchTime = false
        }
    }

    private fun releasePlayer() {
        stopWatchTimeTracking()

        player?.let {
            // Save player state
            playWhenReady = it.playWhenReady
            currentPosition = it.currentPosition

            // Release resources
            it.release()
            player = null
            isVideoInitialized = false
        }
    }

    override fun onResume() {
        super.onResume()
        if (player == null && viewModel.content.value != null) {
            viewModel.content.value?.videoUrl?.let { initializePlayer(it) }
        }
    }

    override fun onPause() {
        super.onPause()
        stopWatchTimeTracking()
        releasePlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopWatchTimeTracking()
        releasePlayer()
    }
}
