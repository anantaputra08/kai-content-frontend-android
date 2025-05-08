package com.example.kai_content.ui.operator.complaint

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Locale
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
//import androidx.compose.ui.text.intl.Locale
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.kai_content.R
import com.example.kai_content.api.ComplaintApi
import com.example.kai_content.api.RetrofitClient
import com.example.kai_content.models.complaint.ComplaintRequest
import com.example.kai_content.models.complaint.ComplaintResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.Date

class AssignToMeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var adapter: ComplaintAdapter
    private lateinit var complaintApi: ComplaintApi

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_assign_to_me, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        progressBar = view.findViewById(R.id.progressBar)
        emptyView = view.findViewById(R.id.emptyView)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        complaintApi = RetrofitClient.instance.create(ComplaintApi::class.java)

        // Initialize adapter with empty data and click listener
        adapter = ComplaintAdapter(
            emptyList(),
            onUpdateClicked = { complaint ->
                handleUpdateButtonClick(complaint)
            },
            onItemClicked = { complaint ->
                showComplaintDetails(complaint)
            }
        )
        recyclerView.adapter = adapter

        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            loadComplaints()
        }

        // Load complaints for the first time
        loadComplaints()
    }

    private fun showComplaintDetails(complaint: ComplaintResponse) {
        // Show bottom sheet dialog with complaint details
        val bottomSheetFragment = ShowComplaintDialogFragment.newInstance(complaint)
        bottomSheetFragment.show(parentFragmentManager, "ComplaintDetailsDialog")
    }

    private fun handleUpdateButtonClick(complaint: ComplaintResponse) {
        when (complaint.status.lowercase()) {
            "open" -> {
                // If status is open, update directly to in_progress
                updateComplaintStatus(complaint, "in_progress", null)
            }
            "in_progress" -> {
                // If status is in_progress, show dialog to collect resolution notes
                showResolutionNotesDialog(complaint)
            }
        }
    }

    private fun showResolutionNotesDialog(complaint: ComplaintResponse) {
        val dialogFragment = ResolutionNotesDialogFragment.newInstance(complaint)
        dialogFragment.setOnConfirmClickListener { updatedComplaint, resolutionNotes ->
            // Get current date as resolution date
            val currentDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(Date())

            // Update complaint to resolved with resolution notes
            updateComplaintStatus(updatedComplaint, "resolved", resolutionNotes, currentDateTime)
        }
        dialogFragment.show(parentFragmentManager, "ResolutionNotesDialog")
    }

    private fun updateComplaintStatus(complaint: ComplaintResponse) {
        // Get token from SharedPreferences
        val sharedPreferences =
            requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token == null) {
            Toast.makeText(context, "Token not found. Please login again.", Toast.LENGTH_LONG).show()
            return
        }

        val bearerToken = "Bearer $token"

        // Show loading indicator
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // Create request based on current status
                val newStatus = when (complaint.status.lowercase()) {
                    "open" -> "in_progress"
                    "in_progress" -> "resolved"
                    else -> complaint.status
                }

                // Create the request object with the new status
                val complaintRequest = ComplaintRequest(
                    status = newStatus,
                    // Keep other fields the same
                    assigned_to = complaint.assigned_to?.id,
                    category_complaint_id = complaint.category_complaint_id,
                    description = complaint.description,
                    resolution_date = complaint.resolution_date,
                    resolution_notes = complaint.resolution_notes
                )

                val response = complaintApi.updateComplaint(
                    bearerToken,
                    complaint.id,
                    complaintRequest
                )

                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Status updated to $newStatus",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Reload complaints to refresh the list
                    loadComplaints()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to update status: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressBar.visibility = View.GONE
                }
            } catch (e: IOException) {
                Toast.makeText(
                    requireContext(),
                    "Network error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                progressBar.visibility = View.GONE
            } catch (e: HttpException) {
                Toast.makeText(
                    requireContext(),
                    "HTTP error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                progressBar.visibility = View.GONE
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "An error occurred: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateComplaintStatus(
        complaint: ComplaintResponse,
        newStatus: String,
        resolutionNotes: String?,
        resolutionDate: String? = null
    ) {
        // Get token from SharedPreferences
        val sharedPreferences =
            requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token == null) {
            Toast.makeText(context, "Token not found. Please login again.", Toast.LENGTH_LONG).show()
            return
        }

        val bearerToken = "Bearer $token"

        // Show loading indicator
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // Create the request object with the new status
                val complaintRequest = ComplaintRequest(
                    status = newStatus,
                    // Keep other fields the same
                    assigned_to = complaint.assigned_to?.id,
                    category_complaint_id = complaint.category_complaint_id,
                    description = complaint.description,
                    resolution_date = resolutionDate ?: complaint.resolution_date,
                    resolution_notes = resolutionNotes ?: complaint.resolution_notes
                )

                val response = complaintApi.updateComplaint(
                    bearerToken,
                    complaint.id,
                    complaintRequest
                )

                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Status updated to $newStatus",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Reload complaints to refresh the list
                    loadComplaints()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to update status: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressBar.visibility = View.GONE
                }
            } catch (e: IOException) {
                Toast.makeText(
                    requireContext(),
                    "Network error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                progressBar.visibility = View.GONE
            } catch (e: HttpException) {
                Toast.makeText(
                    requireContext(),
                    "HTTP error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                progressBar.visibility = View.GONE
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "An error occurred: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun loadComplaints() {
        // Get token from SharedPreferences
        val sharedPreferences =
            requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token == null) {
            Toast.makeText(context, "Token not found. Please login again.", Toast.LENGTH_LONG).show()
            swipeRefreshLayout.isRefreshing = false
            return
        }

        val bearerToken = "Bearer $token"

        // Only show progress bar if swipe refresh is not active
        if (!swipeRefreshLayout.isRefreshing) {
            progressBar.visibility = View.VISIBLE
        }
        emptyView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = complaintApi.getComplaintsOperator(bearerToken)
                if (response.isSuccessful) {
                    val complaints = response.body() ?: emptyList()
                    val assignedToMe = complaints.filter { it.assigned_to != null && it.status != "resolved" }

                    // Hide loading indicators
                    progressBar.visibility = View.GONE
                    swipeRefreshLayout.isRefreshing = false

                    if (assignedToMe.isEmpty()) {
                        // Show empty view
                        emptyView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        // Show complaints
                        emptyView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter = ComplaintAdapter(
                            assignedToMe,  // Use the filtered list here
                            onUpdateClicked = { complaint ->
                                handleUpdateButtonClick(complaint)
                            },
                            onItemClicked = { complaint ->
                                showComplaintDetails(complaint)
                            }
                        )
                        recyclerView.adapter = adapter
                    }
                } else {
                    // Handle API error
                    Toast.makeText(requireContext(), "Gagal mengambil data: ${response.message()}", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                    swipeRefreshLayout.isRefreshing = false
                }
            } catch (e: IOException) {
                // Handle network error
                Toast.makeText(requireContext(), "Kesalahan jaringan: ${e.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false
            } catch (e: HttpException) {
                // Handle HTTP error
                Toast.makeText(requireContext(), "Error HTTP: ${e.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false
            } catch (e: Exception) {
                // Handle general error
                Toast.makeText(requireContext(), "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
}
