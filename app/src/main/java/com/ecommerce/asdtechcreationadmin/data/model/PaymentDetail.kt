package com.ecommerce.asdtechcreationadmin.data.model

data class PaymentDetail(
    val id: Int,
    val client_id: Int,
    val invoice_id: Int,
    val receipt_number: String,
    val payment_date: String,
    val amount_paid: Double,
    val payment_method: String?,
    val transaction_id: String?,
    val notes: String?,
    val client_name: String?,
    val company_name: String?,
    val email: String?,
    val phone: String?,
    val address: String?,
    val gst_number: String?,
    val invoice_number: String?,
    val invoice_date: String?,
    val due_date: String?,
    val total_amount: Double?,
    val paid_amount: Double?,
    val balance_amount: Double?,
    val invoice_status: String?
)
