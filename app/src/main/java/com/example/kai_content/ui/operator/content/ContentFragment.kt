package com.example.kai_content.ui.operator.content

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.kai_content.R
import com.example.kai_content.models.content.Content
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ContentFragment : Fragment() {

    private val viewModel: ContentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_content_operator, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        val searchInput = view.findViewById<EditText>(R.id.search_input)
        val searchButton = view.findViewById<Button>(R.id.search_button)
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        val fabAddComplaint = view.findViewById<FloatingActionButton>(R.id.fab_add_complaint)

        fabAddComplaint.setOnClickListener {
            navigateToCreateFragment()
        }

        val sharedPreferences =
            requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token == null) {
            Toast.makeText(context, "Token not found. Please login again.", Toast.LENGTH_LONG).show()
        }

        recyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.contents.observe(viewLifecycleOwner) { contents ->
            recyclerView.adapter = ContentAdapter(contents) { content ->
                // Handle item click
                showContentDetailBottomSheet(content)
            }
            swipeRefreshLayout.isRefreshing = false // Stop refreshing animation
        }

        if (token != null) {
            viewModel.fetchContents(token)
        }

        searchButton.setOnClickListener {
            val searchText = searchInput.text.toString()
            if (searchText.isNotEmpty()) {
                if (token != null) {
                    viewModel.searchContents(token, searchText)
                }
            }
        }

        // Set up pull-to-refresh listener
        swipeRefreshLayout.setOnRefreshListener {
            if (token != null) {
                viewModel.fetchContents(token)
            } // Refresh data
        }

        return view
    }

    private fun showContentDetailBottomSheet(content: Content) {
        val detailFragment = DetailContentDialogFragment.newInstance(content)

        detailFragment.setNavigateToEditListener { contentId ->
            navigateToEditFragment(contentId)
        }

        detailFragment.show(parentFragmentManager, "DetailContentDialog")
    }

    private fun navigateToEditFragment(contentId: String) {
        // Method 1: Using the action with a bundle
        val bundle = Bundle().apply {
            putString("content_id", contentId)
        }
        findNavController().navigate(R.id.nav_edit_content_operator, bundle)

        // Alternative Method 2: If you have SafeArgs set up
        // val action = ContentFragmentDirections.actionContentFragmentToEditContenFragment(contentId)
        // findNavController().navigate(action)
    }

    private fun navigateToCreateFragment() {
        findNavController().navigate(R.id.action_contentFragment_to_createContentFragment)
    }

}
