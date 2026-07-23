package com.ecommerce.asdtechcreationadmin.ui.payment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ecommerce.asdtechcreationadmin.R
import com.ecommerce.asdtechcreationadmin.api.ApiClient
import com.ecommerce.asdtechcreationadmin.data.model.GetPaymentResponse
import com.ecommerce.asdtechcreationadmin.data.model.InvoiceIdRequest
import com.ecommerce.asdtechcreationadmin.data.model.PaymentDetail
import com.ecommerce.asdtechcreationadmin.data.model.SimpleResponse
import com.ecommerce.asdtechcreationadmin.databinding.ActivityPaymentDetailsBinding
import com.ecommerce.asdtechcreationadmin.ui.common.PdfHelper
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class PaymentDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PAYMENT_ID = "extra_payment_id"
    }

    private lateinit var binding: ActivityPaymentDetailsBinding
    private var paymentId: Int = -1
    private var currentPayment: PaymentDetail? = null

    private val moneyFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadPayment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPaymentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        paymentId = intent.getIntExtra(EXTRA_PAYMENT_ID, -1)

        if (paymentId == -1) {
            Snackbar.make(binding.root, "Invalid payment", Snackbar.LENGTH_LONG).show()
            finish()
            return
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnEdit.setOnClickListener {
            editLauncher.launch(
                Intent(this, EditPaymentActivity::class.java)
                    .putExtra(EditPaymentActivity.EXTRA_PAYMENT_ID, paymentId)
            )
        }

        binding.btnDelete.setOnClickListener { confirmDelete() }

        binding.btnViewPdf.setOnClickListener {
            val payment = currentPayment ?: return@setOnClickListener
            PdfHelper.viewReceipt(this, paymentId, payment.receipt_number, binding.root) { loading ->
                binding.progressDetails.visibility = if (loading) View.VISIBLE else View.GONE
            }
        }

        binding.btnDownloadPdf.setOnClickListener {
            val payment = currentPayment ?: return@setOnClickListener
            PdfHelper.downloadReceiptToDevice(this, paymentId, payment.receipt_number, binding.root) { loading ->
                binding.progressDetails.visibility = if (loading) View.VISIBLE else View.GONE
            }
        }

        binding.btnEmailPdf.setOnClickListener {
            sendReceiptEmail()
        }

        loadPayment()
    }

    private fun loadPayment() {

        binding.progressDetails.visibility = View.VISIBLE

        ApiClient.apiService.getPayment(paymentId).enqueue(object : Callback<GetPaymentResponse> {

            override fun onResponse(
                call: Call<GetPaymentResponse>,
                response: Response<GetPaymentResponse>
            ) {

                binding.progressDetails.visibility = View.GONE

                val body = response.body()

                if (response.isSuccessful && body?.status == "success" && body.data != null) {
                    currentPayment = body.data
                    bindPayment(body.data)
                } else {
                    Snackbar.make(
                        binding.root,
                        body?.message ?: "Unable to load payment",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<GetPaymentResponse>, t: Throwable) {
                binding.progressDetails.visibility = View.GONE
                Snackbar.make(
                    binding.root,
                    t.message ?: "Something went wrong",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun bindPayment(payment: PaymentDetail) {

        binding.txtAmountPaid.text = "₹${moneyFormat.format(payment.amount_paid)}"
        binding.txtReceiptNumber.text = payment.receipt_number
        binding.txtPaymentMethod.text = payment.payment_method?.ifEmpty { "—" } ?: "—"
        binding.txtPaymentDate.text = "Paid on ${payment.payment_date}"

        binding.txtClientName.text = payment.client_name ?: "—"
        binding.txtCompanyName.text = payment.company_name?.ifEmpty { "—" } ?: "—"
        binding.txtClientEmail.text = payment.email?.ifEmpty { "—" } ?: "—"
        binding.txtClientPhone.text = payment.phone?.ifEmpty { "—" } ?: "—"

        binding.txtInvoiceNumber.text = payment.invoice_number ?: "—"
        binding.txtInvoiceDates.text =
            "Invoice: ${payment.invoice_date ?: "—"}   •   Due: ${payment.due_date ?: "—"}"

        binding.txtInvoiceTotal.text = "₹${moneyFormat.format(payment.total_amount ?: 0.0)}"
        binding.txtInvoicePaid.text = "₹${moneyFormat.format(payment.paid_amount ?: 0.0)}"
        binding.txtInvoiceBalance.text = "₹${moneyFormat.format(payment.balance_amount ?: 0.0)}"

        val status = payment.invoice_status?.trim()?.ifEmpty { "Pending" } ?: "Pending"
        binding.txtInvoiceStatus.text = status

        when (status.lowercase()) {
            "paid" -> {
                binding.txtInvoiceStatus.setBackgroundResource(R.drawable.bg_chip_green)
                binding.txtInvoiceStatus.setTextColor(
                    ContextCompat.getColor(this, R.color.accent_green)
                )
            }
            "partial" -> {
                binding.txtInvoiceStatus.setBackgroundResource(R.drawable.bg_chip_blue)
                binding.txtInvoiceStatus.setTextColor(
                    ContextCompat.getColor(this, R.color.accent_blue)
                )
            }
            else -> {
                binding.txtInvoiceStatus.setBackgroundResource(R.drawable.bg_chip_orange)
                binding.txtInvoiceStatus.setTextColor(
                    ContextCompat.getColor(this, R.color.accent_orange)
                )
            }
        }

        val transactionId = payment.transaction_id?.trim()
        binding.txtTransactionId.text = if (!transactionId.isNullOrEmpty()) transactionId else "—"

        val notes = payment.notes?.trim()
        if (!notes.isNullOrEmpty()) {
            binding.txtNotesLabel.visibility = View.VISIBLE
            binding.txtNotes.visibility = View.VISIBLE
            binding.txtNotes.text = notes
        } else {
            binding.txtNotesLabel.visibility = View.GONE
            binding.txtNotes.visibility = View.GONE
        }
    }

    private fun sendReceiptEmail() {

        val payment = currentPayment
        if (payment == null) {
            Snackbar.make(binding.root, "Payment not loaded yet", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (payment.email.isNullOrBlank()) {
            Snackbar.make(
                binding.root,
                "This client has no email address on file",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        binding.progressDetails.visibility = View.VISIBLE

        ApiClient.apiService.sendReceiptEmail(InvoiceIdRequest(paymentId))
            .enqueue(object : Callback<SimpleResponse> {

                override fun onResponse(
                    call: Call<SimpleResponse>,
                    response: Response<SimpleResponse>
                ) {

                    binding.progressDetails.visibility = View.GONE

                    val body = response.body()

                    if (response.isSuccessful && body?.status == "success") {
                        showNotification(
                            body.message ?: "Receipt emailed successfully",
                            isSuccess = true
                        )
                    } else {
                        showNotification(
                            body?.message ?: "Failed to send receipt email",
                            isSuccess = false
                        )
                    }
                }

                override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                    binding.progressDetails.visibility = View.GONE
                    showNotification(
                        t.message ?: "Something went wrong. Please try again",
                        isSuccess = false
                    )
                }
            })
    }

    private fun showNotification(message: String, isSuccess: Boolean) {

        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view

        val colorRes = if (isSuccess) R.color.accent_green else R.color.accent_red

        snackbarView.setBackgroundColor(
            ContextCompat.getColor(this, colorRes)
        )

        val textView = snackbarView.findViewById<android.widget.TextView>(
            com.google.android.material.R.id.snackbar_text
        )
        textView.setTextColor(ContextCompat.getColor(this, R.color.white))
        textView.gravity = android.view.Gravity.CENTER

        snackbar.show()
    }

    private fun confirmDelete() {

        AlertDialog.Builder(this)
            .setTitle("Delete Payment")
            .setMessage("Are you sure you want to delete this payment? The invoice balance will be recalculated. This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deletePayment() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePayment() {

        binding.progressDetails.visibility = View.VISIBLE

        ApiClient.apiService.deletePayment(paymentId).enqueue(object : Callback<SimpleResponse> {

            override fun onResponse(
                call: Call<SimpleResponse>,
                response: Response<SimpleResponse>
            ) {

                binding.progressDetails.visibility = View.GONE

                val body = response.body()

                if (response.isSuccessful && body?.status == "success") {
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Snackbar.make(
                        binding.root,
                        body?.message ?: "Failed to delete payment",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                binding.progressDetails.visibility = View.GONE
                Snackbar.make(
                    binding.root,
                    t.message ?: "Something went wrong",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        })
    }
}
