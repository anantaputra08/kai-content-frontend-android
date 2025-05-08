package com.example.kai_content.ui.complaint

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.kai_content.databinding.FragmentCreateComplainDialogListDialogBinding
import com.example.kai_content.models.complaint.ComplaintCategory
import com.example.kai_content.models.complaint.ComplaintRequest
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateComplaintDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentCreateComplainDialogListDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ComplaintViewModel
    private lateinit var token: String

    private var selectedCategoryId: Int = -1
    private var selectedImageUri: Uri? = null

    // Define categories list
    private val categories = listOf(
        ComplaintCategory(1, "Ticket Issues"),
        ComplaintCategory(2, "Train Delay"),
        ComplaintCategory(3, "Facilities Problem"),
        ComplaintCategory(4, "Staff Service"),
        ComplaintCategory(5, "Others")
    )

    // Activity result launcher for image picker
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                updateImagePreview()
            }
        }
    }

    // Activity result launcher for camera
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri?.let {
                updateImagePreview()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateComplainDialogListDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[ComplaintViewModel::class.java]

        // Ambil token dari SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("auth_token", "") ?: ""

        setupListeners()
        observeViewModel()

        // Fetch categories from backend
        viewModel.fetchCategories(token)
    }

    private fun setupCategorySpinner(categories: List<ComplaintCategory>) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories.map { it.name }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.categorySpinner.adapter = adapter
        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategoryId = categories[position].id
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                selectedCategoryId = -1
            }
        }
    }

    private fun setupListeners() {
        binding.btnAddAttachment.setOnClickListener {
            showImagePickerOptions()
        }

        binding.btnSubmit.setOnClickListener {
            submitComplaint()
        }
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            setupCategorySpinner(categories)
        }

        viewModel.isLoadingCategories.observe(viewLifecycleOwner) { isLoading ->
            binding.categorySpinner.isEnabled = !isLoading
            if (isLoading) {
                binding.categorySpinner.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    listOf("Loading categories...")
                )
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.submissionResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(requireContext(), "Complaint submitted successfully!", Toast.LENGTH_SHORT).show()
                dismiss() // Close the bottom sheet dialog
            }
        }
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Add Photo")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        photoFile?.let {
            selectedImageUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                it
            )

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri)

            takePictureLauncher.launch(takePictureIntent)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = requireContext().getExternalFilesDir(null)

        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
    }

    private fun updateImagePreview() {
        selectedImageUri?.let { uri ->
            binding.attachmentName.text = getFileName(uri)
            binding.imagePreview.visibility = View.VISIBLE
            binding.imagePreview.setImageURI(uri)
        }
    }

    private fun getFileName(uri: Uri): String {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex >= 0) {
                it.getString(nameIndex)
            } else {
                "Selected image"
            }
        } ?: "Selected image"
    }

    private fun submitComplaint() {
        val description = binding.descriptionEditText.text.toString().trim()

        // Validate inputs
        when {
            selectedCategoryId == -1 -> {
                Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
                return
            }
            description.isEmpty() -> {
                binding.descriptionInputLayout.error = "Description is required"
                return
            }
            else -> {
                binding.descriptionInputLayout.error = null
            }
        }

        // Create request object
        val complaintRequest = ComplaintRequest(
            category_complaint_id = selectedCategoryId,
            description = description,
            status = "Pending", // Default status
            attachment = selectedImageUri?.toString() ?: ""
        )

        // Submit the complaint
        viewModel.submitComplaint(requireContext(), token, complaintRequest, selectedImageUri)
    }

    private fun getUserId(): Int {
        // In a real app, this would come from your authentication system
        // For now, we'll return a fixed ID
        return 123
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): CreateComplaintDialogFragment = CreateComplaintDialogFragment()
    }
}
