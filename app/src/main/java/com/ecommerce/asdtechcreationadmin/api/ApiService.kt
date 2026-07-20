package com.ecommerce.asdtechcreationadmin.api

import com.ecommerce.asdtechcreationadmin.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

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


    @GET("get_client.php")
    fun getClient(
        @Query("id") id: Int
    ): Call<SingleClientResponse>


    @GET("delete_client.php")
    fun deleteClient(
        @Query("id") id: Int
    ): Call<SimpleResponse>


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


    @FormUrlEncoded
    @POST("update_client.php")
    fun updateClient(
        @Field("id") id: Int,
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
    ): Call<SimpleResponse>


    // NOTE: assumed to live under an "settings/" subfolder based on the
    // "../../config/db.php" include path (one level deeper than the other
    // endpoints above, which use "../config/db.php"). If your actual path
    // differs, just update these two @GET/@POST strings.

    @GET("company_profile/company-profile-get.php")
    fun getCompanyProfile(): Call<CompanyProfileGetResponse>


    @POST("company_profile/company-profile-save.php")
    fun saveCompanyProfile(
        @Body request: CompanyProfile
    ): Call<CompanyProfileSaveResponse>


    @GET("signatures/get-signatures.php")
    fun getSignatures(): Call<SignaturesResponse>


    @Multipart
    @POST("signatures/add_signature.php")
    fun addSignature(
        @Part("name") name: RequestBody,
        @Part image: MultipartBody.Part
    ): Call<AddSignatureResponse>

}