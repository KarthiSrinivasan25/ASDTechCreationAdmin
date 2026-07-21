package com.ecommerce.asdtechcreationadmin.data.model

data class Payment(
    val id: Int,
    val receipt_number: String,
    val payment_date: String,
    val amount_paid: Double,
    val payment_method: String?,
    val transaction_id: String?,
    val notes: String?,
    val invoice_number: String?,
    val total_amount: Double?,
    val paid_amount: Double?,
    val balance_amount: Double?,
    val status: String?,
    val client_name: String?,
    val company_name: String?
)
