package com.ecommerce.asdtechcreationadmin.data.model

data class Client(
    val id: Int,
    val client_name: String,
    val company_name: String?,
    val email: String,
    val phone: String,
    val address: String?,
    val gst_number: String?,
    val project_name: String?,
    val service: String?,
    val project_value: Double?,
    val status: String?,
    val notes: String?
)
