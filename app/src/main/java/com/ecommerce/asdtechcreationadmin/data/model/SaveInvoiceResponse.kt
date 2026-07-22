package com.ecommerce.asdtechcreationadmin.data.model

data class SaveInvoiceResponse(
    val status: String,
    val message: String?,
    val invoice_id: Int?
)
