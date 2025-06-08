package com.example.kai_content.ui.home2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.kai_content.R
import com.google.android.material.textfield.TextInputEditText

class Home2Fragment : Fragment() {

    private val viewModel: Home2ViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home2, container, false)

        val btnNext = view.findViewById<View>(R.id.btn_next)
        val inputField = view.findViewById<TextInputEditText>(R.id.train_car_input)

        btnNext.setOnClickListener {
            val inputValue = inputField.text?.toString()

            // 1. Konversi input String menjadi Long (aman jika input kosong atau bukan angka)
            val carriageId = inputValue?.toLongOrNull()

            if (carriageId != null) {
                // 2. Buat Bundle dengan tipe data dan kunci yang benar
                val bundle = Bundle().apply {
                    // Gunakan kunci "carriage_id" dan tipe data Long
                    putLong("carriage_id", carriageId)
                }
                // 3. Navigasi ke StreamFragment dengan Bundle yang sudah benar
                findNavController().navigate(R.id.action_home2Fragment_to_StreamFragment, bundle)
            } else {
                // Tampilkan pesan jika input tidak valid
                Toast.makeText(requireContext(), "Masukkan nomor gerbong yang valid", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
