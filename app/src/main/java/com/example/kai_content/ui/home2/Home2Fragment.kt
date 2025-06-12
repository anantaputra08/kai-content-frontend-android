package com.example.kai_content.ui.home2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.kai_content.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class Home2Fragment : Fragment() {

    private val viewModel: Home2ViewModel by viewModels()
    private var selectedTrainId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home2, container, false)

        val trainDropdownLayout = view.findViewById<TextInputLayout>(R.id.train_select_layout)
        val trainDropdown = view.findViewById<AutoCompleteTextView>(R.id.train_select_dropdown)
        val carriageInput = view.findViewById<TextInputEditText>(R.id.train_car_input)
        val btnNext = view.findViewById<View>(R.id.btn_next)
        val progressBar = view.findViewById<View>(R.id.progress_bar)

        // Observe train data from ViewModel
        viewModel.trains.observe(viewLifecycleOwner) { trains ->
            val trainNames = trains.map { it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, trainNames)
            trainDropdown.setAdapter(adapter)

            trainDropdown.setOnItemClickListener { parent, _, position, _ ->
                val selectedTrain = trains[position]
                selectedTrainId = selectedTrain.id
                trainDropdownLayout.error = null // Clear error on selection
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.isVisible = isLoading
            btnNext.isEnabled = !isLoading
        }

        // Observe error state
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }


        btnNext.setOnClickListener {
            val carriageValue = carriageInput.text?.toString()
            val carriageId = carriageValue?.toLongOrNull()

            var isValid = true
            if (selectedTrainId == null) {
                trainDropdownLayout.error = "Harap pilih kereta"
                isValid = false
            } else {
                trainDropdownLayout.error = null
            }

            if (carriageId == null) {
                (carriageInput.parent.parent as? TextInputLayout)?.error = "Masukkan nomor gerbong yang valid"
                isValid = false
            } else {
                (carriageInput.parent.parent as? TextInputLayout)?.error = null
            }

            if (isValid) {
                val bundle = Bundle().apply {
                    putLong("train_id", selectedTrainId!!)
                    putLong("carriage_id", carriageId!!)
                }
                findNavController().navigate(R.id.action_home2Fragment_to_StreamFragment, bundle)
            }
        }
        return view
    }
}
