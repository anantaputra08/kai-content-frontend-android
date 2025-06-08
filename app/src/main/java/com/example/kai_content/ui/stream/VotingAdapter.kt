package com.example.kai_content.ui.stream

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.kai_content.R
import com.example.kai_content.api.VotingOption
import com.example.kai_content.databinding.ItemVotingVideoBinding // <-- Impor View Binding
import java.util.concurrent.TimeUnit

class VotingAdapter(
    // 1. Konstruktor sekarang hanya menerima aksi klik
    private val onVoteClick: (VotingOption) -> Unit
) : RecyclerView.Adapter<VotingAdapter.VotingViewHolder>() {

    // 2. Data disimpan secara internal dan di-update via fungsi
    private var options: List<VotingOption> = emptyList()
    private var hasVoted: Boolean = false

    // 3. ViewHolder sekarang menggunakan View Binding
    inner class VotingViewHolder(private val binding: ItemVotingVideoBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(option: VotingOption, hasVoted: Boolean, isLeading: Boolean) {
            // Gunakan 'binding' untuk mengakses semua view
            binding.textVideoTitle.text = option.content.title
            binding.textChannelName.text = option.content.description
            binding.imgVideoThumbnail.load(option.content.thumbnail) {
                placeholder(R.drawable.placeholder_thumbnail)
                error(R.drawable.error_thumbnail)
            }

            // Format dan tampilkan durasi video
            if (option.content.durationSeconds != null && option.content.durationSeconds > 0) {
                binding.textVideoDuration.isVisible = true
                binding.textVideoDuration.text = formatDuration(option.content.durationSeconds)
            } else {
                binding.textVideoDuration.isVisible = false
            }

            // --- Logika Inti: Menampilkan state vote ---
            if (hasVoted) {
                // Tampilkan hasil jika sudah vote
                showVoteResults(option)
            } else {
                // Tampilkan tombol vote jika belum vote
                showVoteButton(option)
            }

            // Memberi highlight pada opsi dengan vote tertinggi
            if (hasVoted && isLeading) {
                // Anda perlu membuat drawable ini di res/drawable
                binding.root.setBackgroundResource(R.drawable.bg_status_leading_option)
            } else {
                // Anda perlu membuat drawable ini di res/drawable
                binding.root.setBackgroundResource(R.drawable.bg_status_normal_option)
            }
        }

        private fun showVoteResults(option: VotingOption) {
            binding.textVoteCount.text = "${option.voteCount} suara"
            binding.textVotePercentage.text = "${option.votePercentage}%"
            binding.progressVote.progress = option.votePercentage

            // Tampilkan elemen hasil vote dan sembunyikan tombol vote
            binding.textVoteCount.isVisible = true
            binding.textVotePercentage.isVisible = true
            binding.progressVote.isVisible = true
            binding.btnVote.isVisible = false
        }

        private fun showVoteButton(option: VotingOption) {
            // Sembunyikan elemen hasil vote dan tampilkan tombol vote
            binding.textVoteCount.isVisible = false
            binding.textVotePercentage.isVisible = false
            binding.progressVote.isVisible = false
            binding.btnVote.isVisible = true

            // Atur listener klik
            binding.btnVote.setOnClickListener {
                onVoteClick(option)
            }
        }

        private fun formatDuration(totalSeconds: Int): String {
            val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds.toLong())
            val seconds = TimeUnit.SECONDS.toSeconds(totalSeconds.toLong()) % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VotingViewHolder {
        // 4. Inflate layout menggunakan View Binding
        val binding = ItemVotingVideoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VotingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VotingViewHolder, position: Int) {
        val option = options[position]
        // Cek apakah opsi ini adalah yang memiliki vote tertinggi
        val maxVotes = options.maxOfOrNull { it.voteCount } ?: 0
        val isLeading = maxVotes > 0 && option.voteCount == maxVotes
        holder.bind(option, hasVoted, isLeading)
    }

    override fun getItemCount(): Int = options.size

    // 5. Fungsi publik untuk mengupdate data dari Fragment/ViewModel
    fun updateData(newOptions: List<VotingOption>, newHasVoted: Boolean) {
        this.options = newOptions
        this.hasVoted = newHasVoted
        notifyDataSetChanged()
    }
}
