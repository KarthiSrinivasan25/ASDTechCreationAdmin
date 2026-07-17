package com.ecommerce.asdtechcreationadmin.api

import com.ecommerce.asdtechcreationadmin.data.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
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


    @GET("get_clients.php")
    fun getClients(): Call<ClientsResponse>


    @FormUrlEncoded
    @POST("save_client.php")
    fun saveClient(
        @Field("client_name") clientName: String,
        @Field("company_name") companyName: String,
        @Field("email") email: String,
        @Field("phone") phone: String,
        @Field("address") address: String,
        @Field("gst_number") gstNumber: String,
        @Field("project_name") projectName: String,
        @Field("service") service: String,
        @Field("project_value") projectValue: String,
        @Field("status") status: String,
        @Field("notes") notes: String
    ): Call<SaveClientResponse>

}