package com.example.kai_content.models.complaint

/**
 * Model data untuk permintaan pengaduan.
 *
 * @param category_complaint_id ID kategori pengaduan (nullable).
 * @param description Deskripsi pengaduan (nullable).
 * @param status Status pengaduan (nullable).
 * @param attachment Lampiran pengaduan (nullable).
 * @param assigned_to ID pengguna yang ditugaskan untuk menangani pengaduan (nullable).
 * @param resolution_date Tanggal penyelesaian pengaduan (nullable).
 * @param resolution_notes Catatan penyelesaian pengaduan (nullable).
 */
data class ComplaintRequest(
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
