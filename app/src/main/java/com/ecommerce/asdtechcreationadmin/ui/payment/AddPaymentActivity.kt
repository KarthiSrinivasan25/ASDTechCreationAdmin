package com.ecommerce.asdtechcreationadmin.ui.payment

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ecommerce.asdtechcreationadmin.R
import com.ecommerce.asdtechcreationadmin.api.ApiClient
import com.ecommerce.asdtechcreationadmin.data.model.SavePaymentResponse
import com.ecommerce.asdtechcreationadmin.databinding.ActivityAddPaymentBinding
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddPaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPaymentBinding

    private var selectedClientId: Int? = null
    private var selectedMethod: String = "Cash"
    private var selectedDate: String = ""

    private lateinit var methodChips: List<TextView>

    private val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        methodChips = listOf(
            binding.methodCash, binding.methodUpi,
            binding.methodBank, binding.methodCheque, binding.methodCard
        )

        binding.btnBack.setOnClickListener { finish() }

        binding.rowClient.setOnClickListener {
            ClientPickerDialog.show(this, binding.root) { client ->
                selectedClientId = client.id
                binding.txtSelectedClient.text = client.client_name
                binding.txtSelectedClient.setTextColor(
                    ContextCompat.getColor(this, R.color.text_primary)
                )
            }
        }

        binding.rowPaymentDate.setOnClickListener { showDatePicker() }

        binding.methodCash.setOnClickListener { selectMethod(binding.methodCash, "Cash") }
        binding.methodUpi.setOnClickListener { selectMethod(binding.methodUpi, "UPI") }
        binding.methodBank.setOnClickListener { selectMethod(binding.methodBank, "Bank Transfer") }
        binding.methodCheque.setOnClickListener { selectMethod(binding.methodCheque, "Cheque") }
        binding.methodCard.setOnClickListener { selectMethod(binding.methodCard, "Card") }

        binding.btnSavePayment.setOnClickListener { savePayment() }
    }

    private fun selectMethod(selected: TextView, method: String) {

        selectedMethod = method

        for (chip in methodChips) {
            if (chip == selected) {
                chip.setBackgroundResource(R.drawable.bg_filter_chip_selected)
                chip.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                chip.setBackgroundResource(R.drawable.bg_filter_chip_unselected)
                chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            }
        }
    }

    private fun showDatePicker() {

        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                selectedDate = apiFormat.format(calendar.time)
                binding.txtPaymentDate.text = displayFormat.format(calendar.time)
                binding.txtPaymentDate.setTextColor(
                    ContextCompat.getColor(this, R.color.text_primary)
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun savePayment() {

        val clientId = selectedClientId
        if (clientId == null) {
            Snackbar.make(binding.root, "Please select a client", Snackbar.LENGTH_LONG).show()
            return
        }

        val invoiceIdText = binding.etInvoiceId.text.toString().trim()
        val invoiceId = invoiceIdText.toIntOrNull()
        if (invoiceId == null || invoiceId <= 0) {
            binding.etInvoiceId.error = "Enter a valid invoice ID"
            return
        }

        if (selectedDate.isEmpty()) {
            Snackbar.make(binding.root, "Please select a payment date", Snackbar.LENGTH_LONG).show()
            return
        }

        val amountText = binding.etAmountPaid.text.toString().trim()
        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.etAmountPaid.error = "Enter a valid amount"
            return
        }

        val transactionId = binding.etTransactionId.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()

        setLoading(true)

        ApiClient.apiService.savePayment(
            clientId = clientId,
            invoiceId = invoiceId,
            paymentDate = selectedDate,
            amountPaid = amountText,
            paymentMethod = selectedMethod,
            transactionId = transactionId,
            notes = notes
        ).enqueue(object : Callback<SavePaymentResponse> {

            override fun onResponse(
                call: Call<SavePaymentResponse>,
                response: Response<SavePaymentResponse>
            ) {

                setLoading(false)

                val body = response.body()

                if (response.isSuccessful && body?.status == "success") {

                    showNotification(
                        body.message ?: "Payment saved successfully",
                        isSuccess = true
                    )

                    binding.root.postDelayed({
                        setResult(Activity.RESULT_OK)
                        finish()
                    }, 600)

                } else {
                    showNotification(
                        body?.message ?: "Failed to save payment",
                        isSuccess = false
                    )
                }
            }

            override fun onFailure(call: Call<SavePaymentResponse>, t: Throwable) {
                setLoading(false)
                showNotification(
                    t.message ?: "Something went wrong. Please try again",
                    isSuccess = false
                )
            }
        })
    }

    private fun setLoading(loading: Boolean) {

        binding.btnSavePayment.isEnabled = !loading
        binding.btnSavePayment.text = if (loading) "" else "Save Payment"
        binding.progressSavePayment.visibility = if (loading) View.VISIBLE else View.GONE

        binding.rowClient.isEnabled = !loading
        binding.rowPaymentDate.isEnabled = !loading
        binding.etInvoiceId.isEnabled = !loading
        binding.etAmountPaid.isEnabled = !loading
        binding.etTransactionId.isEnabled = !loading
        binding.etNotes.isEnabled = !loading
    }

    private fun showNotification(message: String, isSuccess: Boolean) {

        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view

        val colorRes = if (isSuccess) R.color.accent_green else R.color.accent_red

        snackbarView.setBackgroundColor(
            ContextCompat.getColor(this, colorRes)
        )

        val textView = snackbarView.findViewById<TextView>(
            com.google.android.material.R.id.snackbar_text
        )
        textView.setTextColor(ContextCompat.getColor(this, R.color.white))
        textView.gravity = Gravity.CENTER

        snackbar.show()
    }
}
