package com.example.kai_content.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kai_content.R
import com.example.kai_content.api.ContentApi
import com.example.kai_content.api.RetrofitClient
import com.example.kai_content.databinding.FragmentHomeBinding
import com.example.kai_content.models.content.Content
import com.example.kai_content.repositories.ContentRepository

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var contentAdapter: ContentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Ambil token dari SharedPreferences
        val sharedPreferences =
            requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token == null) {
            Toast.makeText(context, "Token not found. Please login again.", Toast.LENGTH_LONG).show()
            return root
        }

        // Setup repository dan ViewModel
        val contentApi = RetrofitClient.instance.create(ContentApi::class.java)
        val repository = ContentRepository(contentApi)
        val factory = HomeViewModelFactory(repository, "Bearer $token")
        homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()

        return root
    }

    private fun setupRecyclerView() {
        contentAdapter = ContentAdapter { content ->
            onContentClick(content)
        }

        binding.recyclerVideos.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = contentAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            homeViewModel.refreshContents()
        }
    }

    private fun observeViewModel() {
        homeViewModel.contents.observe(viewLifecycleOwner) { contents ->
            contentAdapter.submitList(contents)
            binding.textNoData.visibility = if (contents.isEmpty()) View.VISIBLE else View.GONE
        }

        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility =
                if (isLoading && !binding.swipeRefresh.isRefreshing) View.VISIBLE else View.GONE

            if (!isLoading) {
                binding.swipeRefresh.isRefreshing = false
            }
        }

        homeViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onContentClick(content: Content) {
        val bundle = bundleOf("content_id" to content.id.toString())
        findNavController().navigate(R.id.action_homeFragment_to_showContentFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
