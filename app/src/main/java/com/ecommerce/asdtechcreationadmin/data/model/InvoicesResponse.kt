package com.ecommerce.asdtechcreationadmin.data.model

data class InvoicesResponse(
    val status: String,
    val data: List<InvoiceListItem>?
)
