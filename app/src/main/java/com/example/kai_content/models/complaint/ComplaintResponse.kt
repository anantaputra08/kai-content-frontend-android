package com.example.kai_content.models.complaint

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
/**
 * Model data untuk kategori pengaduan.
 *
 * @param id ID kategori pengaduan.
 * @param name Nama kategori pengaduan.
 *
 * @see Parcelize
 * @see Parcelable
 */
@Parcelize
data class ComplaintCategory(
    val id: Int,
    val name: String
) : Parcelable

/**
 * Model data untuk pengguna yang ditugaskan.
 *
 * @param id ID pengguna.
 * @param name Nama pengguna.
 * @param email Alamat email pengguna.
 * @param email_verified_at Tanggal verifikasi email (nullable).
 * @param role Peran pengguna.
 * @param profile_picture Gambar profil pengguna (nullable).
 * @param profilePictureUrl URL gambar profil pengguna (nullable).
 * @param phone Nomor telepon pengguna (nullable).
 * @param address Alamat pengguna (nullable).
 * @param created_at Tanggal dan waktu pembuatan pengguna.
 * @param updated_at Tanggal dan waktu pembaruan pengguna.
 * @param deleted_at Tanggal penghapusan pengguna (nullable).
 *
 * @see Parcelize
 * @see Parcelable
 */
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

/**
 * Model data untuk respons pengaduan.
 *
 * @param id ID pengaduan.
 * @param user_id ID pengguna yang mengajukan pengaduan.
 * @param category_complaint_id ID kategori pengaduan.
 * @param description Deskripsi pengaduan.
 * @param status Status pengaduan.
 * @param attachment Lampiran pengaduan (nullable).
 * @param attachmentUrl URL lampiran pengaduan (nullable).
 * @param resolution_date Tanggal penyelesaian pengaduan (nullable).
 * @param resolution_notes Catatan penyelesaian pengaduan (nullable).
 * @param assigned_to Pengguna yang ditugaskan untuk menangani pengaduan (nullable).
 * @param created_at Tanggal dan waktu pembuatan pengaduan.
 * @param updated_at Tanggal dan waktu pembaruan pengaduan.
 * @param deleted_at Tanggal penghapusan pengaduan (nullable).
 * @param category_complaint Kategori pengaduan (nullable).
 *
 * @see Parcelize
 * @see Parcelable
 * @see AssignedTo
 * @see ComplaintCategory
 * @see SerializedName
 */
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
