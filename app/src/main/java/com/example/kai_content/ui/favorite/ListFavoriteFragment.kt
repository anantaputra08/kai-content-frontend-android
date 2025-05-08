package com.example.kai_content.ui.favorite

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.kai_content.R
import com.example.kai_content.databinding.FragmentListFavoriteBinding
import com.example.kai_content.models.content.Content
import com.example.kai_content.ui.home.ContentAdapter
import com.example.kai_content.ui.operator.content.DetailContentDialogFragment

class ListFavoriteFragment : Fragment() {

    private val viewModel: ListFavoriteViewModel by viewModels()
    private var _binding: FragmentListFavoriteBinding? = null
    private val binding get() = _binding!!
    private lateinit var contentAdapter: ContentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView with ContentAdapter that uses ListAdapter pattern
        contentAdapter = ContentAdapter { content ->
            // Handle item click
            onContentClick(content)
        }

        binding.recyclerFavoriteVideos.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = contentAdapter
        }

        // Get token from SharedPreferences
        val sharedPreferences =
            requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token == null) {
            Toast.makeText(context, "Token not found. Please login again.", Toast.LENGTH_LONG).show()
        }

        // Observe the favorites data
        viewModel.favorites.observe(viewLifecycleOwner) { favorites ->
            contentAdapter.submitList(favorites)
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // You can add a progress indicator here if needed
        }

        // Observe error state
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }

        // Load data initially
        if (token != null) {
            viewModel.fetchFavorites(token)
        }

        // Set up SwipeRefreshLayout if it exists in your layout
        view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)?.setOnRefreshListener {
            if (token != null) {
                viewModel.fetchFavorites(token)
            }
        }
    }

    private fun onContentClick(content: Content) {
        val bundle = bundleOf("content_id" to content.id.toString())
        findNavController().navigate(R.id.action_favoriteFragment_to_showContentFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
