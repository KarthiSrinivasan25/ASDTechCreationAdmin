package com.ecommerce.asdtechcreationadmin.data.model

data class ClientInvoiceOption(
    val id: Int,
    val invoice_number: String,
    val invoice_date: String,
    val due_date: String,
    val total_amount: Double,
    val paid_amount: Double,
    val balance_amount: Double,
    val status: String?
)
