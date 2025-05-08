package com.example.kai_content.ui.operator.complaint

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.kai_content.R
import com.example.kai_content.api.ComplaintApi
import com.example.kai_content.api.RetrofitClient
import com.example.kai_content.models.complaint.ComplaintResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class RiwayatFragment : Fragment() {

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
        return inflater.inflate(R.layout.fragment_riwayat, container, false)
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

        // Set up API client
        complaintApi = RetrofitClient.instance.create(ComplaintApi::class.java)

        // Initialize adapter with empty data
        adapter = ComplaintAdapter(
            emptyList(),  // Use the filtered list here
            onUpdateClicked = { complaint ->
                // updateComplaintStatus(complaint)
            },
            onItemClicked = { complaint ->
                showComplaintDetails(complaint)
            }
        )
        recyclerView.adapter = adapter

        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            loadRiwayat()
        }

        // Initial data load
        loadRiwayat()
    }

    private fun showComplaintDetails(complaint: ComplaintResponse) {
        val bottomSheetFragment = ShowComplaintDialogFragment.newInstance(complaint)
        bottomSheetFragment.show(parentFragmentManager, "ComplaintDetailsDialog")
    }

    private fun loadRiwayat() {
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
                    val resolvedComplaints = complaints.filter { it.status == "resolved" }

                    // Hide loading indicators
                    progressBar.visibility = View.GONE
                    swipeRefreshLayout.isRefreshing = false

                    if (resolvedComplaints.isEmpty()) {
                        // Show empty view
                        emptyView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        // Show complaints
                        emptyView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter = ComplaintAdapter(
                            resolvedComplaints,  // Use the filtered list here
                            onUpdateClicked = { complaint ->
                                // updateComplaintStatus(complaint)
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
