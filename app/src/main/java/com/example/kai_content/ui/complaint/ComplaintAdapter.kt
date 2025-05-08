package com.example.kai_content.ui.complaint

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kai_content.R
import com.example.kai_content.models.complaint.ComplaintResponse
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Locale

class ComplaintAdapter(private val onItemClick: (ComplaintResponse) -> Unit) :
    ListAdapter<ComplaintResponse, ComplaintAdapter.ComplaintViewHolder>(ComplaintDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_complaint, parent, false)
        return ComplaintViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComplaintViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class ComplaintViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textComplaintId: TextView = itemView.findViewById(R.id.text_complaint_id)
        private val textStatus: TextView = itemView.findViewById(R.id.text_status)
        private val textDescription: TextView = itemView.findViewById(R.id.text_description)
        private val textDate: TextView = itemView.findViewById(R.id.text_date)
        private val btnViewDetails: MaterialButton = itemView.findViewById(R.id.btn_view_details)
        private val divider: View = itemView.findViewById(R.id.divider)
        private val textResolutionLabel: TextView = itemView.findViewById(R.id.text_resolution_label)
        private val textResolution: TextView = itemView.findViewById(R.id.text_resolution)

        fun bind(complaint: ComplaintResponse, onItemClick: (ComplaintResponse) -> Unit) {
            textComplaintId.text = "#${complaint.id}"
            textStatus.text = complaint.status

            // Set status color based on status value
            when (complaint.status.lowercase()) {
                "pending" -> {
                    textStatus.setBackgroundResource(R.drawable.bg_status_pending)
                }
                "in progress" -> {
                    textStatus.setBackgroundResource(R.drawable.bg_status_in_progress)
                }
                "resolved" -> {
                    textStatus.setBackgroundResource(R.drawable.bg_status_resolved)
                }
                "rejected" -> {
                    textStatus.setBackgroundResource(R.drawable.bg_status_rejected)
                }
                else -> {
                    textStatus.setBackgroundResource(R.drawable.bg_status)
                }
            }

            textDescription.text = complaint.description

            // Format date
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            try {
                val date = inputFormat.parse(complaint.created_at)
                textDate.text = "Created: ${date?.let { outputFormat.format(it) }}"
            } catch (e: Exception) {
                textDate.text = "Created: ${complaint.created_at}"
            }

            // Show resolution if available
            if (complaint.resolution_notes?.isNotEmpty() == true &&
                (complaint.status.equals("resolved", ignoreCase = true) ||
                        complaint.status.equals("rejected", ignoreCase = true))) {
                divider.visibility = View.VISIBLE
                textResolutionLabel.visibility = View.VISIBLE
                textResolution.visibility = View.VISIBLE
                textResolution.text = complaint.resolution_notes
            } else {
                divider.visibility = View.GONE
                textResolutionLabel.visibility = View.GONE
                textResolution.visibility = View.GONE
            }

            btnViewDetails.setOnClickListener {
                onItemClick(complaint)
            }
        }
    }

    private class ComplaintDiffCallback : DiffUtil.ItemCallback<ComplaintResponse>() {
        override fun areItemsTheSame(oldItem: ComplaintResponse, newItem: ComplaintResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ComplaintResponse, newItem: ComplaintResponse): Boolean {
            return oldItem == newItem
        }
    }
}
