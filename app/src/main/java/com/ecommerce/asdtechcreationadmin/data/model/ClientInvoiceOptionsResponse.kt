package com.ecommerce.asdtechcreationadmin.data.model

data class ClientInvoiceOptionsResponse(
    val status: String,
    val data: List<ClientInvoiceOption>?,
    val message: String?
)
