package com.example.kai_content.ui.operator.complaint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.kai_content.R
import com.example.kai_content.databinding.FragmentShowComplaintOperatorDialogBinding
import com.example.kai_content.models.complaint.ComplaintResponse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ShowComplaintDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentShowComplaintOperatorDialogBinding? = null
    private val binding get() = _binding!!
    private var complaint: ComplaintResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowComplaintOperatorDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve complaint from arguments
        arguments?.getParcelable<ComplaintResponse>(ARG_COMPLAINT)?.let {
            complaint = it
            displayComplaintDetails(it)
        }

        // Set up action buttons
//        binding.btnClose.setOnClickListener {
//            dismiss()
//        }
    }

    private fun displayComplaintDetails(complaint: ComplaintResponse) {
        with(binding) {
            tvComplaintId.text = "Complaint #${complaint.id}"
            tvCategory.text = complaint.category_complaint?.name ?: "Unknown"
            tvStatus.text = complaint.status
            tvDescription.text = complaint.description
            tvDate.text = complaint.created_at

            // User info would need to be fetched separately since it's not part of the complaint
            // For now, we'll hide this section
            userInfoSection.visibility = View.GONE

            // Show attachment if available
            if (complaint.attachmentUrl != null) {
                tvAttachmentLabel.visibility = View.VISIBLE
                tvAttachment.visibility = View.VISIBLE
                // Menggunakan Glide untuk memuat gambar
                val imageView = tvAttachment as ImageView // Cast ke ImageView sesuai perubahan layout

                Glide.with(requireContext())
                    .load(complaint.attachmentUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(
                        RequestOptions()
                        .placeholder(R.drawable.placeholder_thumbnail) // Ganti dengan placeholder image Anda
                        .error(R.drawable.error_thumbnail)) // Ganti dengan error image Anda
                    .into(imageView)
            } else {
                tvAttachmentLabel.visibility = View.GONE
                tvAttachment.visibility = View.GONE
            }

            // Set assigned operator info if available
            if (complaint.assigned_to != null) {
                tvAssignedOperator.text = complaint.assigned_to.name
                tvAssignedOperatorEmail.text = complaint.assigned_to.email
                tvAssignedOperatorPhone.text = complaint.assigned_to.phone ?: "N/A"
                layoutAssignedOperator.visibility = View.VISIBLE
            } else {
                layoutAssignedOperator.visibility = View.GONE
            }

            // Show resolution notes if available
            if (complaint.resolution_notes != null && complaint.status == "resolved") {
                tvResolutionLabel.visibility = View.VISIBLE
                tvResolutionNotes.visibility = View.VISIBLE
                tvResolutionDate.visibility = View.VISIBLE

                tvResolutionNotes.text = complaint.resolution_notes
                tvResolutionDate.text = "Resolved on: ${complaint.resolution_date ?: complaint.updated_at}"
            } else {
                tvResolutionLabel.visibility = View.GONE
                tvResolutionNotes.visibility = View.GONE
                tvResolutionDate.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_COMPLAINT = "complaint"

        fun newInstance(complaint: ComplaintResponse): ShowComplaintDialogFragment {
            val fragment = ShowComplaintDialogFragment()
            val args = Bundle()
            args.putParcelable(ARG_COMPLAINT, complaint)
            fragment.arguments = args
            return fragment
        }
    }
}
