package com.ecommerce.asdtechcreationadmin.data.model

data class CompanyProfileGetResponse(
    val status: Boolean,
    val data: CompanyProfile?,
    val message: String?
)
