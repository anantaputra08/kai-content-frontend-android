package com.example.kai_content.ui.profile

import android.content.Context
import android.content.Intent
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.kai_content.LoginActivity
import com.example.kai_content.api.AuthApi
import com.example.kai_content.api.RetrofitClient
import com.example.kai_content.databinding.FragmentChangePasswordBottomSheetListDialogBinding
import com.example.kai_content.databinding.FragmentChangePasswordBottomSheetListDialogItemBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: Customize parameter argument names
const val ARG_ITEM_COUNT = "item_count"

/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    ChangePasswordBottomSheetFragment.newInstance(30).show(supportFragmentManager, "dialog")
 * </pre>
 */
class ChangePasswordBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentChangePasswordBottomSheetListDialogBinding? = null

    private val authApi = RetrofitClient.instance.create(AuthApi::class.java)

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding =
            FragmentChangePasswordBottomSheetListDialogBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSubmit.setOnClickListener {
            val newPassword = binding.etNewPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else if (newPassword != confirmPassword) {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
//                Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
//                dismiss()
                changePassword(newPassword)
            }
        }
    }

    private inner class ViewHolder internal constructor(binding: FragmentChangePasswordBottomSheetListDialogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        internal val text: TextView = binding.text
    }

    private inner class ItemAdapter internal constructor(private val mItemCount: Int) :
        RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            return ViewHolder(
                FragmentChangePasswordBottomSheetListDialogItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.text.text = position.toString()
        }

        override fun getItemCount(): Int {
            return mItemCount
        }
    }

    companion object {

        // TODO: Customize parameters
        fun newInstance(itemCount: Int): ChangePasswordBottomSheetFragment =
            ChangePasswordBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ITEM_COUNT, itemCount)
                }
            }

    }

    private fun changePassword(newPassword: String) {
        // Ambil token dari SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("AppPreferences", 0)
        val token = sharedPreferences.getString("auth_token", null)

        if (token == null) {
            Toast.makeText(context, "Token not found. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        val passwordData = mapOf(
            "password" to newPassword,
            "password_confirmation" to newPassword
        )

        authApi.changePassword("Bearer $token", passwordData)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
                        // Tambahkan logika untuk logout setelah password berhasil diubah
                        // Misalnya, panggil fungsi logout()
                        logout(token)
                    } else {
                        Toast.makeText(context, "Failed to update password", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun logout(token: String) {
        authApi.logout("Bearer $token")
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        // Hapus token dari SharedPreferences
                        val sharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.remove("auth_token")
                        editor.apply()

                        // Navigasi ke LoginActivity
                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)

                        // Tutup BottomSheet
                        dismiss()
                    } else {
                        Toast.makeText(context, "Failed to logout", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(context, "Logout error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}