package com.example.kai_content.ui.stream

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.kai_content.R
import com.example.kai_content.api.VotingOption
import com.example.kai_content.databinding.ItemVotingVideoBinding
import java.util.concurrent.TimeUnit

class VotingAdapter(
    private val onVoteClick: (VotingOption) -> Unit
) : RecyclerView.Adapter<VotingAdapter.VotingViewHolder>() {

    private var options: List<VotingOption> = emptyList()
    private var hasVoted: Boolean = false

    inner class VotingViewHolder(private val binding: ItemVotingVideoBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(option: VotingOption, hasVoted: Boolean, isLeading: Boolean) {
            binding.textVideoTitle.text = option.content.title
            binding.textChannelName.text = option.content.description
            binding.imgVideoThumbnail.load(option.content.thumbnail) {
                placeholder(R.drawable.placeholder_thumbnail)
                error(R.drawable.error_thumbnail)
            }

            if (option.content.durationSeconds != null && option.content.durationSeconds > 0) {
                binding.textVideoDuration.isVisible = true
                binding.textVideoDuration.text = formatDuration(option.content.durationSeconds)
            } else {
                binding.textVideoDuration.isVisible = false
            }

            if (hasVoted) {
                showVoteResults(option)
            } else {
                showVoteButton(option)
            }

            if (hasVoted && isLeading) {
                binding.root.setBackgroundResource(R.drawable.bg_status_leading_option)
            } else {
                binding.root.setBackgroundResource(R.drawable.bg_status_normal_option)
            }
        }

        private fun showVoteResults(option: VotingOption) {
            binding.textVoteCount.text = "${option.voteCount} suara"
            binding.textVotePercentage.text = "${option.votePercentage.toInt()}%" // Diubah ke Int untuk tampilan
            // -- SOLUSI: Mengubah Double menjadi Int sebelum diberikan ke ProgressBar --
            binding.progressVote.progress = option.votePercentage.toInt()
            binding.textVoteCount.isVisible = true
            binding.textVotePercentage.isVisible = true
            binding.progressVote.isVisible = true
            binding.btnVote.isVisible = false
        }

        private fun showVoteButton(option: VotingOption) {
            binding.textVoteCount.isVisible = false
            binding.textVotePercentage.isVisible = false
            binding.progressVote.isVisible = false
            binding.btnVote.isVisible = true

            binding.btnVote.setOnClickListener {
                onVoteClick(option)
            }
        }

        private fun formatDuration(totalSeconds: Int): String {
            val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds.toLong())
            val seconds = totalSeconds.toLong() % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VotingViewHolder {
        val binding = ItemVotingVideoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VotingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VotingViewHolder, position: Int) {
        val option = options[position]
        val maxVotes = options.maxOfOrNull { it.voteCount } ?: 0
        val isLeading = maxVotes > 0 && option.voteCount == maxVotes
        holder.bind(option, hasVoted, isLeading)
    }

    override fun getItemCount(): Int = options.size

    fun updateData(newOptions: List<VotingOption>, newHasVoted: Boolean) {
        this.options = newOptions
        this.hasVoted = newHasVoted
        notifyDataSetChanged()
    }
}
