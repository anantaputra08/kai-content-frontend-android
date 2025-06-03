package com.example.kai_content.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.kai_content.R
import com.example.kai_content.data.models.VotingOption

class VotingAdapter(
    private var votingOptions: List<VotingOption>,
    private val onVoteClick: (VotingOption) -> Unit,
    private var hasVoted: Boolean = false
) : RecyclerView.Adapter<VotingAdapter.VotingViewHolder>() {

    fun updateData(options: List<VotingOption>, hasVoted: Boolean) {
        this.votingOptions = options
        this.hasVoted = hasVoted
        notifyDataSetChanged()
    }

    inner class VotingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val thumbnailImageView: ImageView = itemView.findViewById(R.id.img_video_thumbnail)
        private val titleTextView: TextView = itemView.findViewById(R.id.text_video_title)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.text_video_description)
        private val voteCountTextView: TextView = itemView.findViewById(R.id.text_vote_count)
        private val votePercentageTextView: TextView = itemView.findViewById(R.id.text_vote_percentage)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_votes)
        private val voteButton: View = itemView.findViewById(R.id.btn_vote)

        fun bind(option: VotingOption) {
            // Load thumbnail
            thumbnailImageView.load(option.content.thumbnail) {
                placeholder(R.drawable.placeholder_video)
                error(R.drawable.placeholder_video)
            }

            // Set text data
            titleTextView.text = option.content.title
            descriptionTextView.text = option.content.description
            voteCountTextView.text = "${option.voteCount} votes"
            votePercentageTextView.text = "${option.votePercentage}%"

            // Set progress bar
            progressBar.progress = option.votePercentage

            // Handle vote button state
            if (hasVoted) {
                voteButton.isEnabled = false
                voteButton.alpha = 0.5f
                (voteButton as? TextView)?.text = "Voted"
            } else {
                voteButton.isEnabled = true
                voteButton.alpha = 1.0f
                (voteButton as? TextView)?.text = "Vote"
                voteButton.setOnClickListener {
                    onVoteClick(option)
                }
            }

            // Visual feedback for leading option
            if (option.votePercentage > 0 && option == votingOptions.maxByOrNull { it.voteCount }) {
                itemView.setBackgroundResource(R.drawable.voting_item_leading_background)
            } else {
                itemView.setBackgroundResource(R.drawable.voting_item_background)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VotingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_voting_video, parent, false)
        return VotingViewHolder(view)
    }

    override fun onBindViewHolder(holder: VotingViewHolder, position: Int) {
        holder.bind(votingOptions[position])
    }

    override fun getItemCount(): Int = votingOptions.size
}
