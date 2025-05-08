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
import com.example.kai_content.models.content.Content
import java.io.File
import java.io.FileOutputStream

class EditContenFragment : Fragment() {

    private val viewModel: EditContenViewModel by viewModels()

    private var selectedFile: File? = null
    private var selectedThumbnail: File? = null
    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var statusSpinner: Spinner
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var contentLayout: ViewGroup
    private lateinit var thumbnailImageView: ImageView
    private lateinit var fileNameTextView: TextView
    private lateinit var thumbnailNameTextView: TextView
    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var categoryAdapter: CategorySelectionAdapter
    private var contentId: String = ""
    private var token: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_edit_conten_operator, container, false)

        titleInput = view.findViewById(R.id.edit_title)
        descriptionInput = view.findViewById(R.id.edit_description)
        statusSpinner = view.findViewById(R.id.edit_status)

        // Find other views
        loadingProgressBar = view.findViewById(R.id.loading_progress)
        contentLayout = view.findViewById(R.id.content_layout)
        thumbnailImageView = view.findViewById(R.id.thumbnail_preview)
        fileNameTextView = view.findViewById(R.id.file_name)
        thumbnailNameTextView = view.findViewById(R.id.thumbnail_name)
        categoryRecyclerView = view.findViewById(R.id.category_recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences =
            requireContext().getSharedPreferences("AppPreferences", Activity.MODE_PRIVATE)
        token = sharedPreferences.getString("auth_token", null)

        // Get the content ID from arguments
        contentId = arguments?.getString("content_id") ?: ""

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
        view.findViewById<Button>(R.id.button_select_file).setOnClickListener {
            selectFile()
        }

        view.findViewById<Button>(R.id.button_select_thumbnail).setOnClickListener {
            selectThumbnail()
        }

        view.findViewById<Button>(R.id.button_submit).setOnClickListener {
            submitContentUpdate()
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        // Load categories
        loadCategories()

        // Load content data if we have a valid contentId
        if (contentId.isNotEmpty() && token != null) {
            loadContentData(contentId, token!!)
        }
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

    private fun submitContentUpdate() {
        val title = titleInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()
        val status = statusSpinner.selectedItem.toString()
        val selectedCategoryIds = categoryAdapter.getSelectedCategoryIds()

        // Basic validation
        if (title.isEmpty()) {
            Toast.makeText(context, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        token?.let { authToken ->
            viewModel.updateContent(
                contentId,
                title,
                description,
                status,
                selectedCategoryIds,
                selectedFile,
                selectedThumbnail,
                authToken,
                onSuccess = {
                    // Refresh content view to show updates
                    loadContentData(contentId, authToken)
                    Toast.makeText(context, "Content updated successfully", Toast.LENGTH_SHORT).show()

                    // Clear selected files after successful update
                    selectedFile = null
                    selectedThumbnail = null
                },
                onError = { error ->
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }
            )
        } ?: run {
            Toast.makeText(context, "Authentication token not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadContentData(contentId: String, token: String) {
        viewModel.getContentDetails(
            contentId,
            token,
            onSuccess = { content ->
                populateUI(content)

                // Update selected categories in adapter
                content.categories?.let { categories ->
                    categoryAdapter.updateSelectedCategories(categories)
                }
            },
            onError = { error ->
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun populateUI(content: Content) {
        titleInput.setText(content.title)
        descriptionInput.setText(content.description ?: "")

        // Display thumbnail preview if available
        if (!content.thumbnailUrl.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(content.thumbnailUrl)
                .placeholder(R.drawable.placeholder_thumbnail)
                .error(R.drawable.error_thumbnail)
                .into(thumbnailImageView)
            thumbnailImageView.visibility = View.VISIBLE
        } else {
            thumbnailImageView.visibility = View.GONE
        }

        // Show file name
        fileNameTextView.text = content.filePath?.substringAfterLast('/') ?: "No file selected"
        thumbnailNameTextView.text = "Current thumbnail"

        // Set spinner selection based on content status
        val statusAdapter = statusSpinner.adapter as ArrayAdapter<*>
        val statusPosition = (0 until statusAdapter.count)
            .firstOrNull { statusAdapter.getItem(it).toString() == content.status }
            ?: 0
        statusSpinner.setSelection(statusPosition)
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
                }
                THUMBNAIL_REQUEST_CODE -> {
                    selectedThumbnail = getFileFromUri(uri)
                    thumbnailNameTextView.text = selectedThumbnail?.name ?: "No thumbnail selected"

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

    companion object {
        private const val FILE_REQUEST_CODE = 100
        private const val THUMBNAIL_REQUEST_CODE = 101

        fun newInstance(contentId: String): EditContenFragment {
            val fragment = EditContenFragment()
            val args = Bundle()
            args.putString("content_id", contentId)
            fragment.arguments = args
            return fragment
        }
    }
}
