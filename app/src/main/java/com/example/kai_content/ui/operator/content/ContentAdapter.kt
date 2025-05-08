package com.example.kai_content.ui.operator.content

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kai_content.R
import com.example.kai_content.models.content.Content

class ContentAdapter(
    private val contents: List<Content>,
    private val onItemClickListener: (Content) -> Unit
) : RecyclerView.Adapter<ContentAdapter.ContentViewHolder>() {

    inner class ContentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.content_title)
        val description: TextView = view.findViewById(R.id.content_description)
        val thumbnail: ImageView = view.findViewById(R.id.content_thumbnail)

        // Bind data to views and set the click listener
        fun bind(content: Content) {
            title.text = content.title
            description.text = content.description ?: "No description available" // Handle null descriptions gracefully

            Glide.with(thumbnail.context)
                .load(content.thumbnailUrl) // Load thumbnail image using Glide
                .placeholder(R.drawable.placeholder_thumbnail) // Placeholder image
                .error(R.drawable.error_thumbnail) // Error image
                .into(thumbnail)

            itemView.setOnClickListener {
                onItemClickListener(content) // Invoke the click listener
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_content_operator, parent, false)
        return ContentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        holder.bind(contents[position]) // Bind content data to the ViewHolder
    }

    override fun getItemCount(): Int = contents.size
}
