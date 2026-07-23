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
import retrofit2.http.Streaming

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


    @GET("payments/get_payments.php")
    fun getPayments(): Call<PaymentsResponse>


    @FormUrlEncoded
    @POST("payments/save_payment.php")
    fun savePayment(
        @Field("client_id") clientId: Int,
        @Field("invoice_id") invoiceId: Int,
        @Field("payment_date") paymentDate: String,
        @Field("amount_paid") amountPaid: String,
        @Field("payment_method") paymentMethod: String,
        @Field("transaction_id") transactionId: String,
        @Field("notes") notes: String
    ): Call<SavePaymentResponse>


    // NOTE: assumed to live under an "invoices/" subfolder, matching the
    // per-feature subfolder convention used elsewhere (payments/, settings/).

    @GET("invoice/get_invoices.php")
    fun getInvoices(): Call<InvoicesResponse>


    @GET("invoice/get_invoice_next.php")
    fun getNextInvoiceNumber(): Call<NextInvoiceNumberResponse>


    @FormUrlEncoded
    @POST("invoice/save_invoice.php")
    fun saveInvoice(
        @Field("client_id") clientId: Int,
        @Field("invoice_date") invoiceDate: String,
        @Field("due_date") dueDate: String,
        @Field("subtotal") subtotal: String,
        @Field("gst_percent") gstPercent: String,
        @Field("gst_amount") gstAmount: String,
        @Field("discount") discount: String,
        @Field("total_amount") totalAmount: String,
        @Field("paid_amount") paidAmount: String,
        @Field("balance_amount") balanceAmount: String,
        @Field("status") status: String,
        @Field("notes") notes: String,
        @Field("signatory") signatory: String,
        @Field("item_name[]") itemNames: List<String>,
        @Field("description[]") descriptions: List<String>,
        @Field("quantity[]") quantities: List<String>,
        @Field("unit_price[]") unitPrices: List<String>,
        @Field("total[]") totals: List<String>
    ): Call<SaveInvoiceResponse>


    @GET("invoice/get_invoice.php")
    fun getInvoice(
        @Query("id") id: Int
    ): Call<GetInvoiceResponse>


    @GET("invoice/delete_invoice.php")
    fun deleteInvoice(
        @Query("id") id: Int
    ): Call<SimpleResponse>


    @FormUrlEncoded
    @POST("invoice/update_invoice.php")
    fun updateInvoice(
        @Field("invoice_id") invoiceId: Int,
        @Field("client_id") clientId: Int,
        @Field("invoice_number") invoiceNumber: String,
        @Field("invoice_date") invoiceDate: String,
        @Field("due_date") dueDate: String,
        @Field("subtotal") subtotal: String,
        @Field("gst_percent") gstPercent: String,
        @Field("gst_amount") gstAmount: String,
        @Field("discount") discount: String,
        @Field("total_amount") totalAmount: String,
        @Field("paid_amount") paidAmount: String,
        @Field("balance_amount") balanceAmount: String,
        @Field("status") status: String,
        @Field("notes") notes: String,
        @Field("signature_id") signatureId: String,
        @Field("item_name[]") itemNames: List<String>,
        @Field("description[]") descriptions: List<String>,
        @Field("quantity[]") quantities: List<String>,
        @Field("unit_price[]") unitPrices: List<String>,
        @Field("total[]") totals: List<String>
    ): Call<SimpleResponse>


    @Streaming
    @GET("invoice/generate_invoice_pdf.php")
    fun downloadInvoicePdf(
        @Query("id") id: Int
    ): Call<okhttp3.ResponseBody>


    // Path taken directly from the docblock in send_invoice_email.php:
    // "Endpoint: POST api/invoice/send_invoice_email.php" (singular "invoice").

    @POST("invoice/send_invoice_email.php")
    fun sendInvoiceEmail(
        @Body request: InvoiceIdRequest
    ): Call<SimpleResponse>


    @GET("payments/get_client_invoices.php")
    fun getClientInvoices(
        @Query("client_id") clientId: Int
    ): Call<ClientInvoiceOptionsResponse>

}