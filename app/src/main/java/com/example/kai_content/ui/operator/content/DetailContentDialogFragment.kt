package com.example.kai_content.ui.operator.content

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.kai_content.R
import com.example.kai_content.models.content.Content
import com.example.kai_content.databinding.FragmentDetailContentOperatorDialogBinding

class DetailContentDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDetailContentOperatorDialogBinding? = null
    private val binding get() = _binding!!
    private var navigateToEditListener: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailContentOperatorDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve content data from arguments
        val content: Content? = arguments?.getParcelable(ARG_CONTENT)

        content?.let {
            binding.contentTitle.text = it.title
            binding.contentDescription.text = it.description ?: "No description available"
            binding.contentCategories.text = it.categories?.joinToString { category -> category.name } ?: "No categories"
            binding.contentViewCount.text = "Views: ${it.viewCount}"
            binding.contentRank.text = "Rank: ${it.rank ?: "N/A"}"
            binding.contentStatus.text = "${it.status}"
//            val editFragment = EditContenFragment.newInstance(content.id.toString())
//            dismiss() // Close the dialog fragment
//            parentFragmentManager.beginTransaction()
//                .replace(R.id.fragment_edit, editFragment)
//                .addToBackStack(null) // Add to back stack for navigation
//                .commit()

            when (it.status) {
                "pending" -> {
                    binding.contentStatus.setChipBackgroundColorResource(R.color.status_pending)
                }
                "published" -> {
                    binding.contentStatus.setChipBackgroundColorResource(R.color.status_resolved)
                }
                "rejected" -> {
                    binding.contentStatus.setChipBackgroundColorResource(R.color.status_rejected)
                }
                else -> {
                    binding.contentStatus.setChipBackgroundColorResource(R.color.status_default)
                }
            }
            binding.actionButton.setOnClickListener {
                content.let { contentData ->
                    navigateToEditListener?.invoke(contentData.id.toString())
                    dismiss()
                }
            }
        }
    }

    /**
     * Set a listener for when edit button is clicked
     */
    fun setNavigateToEditListener(listener: (String) -> Unit) {
        navigateToEditListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CONTENT = "content"

        fun newInstance(content: Content): DetailContentDialogFragment {
            val fragment = DetailContentDialogFragment()
            val args = Bundle()
            args.putParcelable(ARG_CONTENT, content)
            fragment.arguments = args
            return fragment
        }
    }
}
