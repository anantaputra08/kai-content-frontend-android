package com.example.kai_content.ui.stream

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kai_content.R
import android.widget.TextView
import android.util.Log
import android.widget.Toast

class StreamFragment : Fragment() {

    companion object {
        fun newInstance() = StreamFragment()
    }

    private val viewModel: StreamViewModel by viewModels()

    // Variable untuk menyimpan data yang diterima
    private var trainCarNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Terima data dari arguments
        trainCarNumber = arguments?.getString("trainCarNumber")

        // Log untuk debugging
        Log.d("StreamFragment", "Received trainCarNumber: $trainCarNumber")

        // Tambahan: Log untuk memastikan data diterima
        if (trainCarNumber.isNullOrEmpty()) {
            Log.w("StreamFragment", "trainCarNumber is null or empty!")
        } else {
            Log.d("StreamFragment", "trainCarNumber received successfully: $trainCarNumber")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_stream, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Gunakan data yang diterima
        trainCarNumber?.let { carNumber ->
            // Tampilkan Toast dengan informasi lebih jelas
            Toast.makeText(
                requireContext(),
                "Nomor Gerbong: $carNumber",
                Toast.LENGTH_LONG
            ).show()

            Log.d("StreamFragment", "Toast displayed for car number: $carNumber")

            // Setup streaming jika diperlukan
            setupStreamingWithCarNumber(carNumber)
        } ?: run {
            // Jika data tidak ada, tampilkan pesan error
            Toast.makeText(
                requireContext(),
                "Data nomor gerbong tidak ditemukan",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("StreamFragment", "No train car number received!")
        }
    }

    private fun setupStreamingWithCarNumber(carNumber: String) {
        // Implementasi logika streaming berdasarkan nomor gerbong
        Log.d("StreamFragment", "Setting up streaming for car number: $carNumber")

        // Contoh penggunaan dengan ViewModel
        // viewModel.startStreaming(carNumber)
    }
}
