package com.example.kai_content.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.kai_content.databinding.FragmentEditProfileBinding

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EditProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(EditProfileViewModel::class.java)

        val sharedPreferences =
            requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token != null) {
            viewModel.fetchUser(token)

            // Observe data
            viewModel.user.observe(viewLifecycleOwner) { user ->
                binding.etName.setText(user.name)
                binding.etEmail.setText(user.email)
                binding.etPhone.setText(user.phone ?: "")
                binding.etAddress.setText(user.address ?: "")
            }

            viewModel.message.observe(viewLifecycleOwner) {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }

            // Tombol Save
            binding.btnSave.setOnClickListener {
                val data = mapOf(
                    "name" to binding.etName.text.toString(),
                    "email" to binding.etEmail.text.toString(),
                    "phone" to binding.etPhone.text.toString(),
                    "address" to binding.etAddress.text.toString()
                )
                viewModel.updateUser(token, data)

                viewModel.updateSuccess.observe(viewLifecycleOwner) { isSuccess ->
                    if (isSuccess) {
                        viewModel.fetchUser(token) // Fetch ulang dari backend
//                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        } else {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
