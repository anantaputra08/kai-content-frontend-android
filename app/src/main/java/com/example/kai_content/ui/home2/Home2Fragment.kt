package com.example.kai_content.ui.home2

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.kai_content.R
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText

class Home2Fragment : Fragment() {

    companion object {
        fun newInstance() = Home2Fragment()
    }

    private val viewModel: Home2ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home2, container, false)

        val btnNext = view.findViewById<View>(R.id.btn_next)
        val inputField = view.findViewById<TextInputEditText>(R.id.train_car_input)

        btnNext.setOnClickListener {
            val inputValue = inputField.text?.toString() ?: ""

            // Debug: pastikan input tidak kosong
            Log.d("Home2Fragment", "Sending trainCarNumber: $inputValue")

            if (inputValue.isNotEmpty()) {
                val bundle = Bundle().apply {
                    putString("trainCarNumber", inputValue) // Key harus sama dengan yang di StreamFragment
                }
                findNavController().navigate(R.id.action_home2Fragment_to_StreamFragment, bundle)
            } else {
                Toast.makeText(requireContext(), "Masukkan nomor gerbong terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
