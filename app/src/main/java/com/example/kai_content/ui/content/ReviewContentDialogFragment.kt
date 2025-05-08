package com.example.kai_content.ui.content

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.kai_content.api.RetrofitClient
import com.example.kai_content.api.ReviewApi
import com.example.kai_content.api.ReviewRequest
import com.example.kai_content.databinding.FragmentReviewContentDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import retrofit2.http.Query

// Fragment arguments
const val ARG_CONTENT_ID = "content_id"

/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    ReviewContentDialogFragment.newInstance(30).show(supportFragmentManager, "dialog")
 * </pre>
 */
class ReviewContentDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentReviewContentDialogBinding? = null
    private val binding get() = _binding!!

    private val reviewApi = RetrofitClient.instance.create(ReviewApi::class.java)

    // Content ID yang akan direview
    private lateinit var contentId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewContentDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Ambil content ID dari arguments
        arguments?.getString(ARG_CONTENT_ID)?.let {
            contentId = it
        } ?: run {
            Toast.makeText(context, "Content ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            dismiss()
            return
        }

        // Cek apakah user sudah memberikan feedback sebelumnya
        checkExistingFeedback()

        binding.btnSubmitFeedback.setOnClickListener {
            submitFeedback()
        }
    }

    private fun checkExistingFeedback() {
        // Ambil token dari SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token == null) {
            Toast.makeText(context, "Token not found. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        // Tampilkan loading state
        binding.progressBar.visibility = View.VISIBLE

        // Menggunakan coroutine untuk panggilan suspend function
        lifecycleScope.launch {
            try {
                val response = reviewApi.checkUserReview("Bearer $token", contentId)
                binding.progressBar.visibility = View.GONE

                Log.d("ReviewContentDialogFragment", "Response: $response")

                // Periksa apakah response memiliki review untuk content yang sama
                if (response.has_review && response.review != null && response.review.content_id.toString() == contentId) {

                    val existingReview = response.review
                    binding.ratingBar.rating = existingReview.rating ?: 0f
                    binding.feedbackInput.setText(existingReview.review ?: "")

                    // Ubah text button
                    binding.btnSubmitFeedback.text = "Update Review"
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("ReviewContentDialogFragment", "Error checking existing review: ${e.message}")
            }
        }
    }

    private fun submitFeedback() {
        // Ambil token dari SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token == null) {
            Toast.makeText(context, "Token not found. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        val rating = binding.ratingBar.rating
        val review = binding.feedbackInput.text.toString()

        val reviewRequest = ReviewRequest(
            content_id = contentId.toInt(),
            rating = if (rating > 0) rating else null, // Kirim hanya jika ada rating
            review = if (review.isNotBlank()) review else null // Kirim hanya jika ada ulasan
        )

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubmitFeedback.isEnabled = false

        // Menggunakan coroutine untuk panggilan suspend function
        lifecycleScope.launch {
            try {
                val response = reviewApi.submitReview("Bearer $token", reviewRequest)
                binding.progressBar.visibility = View.GONE
                binding.btnSubmitFeedback.isEnabled = true

                Toast.makeText(context, "Feedback berhasil dikirim", Toast.LENGTH_SHORT).show()
                dismiss() // Tutup dialog
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnSubmitFeedback.isEnabled = true
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        /**
         * Create a new instance of the dialog with the content ID
         * @param contentId The ID of the content being reviewed
         * @return A new instance of ReviewContentDialogFragment
         */
        fun newInstance(contentId: String): ReviewContentDialogFragment =
            ReviewContentDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CONTENT_ID, contentId)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
