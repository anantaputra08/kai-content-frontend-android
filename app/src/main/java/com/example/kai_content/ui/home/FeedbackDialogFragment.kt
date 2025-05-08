package com.example.kai_content.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.kai_content.api.CheckFeedbackResponse
import com.example.kai_content.api.FeedbackApi
import com.example.kai_content.api.FeedbackRequest
import com.example.kai_content.api.FeedbackResponse
import com.example.kai_content.api.RetrofitClient
import com.example.kai_content.databinding.FragmentFeedbackDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val ARG_ITEM_COUNT = "item_count"

class FeedbackDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentFeedbackDialogBinding? = null
    private val binding get() = _binding!!

    private val feedbackApi = RetrofitClient.instance.create(FeedbackApi::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedbackDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

        feedbackApi.checkUserFeedback("Bearer $token").enqueue(object :
            Callback<CheckFeedbackResponse> {
            override fun onResponse(call: Call<CheckFeedbackResponse>, response: Response<CheckFeedbackResponse>) {
                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val checkResponse = response.body()
                    if (checkResponse?.has_feedback == true && checkResponse.feedback != null) {
                        // Jika user sudah memberikan feedback, tampilkan data yang ada
                        val existingFeedback = checkResponse.feedback
                        binding.ratingBar.rating = existingFeedback.rating ?: 0f
                        binding.feedbackInput.setText(existingFeedback.review ?: "")

                        // Ubah text button
                        binding.btnSubmitFeedback.text = "Update Feedback"
                    }
                } else {
                    Toast.makeText(context, "Gagal memeriksa feedback: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CheckFeedbackResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
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

        val feedbackRequest = FeedbackRequest(
            rating = if (rating > 0) rating else null, // Kirim hanya jika ada rating
            review = if (review.isNotBlank()) review else null // Kirim hanya jika ada ulasan
        )

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubmitFeedback.isEnabled = false

        feedbackApi.submitFeedback("Bearer $token", feedbackRequest).enqueue(object :
            Callback<FeedbackResponse> {
            override fun onResponse(call: Call<FeedbackResponse>, response: Response<FeedbackResponse>) {
                binding.progressBar.visibility = View.GONE
                binding.btnSubmitFeedback.isEnabled = true

                if (response.isSuccessful) {
                    Toast.makeText(context, "Feedback berhasil dikirim", Toast.LENGTH_SHORT).show()
                    dismiss() // Tutup dialog
                } else {
                    Toast.makeText(context, "Gagal mengirim feedback: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<FeedbackResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                binding.btnSubmitFeedback.isEnabled = true
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    companion object {
        // Method to create a new instance of FeedbackDialogFragment
        fun newInstance(maxRating: Int): FeedbackDialogFragment {
            val fragment = FeedbackDialogFragment()
            val args = Bundle()
            args.putInt("MAX_RATING", maxRating)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
