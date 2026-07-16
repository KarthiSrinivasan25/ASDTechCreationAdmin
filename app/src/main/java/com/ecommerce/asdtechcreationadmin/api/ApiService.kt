package com.ecommerce.asdtechcreationadmin.api

import com.ecommerce.asdtechcreationadmin.data.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("login.php")
    fun login(
        @Body request: LoginRequest
    ): Call<LoginResponse>


    @POST("verify.php")
    fun verify(
        @Body request: VerifyRequest
    ): Call<VerifyResponse>


    @GET("dashboard.php")
    fun getDashboard():
            Call<DashboardResponse>

}