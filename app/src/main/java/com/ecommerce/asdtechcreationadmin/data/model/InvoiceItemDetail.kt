package com.ecommerce.asdtechcreationadmin.data.model

data class InvoiceItemDetail(
    val id: Int,
    val invoice_id: Int,
    val item_name: String,
    val description: String?,
    val quantity: Int,
    val unit_price: Double,
    val total: Double
)
