package com.example.kai_content.ui.operator.complaint

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.kai_content.R
import com.example.kai_content.models.complaint.ComplaintResponse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [ResolutionNotesDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ResolutionNotesDialogFragment : BottomSheetDialogFragment() {
    private lateinit var etResolutionNotes: EditText
    private lateinit var btnCancel: Button
    private lateinit var btnConfirm: Button

    private var onConfirmClickListener: ((ComplaintResponse, String) -> Unit)? = null

    companion object {
        private const val ARG_COMPLAINT = "complaint"

        fun newInstance(complaint: ComplaintResponse): ResolutionNotesDialogFragment {
            val fragment = ResolutionNotesDialogFragment()
            val args = Bundle()
            args.putParcelable(ARG_COMPLAINT, complaint)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_resolution_notes_operator_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etResolutionNotes = view.findViewById(R.id.etResolutionNotes)
        btnCancel = view.findViewById(R.id.btnCancel)
        btnConfirm = view.findViewById(R.id.btnConfirm)

        val complaint = arguments?.getParcelable<ComplaintResponse>(ARG_COMPLAINT)

        if (complaint == null) {
            Toast.makeText(context, "Error: Complaint data not found", Toast.LENGTH_SHORT).show()
            dismiss()
            return
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnConfirm.setOnClickListener {
            val resolutionNotes = etResolutionNotes.text.toString().trim()

            if (resolutionNotes.isEmpty()) {
                etResolutionNotes.error = "Resolution notes cannot be empty"
                return@setOnClickListener
            }

            onConfirmClickListener?.invoke(complaint, resolutionNotes)
            dismiss()
        }
    }

    fun setOnConfirmClickListener(listener: (ComplaintResponse, String) -> Unit) {
        onConfirmClickListener = listener
    }
}
