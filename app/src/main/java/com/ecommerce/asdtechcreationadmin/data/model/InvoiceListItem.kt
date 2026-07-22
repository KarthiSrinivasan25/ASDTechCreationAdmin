package com.ecommerce.asdtechcreationadmin.data.model

data class InvoiceListItem(
    val id: Int,
    val invoice_number: String,
    val invoice_date: String,
    val due_date: String,
    val total_amount: Double,
    val paid_amount: Double,
    val balance_amount: Double,
    val status: String?,
    val client_name: String?,
    val company_name: String?
)
