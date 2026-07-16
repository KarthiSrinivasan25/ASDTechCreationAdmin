package com.ecommerce.asdtechcreationadmin.data.model

data class Dashboard(

    val total_clients: Int,

    val total_invoices: Int,

    val invoice_amount: String,

    val paid_amount: String,

    val pending_amount: String

)