package com.ecommerce.asdtechcreationadmin.data.model

data class SignaturesResponse(
    val status: Boolean,
    val data: List<Signature>?,
    val message: String?
)
