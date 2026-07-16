package com.ecommerce.asdtechcreationadmin.data.model

data class RecentPayment(

    val receipt_number: String,

    val client_name: String,

    val amount_paid: String,

    val payment_date: String,

    val payment_method: String

)