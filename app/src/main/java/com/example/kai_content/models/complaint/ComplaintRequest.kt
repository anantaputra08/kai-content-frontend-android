package com.example.kai_content.models.complaint

data class ComplaintRequest(
//    val user_id: Int,
    val category_complaint_id: Int? = null,
    val description: String? = null,
    val status: String? = null,
    val attachment: String? = null,
    val assigned_to: Int? = null,
    val resolution_date: String? = null,
    val resolution_notes: String? = null
)
data class UpdateComplaintRequest(
    val category_complaint_id: Int? = null,
    val description: String? = null,
    val assigned_to: Int? = null,
    val resolution_date: String? = null,
    val resolution_notes: String? = null,
    val status: String? = null
    // Note: attachment will be handled separately in multipart form data
)
