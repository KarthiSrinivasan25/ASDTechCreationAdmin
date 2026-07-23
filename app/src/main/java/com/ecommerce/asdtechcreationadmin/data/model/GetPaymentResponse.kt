package com.ecommerce.asdtechcreationadmin.data.model

data class GetPaymentResponse(
    val status: String,
    val data: PaymentDetail?,
    val message: String?
)
