package com.example.kai_content.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.kai_content.R
import com.example.kai_content.databinding.ItemVideoBinding
import com.example.kai_content.models.content.Content

class ContentAdapter(private val onItemClick: (Content) -> Unit) :
    ListAdapter<Content, ContentAdapter.ContentViewHolder>(ContentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val binding = ItemVideoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        val content = getItem(position)
        holder.bind(content)
    }

    inner class ContentViewHolder(private val binding: ItemVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(content: Content) {
            binding.textTitle.text = content.title
            binding.textChannel.text = content.categories?.firstOrNull()?.name ?: "Uncategorized"
            binding.textDetails.text = "${formatViewCount(content.viewCount)} views • ${content.getTimeAgo()}"

            Glide.with(binding.imageThumbnail)
                .load(content.thumbnailUrl)
                .centerCrop()
                .placeholder(R.drawable.placeholder_thumbnail)
                .error(R.drawable.error_thumbnail)
                .into(binding.imageThumbnail)
        }

        private fun formatViewCount(viewCount: Int): String {
            return when {
                viewCount < 1000 -> viewCount.toString()
                viewCount < 1_000_000 -> String.format("%.1fK", viewCount / 1000f)
                else -> String.format("%.1fM", viewCount / 1_000_000f)
            }
        }
    }

    class ContentDiffCallback : DiffUtil.ItemCallback<Content>() {
        override fun areItemsTheSame(oldItem: Content, newItem: Content): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Content, newItem: Content): Boolean {
            return oldItem == newItem
        }
    }
}
