package com.ecommerce.asdtechcreationadmin.data.model

data class RecentInvoice(

    val invoice_number: String,

    val client_name: String,

    val invoice_date: String,

    val total_amount: String,

    val status: String

)