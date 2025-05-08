package com.example.kai_content.ui.operator.content

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kai_content.R
import com.example.kai_content.models.content.Category
import java.io.File
import java.io.FileOutputStream

class CreateContentFragment : Fragment() {

    private val viewModel: CreateContentViewModel by viewModels()

    private var selectedFile: File? = null
    private var selectedThumbnail: File? = null
    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var statusSpinner: Spinner

    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var progressPercentageTextView: TextView
    private lateinit var uploadStatusTextView: TextView
    private lateinit var progressContainer: ViewGroup
    private lateinit var contentLayout: ViewGroup

    private lateinit var thumbnailImageView: ImageView
    private lateinit var fileNameTextView: TextView
    private lateinit var thumbnailNameTextView: TextView
    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var categoryAdapter: CategorySelectionAdapter
    private var token: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_create_content_operator, container, false)

        titleInput = view.findViewById(R.id.titleEditText)
        descriptionInput = view.findViewById(R.id.descriptionEditText)
        statusSpinner = view.findViewById(R.id.statusSpinner)

        // Find other views
        loadingProgressBar = view.findViewById(R.id.progressBar)
        progressPercentageTextView = view.findViewById(R.id.progressPercentageTextView)
        uploadStatusTextView = view.findViewById(R.id.uploadStatusTextView)
        progressContainer = view.findViewById(R.id.progress_container)
        contentLayout = view.findViewById(R.id.content_layout)


        thumbnailImageView = view.findViewById(R.id.thumbnail_preview)
        fileNameTextView = view.findViewById(R.id.videoNameTextView)
        thumbnailNameTextView = view.findViewById(R.id.thumbnailNameTextView)
        categoryRecyclerView = view.findViewById(R.id.category_recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences =
            requireContext().getSharedPreferences("AppPreferences", Activity.MODE_PRIVATE)
        token = sharedPreferences.getString("auth_token", null)

        // Set up RecyclerView
        categoryAdapter = CategorySelectionAdapter()
        categoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = categoryAdapter
        }

        // Set up spinner adapter for status selection
        val statusAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.content_status_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            statusSpinner.adapter = adapter
        }

        // Set up click listeners
        view.findViewById<Button>(R.id.selectVideoButton).setOnClickListener {
            selectFile()
        }

        view.findViewById<Button>(R.id.selectThumbnailButton).setOnClickListener {
            selectThumbnail()
        }

        view.findViewById<Button>(R.id.uploadButton).setOnClickListener {
            submitContent()
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressContainer.visibility = if (isLoading) View.VISIBLE else View.GONE
            contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        // Observe upload progress
        viewModel.uploadProgress.observe(viewLifecycleOwner) { progress ->
            loadingProgressBar.progress = progress
            progressPercentageTextView.text = "$progress%"
        }

        // Observe upload status
        viewModel.uploadStatus.observe(viewLifecycleOwner) { status ->
            uploadStatusTextView.text = status
        }

        // Load categories
        loadCategories()
    }

    private fun loadCategories() {
        token?.let { authToken ->
            viewModel.getAllCategories(
                authToken,
                onSuccess = { categories ->
                    categoryAdapter.updateCategories(categories)
                },
                onError = { error ->
                    Toast.makeText(context, "Error loading categories: $error", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun submitContent() {
        val title = titleInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()
        val status = statusSpinner.selectedItem.toString()
        val selectedCategoryIds = categoryAdapter.getSelectedCategoryIds()

        // Basic validation
        if (title.isEmpty()) {
            Toast.makeText(context, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedFile == null) {
            Toast.makeText(context, "Please select a video file", Toast.LENGTH_SHORT).show()
            return
        }

        token?.let { authToken ->
            viewModel.createContent(
                token = authToken,
                title = title,
                description = description,
                status = status,
                file = selectedFile!!,
                thumbnail = selectedThumbnail,
                categoryIds = selectedCategoryIds,
                onSuccess = {
                    Toast.makeText(context, "Content created successfully", Toast.LENGTH_SHORT).show()
                    clearForm()
                },
                onError = { error ->
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }
            )
        } ?: run {
            Toast.makeText(context, "Authentication token not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "video/*"
        startActivityForResult(intent, FILE_REQUEST_CODE)
    }

    private fun selectThumbnail() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, THUMBNAIL_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data ?: return

            when (requestCode) {
                FILE_REQUEST_CODE -> {
                    selectedFile = getFileFromUri(uri)
                    fileNameTextView.text = selectedFile?.name ?: "No file selected"
                    fileNameTextView.visibility = View.VISIBLE
                }
                THUMBNAIL_REQUEST_CODE -> {
                    selectedThumbnail = getFileFromUri(uri)
                    thumbnailNameTextView.text = selectedThumbnail?.name ?: "No thumbnail selected"
                    thumbnailNameTextView.visibility = View.VISIBLE

                    // Preview selected thumbnail
                    selectedThumbnail?.let {
                        Glide.with(requireContext())
                            .load(it)
                            .into(thumbnailImageView)
                        thumbnailImageView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && it.moveToFirst()) {
                val name = it.getString(nameIndex)
                val file = File(requireContext().cacheDir, name)

                try {
                    requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                        FileOutputStream(file).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    return file
                } catch (e: Exception) {
                    Toast.makeText(context, "Error processing file: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return null
    }

    private fun clearForm() {
        titleInput.text.clear()
        descriptionInput.text.clear()
        statusSpinner.setSelection(0)
        selectedFile = null
        selectedThumbnail = null
        fileNameTextView.visibility = View.GONE
        thumbnailNameTextView.visibility = View.GONE
        thumbnailImageView.visibility = View.GONE
        categoryAdapter.clearSelections()
    }

    companion object {
        private const val FILE_REQUEST_CODE = 100
        private const val THUMBNAIL_REQUEST_CODE = 101

        fun newInstance(): CreateContentFragment {
            return CreateContentFragment()
        }
    }
}
