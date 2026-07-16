package com.ecommerce.asdtechcreationadmin.data.model

data class LoginResponse(

    val status: Boolean,

    val message: String,

    val token: String?,

    val admin: Admin?

)