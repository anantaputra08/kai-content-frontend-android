package com.example.kai_content.models.complaint

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ComplaintCategory(
    val id: Int,
    val name: String
) : Parcelable

@Parcelize
data class AssignedTo(
    val id: Int,
    val name: String,
    val email: String,
    val email_verified_at: String?,
    val role: String,
    val profile_picture: String?,
    @SerializedName("profile_picture_url")
    val profilePictureUrl: String?,
    val phone: String?,
    val address: String?,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?
) : Parcelable

@Parcelize
data class ComplaintResponse(
    val id: Int,
    val user_id: Int,
    val category_complaint_id: Int,
    val description: String,
    val status: String,
    val attachment: String?,
    @SerializedName("attachment_url")
    val attachmentUrl: String?,
    val resolution_date: String?,
    val resolution_notes: String?,
    val assigned_to: AssignedTo?,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?,
    val category_complaint: ComplaintCategory?
) : Parcelable
