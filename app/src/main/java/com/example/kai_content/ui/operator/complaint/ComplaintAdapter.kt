package com.example.kai_content.ui.operator.complaint

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.kai_content.R
import com.example.kai_content.models.complaint.ComplaintResponse
import java.text.SimpleDateFormat
import java.util.Locale

class ComplaintAdapter(
    private val complaints: List<ComplaintResponse>,
    private val onUpdateClicked: (ComplaintResponse) -> Unit,
    private val onItemClicked: (ComplaintResponse) -> Unit,) :
    RecyclerView.Adapter<ComplaintAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnUpdate: Button = itemView.findViewById(R.id.btnUpdate)
        val tvComplaintId: TextView = itemView.findViewById(R.id.tvComplaintId)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvComplaintDescription: TextView = itemView.findViewById(R.id.tvComplaintDescription)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_complaint_operator, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val complaint = complaints[position]

        holder.tvComplaintId.text = "Complaint #${complaint.id}"

        // Set kategori complaint
        holder.tvCategory.text = complaint.category_complaint?.name ?: "Tidak ada kategori"

        // Set status complaint
        holder.tvStatus.text = complaint.status

        // Set warna background status sesuai dengan statusnya
        when (complaint.status) {
            "open" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
            }
            "in_progress" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_in_progress)
            }
            "resolved" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_resolved)
            }
            else -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status)
            }
        }

        holder.tvComplaintDescription.text = complaint.description

        // Format tanggal created_at ke format yang lebih mudah dibaca
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val date = inputFormat.parse(complaint.created_at)
            holder.tvDate.text = if (date != null) outputFormat.format(date) else complaint.created_at
        } catch (e: Exception) {
            holder.tvDate.text = complaint.created_at
        }

        // Show update button only for open and in_progress status
        if (complaint.status?.lowercase() == "open" || complaint.status?.lowercase() == "in_progress") {
            holder.btnUpdate.visibility = View.VISIBLE

            // Set button text based on current status
            if (complaint.status?.lowercase() == "open") {
                holder.btnUpdate.text = "Start Progress"

                holder.btnUpdate.setOnClickListener {
                    Toast.makeText(holder.itemView.context, "Progress button clicked", Toast.LENGTH_SHORT).show()
                    onUpdateClicked(complaint)
                }
            } else {
                holder.btnUpdate.text = "Resolve"
                holder.btnUpdate.setOnClickListener {
                    Toast.makeText(holder.itemView.context, "Resolve button clicked", Toast.LENGTH_SHORT).show()
                    onUpdateClicked(complaint)
                }
            }
        } else {
            holder.btnUpdate.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onItemClicked(complaint)
        }

    }

    override fun getItemCount(): Int = complaints.size
}
