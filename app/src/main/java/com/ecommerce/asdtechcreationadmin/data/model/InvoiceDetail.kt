package com.ecommerce.asdtechcreationadmin.data.model

data class InvoiceDetail(
    val id: Int,
    val client_id: Int,
    val invoice_number: String,
    val invoice_date: String,
    val due_date: String,
    val subtotal: Double,
    val gst_percent: Double,
    val gst_amount: Double,
    val discount: Double,
    val total_amount: Double,
    val paid_amount: Double,
    val balance_amount: Double,
    val status: String?,
    val notes: String?,
    val signature_id: Int?,
    val client_name: String?,
    val company_name: String?,
    val email: String?,
    val phone: String?,
    val address: String?,
    val gst_number: String?
)
