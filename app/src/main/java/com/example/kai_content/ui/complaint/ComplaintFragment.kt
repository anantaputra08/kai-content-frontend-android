package com.example.kai_content.ui.complaint

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.kai_content.R
import com.example.kai_content.models.complaint.ComplaintRequest
import com.example.kai_content.models.complaint.ComplaintResponse
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class ComplaintFragment : Fragment() {

    private lateinit var complaintViewModel: ComplaintViewModel
    private lateinit var complaintAdapter: ComplaintAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var textNoComplaints: TextView
    private lateinit var fabAddComplaint: FloatingActionButton
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    // For image selection
    private var selectedImageUri: Uri? = null
    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                selectedImageUri = uri
                // Now you can show a dialog to create the complaint with the selected image
                showCreateComplaintDialog()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_complaint, container, false)

        // Ambil token dari SharedPreferences
        val sharedPreferences =
            requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token == null) {
            Toast.makeText(context, "Token not found. Please login again.", Toast.LENGTH_LONG).show()
            return root
        }

        recyclerView = root.findViewById(R.id.recycler_complaint)
            ?: throw IllegalStateException("RecyclerView not found")
        progressBar = root.findViewById(R.id.progress_complaint)
            ?: throw IllegalStateException("ProgressBar not found")
        textNoComplaints = root.findViewById(R.id.text_no_complaints)
            ?: throw IllegalStateException("TextView for empty complaints not found")
        fabAddComplaint = root.findViewById(R.id.fab_add_complaint)
            ?: throw IllegalStateException("FloatingActionButton not found")
        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh)
            ?: throw IllegalStateException("SwipeRefreshLayout not found")

        setupRecyclerView()
        setupViewModel(token)
        setupListeners(token)

        return root
    }

    private fun setupRecyclerView() {
        complaintAdapter = ComplaintAdapter { complaint ->
            // Handle complaint item click
            navigateToComplaintDetail(complaint)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = complaintAdapter
            layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fade)
        }
    }

    private fun setupViewModel(token: String) {
        complaintViewModel = ViewModelProvider(this, ComplaintViewModelFactory())[ComplaintViewModel::class.java]

        complaintViewModel.complaints.observe(viewLifecycleOwner) { complaints ->
            swipeRefreshLayout.isRefreshing = false
            if (complaints.isNullOrEmpty()) {
                textNoComplaints.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                textNoComplaints.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                complaintAdapter.submitList(complaints)
                recyclerView.scheduleLayoutAnimation()
            }
        }

        complaintViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading && !swipeRefreshLayout.isRefreshing) View.VISIBLE else View.GONE
        }

        complaintViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            swipeRefreshLayout.isRefreshing = false
            message?.let { showSnackbar(it) }
        }

        complaintViewModel.isSubmitting.observe(viewLifecycleOwner) { isSubmitting ->
            // You can show a submission progress if needed
        }

        complaintViewModel.submissionResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                showSnackbar("Complaint submitted successfully")
            }
        }

        // Load complaints
        complaintViewModel.loadComplaints(token)
    }

    private fun setupListeners(token: String) {
        fabAddComplaint.setOnClickListener {
            // First select an image (optional)
            showCreateComplaintDialog()
        }

        swipeRefreshLayout.setOnRefreshListener {
            complaintViewModel.loadComplaints(token)
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getContent.launch(intent)
    }

    private fun showCreateComplaintDialog() {
        val dialog = CreateComplaintDialogFragment.newInstance()
        dialog.show(parentFragmentManager, "CreateComplaintDialog")
    }

    private fun navigateToComplaintDetail(complaint: ComplaintResponse) {
        // TODO: Implement navigation to complaint details screen
        val dialog = ShowComplaintDialogFragment.newInstance(complaint)
        dialog.show(parentFragmentManager, "ShowComplaintDialog")
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    // Implemented from CreateComplaintDialogListener
    fun onComplaintSubmit(request: ComplaintRequest) {
        context?.let { ctx ->
            // Ambil token dari SharedPreferences
            val sharedPreferences =
                ctx.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("auth_token", null)

            if (token != null) {
                complaintViewModel.submitComplaint(ctx, token, request, selectedImageUri)
            } else {
                showSnackbar("Token not found. Please login again.")
            }
        }
        // Reset selected image
        selectedImageUri = null
    }
}
