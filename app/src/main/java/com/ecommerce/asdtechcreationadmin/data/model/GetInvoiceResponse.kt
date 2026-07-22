package com.ecommerce.asdtechcreationadmin.data.model

data class GetInvoiceResponse(
    val status: String,
    val invoice: InvoiceDetail?,
    val items: List<InvoiceItemDetail>?,
    val message: String?
)
