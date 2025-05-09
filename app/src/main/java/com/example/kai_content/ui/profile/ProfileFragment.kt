package com.example.kai_content.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.kai_content.LoginActivity
import com.example.kai_content.R
import com.example.kai_content.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Ambil token dari SharedPreferences
        val sharedPreferences =
            requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token != null) {
            // Fetch user data menggunakan token
            profileViewModel.fetchUserData(token)
        } else {
            // Token tidak ditemukan, tampilkan pesan atau navigasikan ke LoginActivity
            binding.tvUsername.text = "Token not found. Please log in."
        }

        // Observasi LiveData dari ViewModel
        profileViewModel.profile.observe(viewLifecycleOwner) { profile ->
            binding.tvUsername.text = profile.name
            binding.tvUserType.text = profile.role

            // Menampilkan gambar profil menggunakan Glide
            Glide.with(this)
                .load(profile.profilePictureUrl) // URL dari API
                .placeholder(R.drawable.ic_edit_profile) // jika belum ada gambar
                .error(R.drawable.ic_lock) // jika gagal load
                .into(binding.ivProfileImage)
        }

        profileViewModel.error.observe(viewLifecycleOwner) { message ->
//            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        // Observe data dari ViewModel
        profileViewModel.name.observe(viewLifecycleOwner) { username ->
            binding.tvUsername.text = username
        }

        profileViewModel.email.observe(viewLifecycleOwner) { email ->
            binding.tvUserType.text = email // Assuming user type is derived from email
        }

        // Handle click events
        setupClickListeners()

        return root
    }

    private fun setupClickListeners() {
        // Edit profile pict
        binding.ivProfileImage.setOnClickListener {
            // Handle profile picture click
            Toast.makeText(requireContext(), "Profile picture clicked", Toast.LENGTH_SHORT).show()
            // Add navigation to Edit Profile screen if needed
        }

        // Edit Profile
        binding.cvEditProfile.setOnClickListener {
//            Toast.makeText(requireContext(), "Edit Profile clicked", Toast.LENGTH_SHORT).show()

            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

//        // Notifications
//        binding.cvNotification.setOnClickListener {
//            Toast.makeText(requireContext(), "Notifications clicked", Toast.LENGTH_SHORT).show()
//            // Add navigation to Notifications screen if needed
//        }
//
//        // Shipping Address
//        binding.cvShippingAddress.setOnClickListener {
//            Toast.makeText(requireContext(), "Shipping Address clicked", Toast.LENGTH_SHORT).show()
//            // Add navigation to Shipping Address screen if needed
//        }

        // Change Password
        binding.cvChangePassword.setOnClickListener {
            Toast.makeText(requireContext(), "Change Password clicked", Toast.LENGTH_SHORT).show()
            // Add navigation to Change Password screen if needed

            val changePasswordBottomSheet = ChangePasswordBottomSheetFragment()
            changePasswordBottomSheet.show(parentFragmentManager, "ChangePasswordBottomSheet")
        }

        // Sign Out
        binding.btnSignOut.setOnClickListener {
            // Clear token from SharedPreferences
            val sharedPreferences =
                requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.remove("auth_token")
            editor.apply()

            // Navigate to LoginActivity
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            // Show a toast
            Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
