package com.ecommerce.asdtechcreationadmin.ui.invoice

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ecommerce.asdtechcreationadmin.R
import com.ecommerce.asdtechcreationadmin.api.ApiClient
import com.ecommerce.asdtechcreationadmin.data.model.GetInvoiceResponse
import com.ecommerce.asdtechcreationadmin.data.model.InvoiceDetail
import com.ecommerce.asdtechcreationadmin.data.model.InvoiceIdRequest
import com.ecommerce.asdtechcreationadmin.data.model.InvoiceItemDetail
import com.ecommerce.asdtechcreationadmin.data.model.SimpleResponse
import com.ecommerce.asdtechcreationadmin.databinding.ActivityInvoiceDetailsBinding
import com.ecommerce.asdtechcreationadmin.databinding.ItemInvoiceDetailLineBinding
import com.ecommerce.asdtechcreationadmin.ui.common.LoadingDialog
import com.ecommerce.asdtechcreationadmin.ui.common.PdfHelper
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class InvoiceDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_INVOICE_ID = "extra_invoice_id"
    }

    private lateinit var binding: ActivityInvoiceDetailsBinding
    private var invoiceId: Int = -1
    private var currentInvoice: InvoiceDetail? = null
    private lateinit var loadingDialog: LoadingDialog

    private val moneyFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadInvoice()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInvoiceDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this)

        invoiceId = intent.getIntExtra(EXTRA_INVOICE_ID, -1)

        if (invoiceId == -1) {
            Snackbar.make(binding.root, "Invalid invoice", Snackbar.LENGTH_LONG).show()
            finish()
            return
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnEdit.setOnClickListener {
            editLauncher.launch(
                Intent(this, EditInvoiceActivity::class.java)
                    .putExtra(EditInvoiceActivity.EXTRA_INVOICE_ID, invoiceId)
            )
        }

        binding.btnDelete.setOnClickListener { confirmDelete() }

        binding.btnViewPdf.setOnClickListener {
            val invoice = currentInvoice ?: return@setOnClickListener
            PdfHelper.viewInvoice(this, invoiceId, invoice.invoice_number, binding.root) { loading ->
                if (loading) loadingDialog.show("Generating PDF…", "Opening invoice ${invoice.invoice_number}")
                else loadingDialog.dismiss()
            }
        }

        binding.btnDownloadPdf.setOnClickListener {
            val invoice = currentInvoice ?: return@setOnClickListener
            PdfHelper.downloadInvoiceToDevice(this, invoiceId, invoice.invoice_number, binding.root) { loading ->
                if (loading) loadingDialog.show("Downloading PDF…", "Saving to your Downloads folder")
                else loadingDialog.dismiss()
            }
        }

        binding.btnEmailPdf.setOnClickListener {
            sendInvoiceEmail()
        }

        loadInvoice()
    }

    private fun loadInvoice() {

        binding.progressDetails.visibility = View.VISIBLE

        ApiClient.apiService.getInvoice(invoiceId).enqueue(object : Callback<GetInvoiceResponse> {

            override fun onResponse(
                call: Call<GetInvoiceResponse>,
                response: Response<GetInvoiceResponse>
            ) {

                binding.progressDetails.visibility = View.GONE

                val body = response.body()

                if (response.isSuccessful && body?.status == "success" && body.invoice != null) {
                    currentInvoice = body.invoice
                    bindInvoice(body.invoice, body.items ?: emptyList())
                } else {
                    Snackbar.make(
                        binding.root,
                        body?.message ?: "Unable to load invoice",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<GetInvoiceResponse>, t: Throwable) {
                binding.progressDetails.visibility = View.GONE
                Snackbar.make(
                    binding.root,
                    t.message ?: "Something went wrong",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun bindInvoice(invoice: InvoiceDetail, items: List<InvoiceItemDetail>) {

        binding.txtInvoiceNumber.text = invoice.invoice_number
        binding.txtClientName.text = invoice.client_name ?: "—"
        binding.txtCompanyName.text = invoice.company_name?.ifEmpty { "—" } ?: "—"
        binding.txtInvoiceDates.text =
            "Invoice: ${invoice.invoice_date}   •   Due: ${invoice.due_date}"

        binding.txtClientEmail.text = invoice.email?.ifEmpty { "—" } ?: "—"
        binding.txtClientPhone.text = invoice.phone?.ifEmpty { "—" } ?: "—"
        binding.txtClientAddress.text = invoice.address?.ifEmpty { "—" } ?: "—"

        val status = invoice.status?.trim()?.ifEmpty { "Pending" } ?: "Pending"
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

        binding.txtSubtotal.text = "₹${moneyFormat.format(invoice.subtotal)}"
        binding.txtGstLabel.text = "GST (${trimNumber(invoice.gst_percent)}%)"
        binding.txtGstAmount.text = "₹${moneyFormat.format(invoice.gst_amount)}"
        binding.txtDiscount.text = "₹${moneyFormat.format(invoice.discount)}"
        binding.rowDiscount.visibility = if (invoice.discount > 0) View.VISIBLE else View.GONE
        binding.txtTotalAmount.text = "₹${moneyFormat.format(invoice.total_amount)}"
        binding.txtPaidAmount.text = "₹${moneyFormat.format(invoice.paid_amount)}"
        binding.txtBalanceAmount.text = "₹${moneyFormat.format(invoice.balance_amount)}"

        val notes = invoice.notes?.trim()
        if (!notes.isNullOrEmpty()) {
            binding.cardNotes.visibility = View.VISIBLE
            binding.txtNotes.text = notes
        } else {
            binding.cardNotes.visibility = View.GONE
        }

        binding.itemsContainer.removeAllViews()

        for (item in items) {

            val rowBinding = ItemInvoiceDetailLineBinding.inflate(
                LayoutInflater.from(this), binding.itemsContainer, false
            )

            rowBinding.txtItemName.text = item.item_name
            rowBinding.txtItemTotal.text = "₹${moneyFormat.format(item.total)}"
            rowBinding.txtItemMeta.text =
                "${item.quantity} x ₹${moneyFormat.format(item.unit_price)}"

            val description = item.description?.trim()
            if (!description.isNullOrEmpty()) {
                rowBinding.txtItemDescription.visibility = View.VISIBLE
                rowBinding.txtItemDescription.text = description
            }

            binding.itemsContainer.addView(rowBinding.root)
        }
    }

    private fun trimNumber(value: Double): String {
        return if (value == value.toLong().toDouble()) value.toLong().toString()
        else value.toString()
    }

    private fun sendInvoiceEmail() {

        val invoice = currentInvoice
        if (invoice == null) {
            Snackbar.make(binding.root, "Invoice not loaded yet", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (invoice.email.isNullOrBlank()) {
            Snackbar.make(
                binding.root,
                "This client has no email address on file",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        loadingDialog.show("Sending email…", "Emailing invoice to ${invoice.email}")

        ApiClient.apiService.sendInvoiceEmail(InvoiceIdRequest(invoiceId))
            .enqueue(object : Callback<SimpleResponse> {

                override fun onResponse(
                    call: Call<SimpleResponse>,
                    response: Response<SimpleResponse>
                ) {

                    loadingDialog.dismiss()

                    val body = response.body()

                    if (response.isSuccessful && body?.status == "success") {
                        showNotification(
                            body.message ?: "Invoice emailed successfully",
                            isSuccess = true
                        )
                    } else {
                        showNotification(
                            body?.message ?: "Failed to send invoice email",
                            isSuccess = false
                        )
                    }
                }

                override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                    loadingDialog.dismiss()
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
            .setTitle("Delete Invoice")
            .setMessage("Are you sure you want to delete this invoice? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteInvoice() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteInvoice() {

        loadingDialog.show("Deleting invoice…", "This will only take a moment")

        ApiClient.apiService.deleteInvoice(invoiceId).enqueue(object : Callback<SimpleResponse> {

            override fun onResponse(
                call: Call<SimpleResponse>,
                response: Response<SimpleResponse>
            ) {

                loadingDialog.dismiss()

                val body = response.body()

                if (response.isSuccessful && body?.status == "success") {
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Snackbar.make(
                        binding.root,
                        body?.message ?: "Failed to delete invoice",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                loadingDialog.dismiss()
                Snackbar.make(
                    binding.root,
                    t.message ?: "Something went wrong",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        })
    }
}
