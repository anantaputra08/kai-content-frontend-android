package com.example.kai_content.ui.complaint

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.kai_content.R
import com.example.kai_content.databinding.FragmentShowComplaintDialogBinding
import com.example.kai_content.models.complaint.ComplaintResponse

class ShowComplaintDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentShowComplaintDialogBinding? = null
    private val binding get() = _binding!!

    private var complaint: ComplaintResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            complaint = it.getParcelable(ARG_COMPLAINT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowComplaintDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadComplaintDetails()
    }

    private fun loadComplaintDetails() {
        complaint?.let { complaint ->
            // Header card
            binding.textComplaintTitle.text = "Complaint #${complaint.id}"
            binding.textComplaintDescription.text = complaint.description

            // Details card
            binding.textComplaintCategory.text = complaint.category_complaint?.name ?: "Unknown"

            // Set status with appropriate color
            binding.textComplaintStatus.text = complaint.status
            setStatusColor(complaint.status)

            // Resolution card
            binding.textResolutionDate.text = complaint.resolution_date ?: "Not resolved yet"
            binding.textResolutionNotes.text = complaint.resolution_notes ?: "No resolution notes available"

            // Support agent card
            complaint.assigned_to?.let { assignedTo ->
                binding.textAssignedTo.text = assignedTo.name
                binding.textAgentEmail.text = assignedTo.email

                // Load profile picture if available
                if (!assignedTo.profile_picture.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(assignedTo.profilePictureUrl)
                        .placeholder(R.drawable.ic_profile_placeholder) // You need this drawable
                        .error(R.drawable.ic_profile_placeholder) // You need this drawable
                        .circleCrop()
                        .into(binding.imageAgent)
                }
            } ?: run {
                binding.textAssignedTo.text = "Not assigned"
                binding.textAgentEmail.text = "N/A"
            }

            if (complaint.attachmentUrl != null) {
                // Load attachment if available
                Glide.with(requireContext())
                    .load(complaint.attachmentUrl)
                    .placeholder(R.drawable.bg_complaint_id)
                    .error(R.drawable.error_thumbnail)
                    .into(binding.imageAttachment)
            } else {
                binding.imageAttachment.visibility = View.GONE
            }

            // Show/hide resolution card if there's no resolution yet
            if (complaint.resolution_date.isNullOrEmpty() && complaint.resolution_notes.isNullOrEmpty()) {
                binding.cardResolution.visibility = View.GONE
            } else {
                binding.cardResolution.visibility = View.VISIBLE
            }
        }
    }

    private fun setStatusColor(status: String) {
        val colorResId = when (status.lowercase()) {
            "open" -> R.color.status_pending  // You need to define these colors
            "in_progress" -> R.color.status_in_progress
            "resolved" -> R.color.status_resolved
            "rejected" -> R.color.status_rejected
            else -> R.color.status_default
        }

        binding.textComplaintStatus.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), colorResId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_COMPLAINT = "complaint"

        fun newInstance(complaint: ComplaintResponse): ShowComplaintDialogFragment =
            ShowComplaintDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_COMPLAINT, complaint)
                }
            }
    }
}
