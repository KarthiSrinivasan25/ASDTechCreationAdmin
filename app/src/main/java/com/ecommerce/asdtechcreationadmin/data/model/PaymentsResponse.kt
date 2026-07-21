package com.ecommerce.asdtechcreationadmin.data.model

data class PaymentsResponse(
    val status: String,
    val data: List<Payment>?
)
