package com.ecommerce.asdtechcreationadmin.ui.invoice

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ecommerce.asdtechcreationadmin.R
import com.ecommerce.asdtechcreationadmin.api.ApiClient
import com.ecommerce.asdtechcreationadmin.data.model.NextInvoiceNumberResponse
import com.ecommerce.asdtechcreationadmin.data.model.SaveInvoiceResponse
import com.ecommerce.asdtechcreationadmin.databinding.ActivityAddInvoiceBinding
import com.ecommerce.asdtechcreationadmin.databinding.ItemInvoiceLineBinding
import com.ecommerce.asdtechcreationadmin.ui.common.ClientPickerDialog
import com.ecommerce.asdtechcreationadmin.ui.signature.SignaturePickerDialog
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddInvoiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddInvoiceBinding

    private var selectedClientId: Int? = null
    private var selectedSignatoryId: Int? = null
    private var selectedStatus: String = "Pending"

    private var invoiceDateApi: String = ""
    private var dueDateApi: String = ""

    private val lineRows = mutableListOf<ItemInvoiceLineBinding>()
    private lateinit var statusChips: List<TextView>

    private val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val moneyFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddInvoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        statusChips = listOf(binding.statusPending, binding.statusPartial, binding.statusPaid)

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

        binding.rowInvoiceDate.setOnClickListener { pickInvoiceDate() }
        binding.rowDueDate.setOnClickListener { pickDueDate() }

        binding.rowSignatory.setOnClickListener {
            SignaturePickerDialog.show(this, binding.root, selectedSignatoryId) { signature ->
                selectedSignatoryId = signature.id
                binding.txtSelectedSignatory.text = signature.name
                binding.txtSelectedSignatory.setTextColor(
                    ContextCompat.getColor(this, R.color.text_primary)
                )
            }
        }

        binding.statusPending.setOnClickListener { selectStatus(binding.statusPending, "Pending") }
        binding.statusPartial.setOnClickListener { selectStatus(binding.statusPartial, "Partial") }
        binding.statusPaid.setOnClickListener { selectStatus(binding.statusPaid, "Paid") }

        binding.btnAddItem.setOnClickListener { addItemRow() }

        binding.etGstPercent.addTextChangedListener(recalcWatcher())
        binding.etDiscount.addTextChangedListener(recalcWatcher())
        binding.etPaidAmount.addTextChangedListener(recalcWatcher())

        binding.btnSaveInvoice.setOnClickListener { saveInvoice() }

        // Start with one blank item row.
        addItemRow()

        loadNextInvoiceNumber()
    }

    private fun loadNextInvoiceNumber() {

        ApiClient.apiService.getNextInvoiceNumber()
            .enqueue(object : Callback<NextInvoiceNumberResponse> {

                override fun onResponse(
                    call: Call<NextInvoiceNumberResponse>,
                    response: Response<NextInvoiceNumberResponse>
                ) {
                    val number = response.body()?.invoice_number
                    if (!number.isNullOrEmpty()) {
                        binding.txtNextInvoiceNumber.text = number
                    }
                }

                override fun onFailure(call: Call<NextInvoiceNumberResponse>, t: Throwable) {
                    // Non-critical — the server generates the real number on save anyway.
                }
            })
    }

    private fun pickInvoiceDate() {

        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                invoiceDateApi = apiFormat.format(calendar.time)
                binding.txtInvoiceDate.text = displayFormat.format(calendar.time)
                binding.txtInvoiceDate.setTextColor(
                    ContextCompat.getColor(this, R.color.text_primary)
                )

                // Default due date to +15 days if not already set.
                if (dueDateApi.isEmpty()) {
                    calendar.add(Calendar.DAY_OF_MONTH, 15)
                    dueDateApi = apiFormat.format(calendar.time)
                    binding.txtDueDate.text = displayFormat.format(calendar.time)
                    binding.txtDueDate.setTextColor(
                        ContextCompat.getColor(this, R.color.text_primary)
                    )
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun pickDueDate() {

        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                dueDateApi = apiFormat.format(calendar.time)
                binding.txtDueDate.text = displayFormat.format(calendar.time)
                binding.txtDueDate.setTextColor(
                    ContextCompat.getColor(this, R.color.text_primary)
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun selectStatus(selected: TextView, status: String) {

        selectedStatus = status

        for (chip in statusChips) {
            if (chip == selected) {
                chip.setBackgroundResource(R.drawable.bg_filter_chip_selected)
                chip.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                chip.setBackgroundResource(R.drawable.bg_filter_chip_unselected)
                chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            }
        }
    }

    // ==========================================
    // ITEM ROWS
    // ==========================================

    private fun addItemRow() {

        val rowBinding = ItemInvoiceLineBinding.inflate(
            LayoutInflater.from(this), binding.itemsContainer, false
        )

        rowBinding.etQuantity.setText("1")

        val watcher = recalcWatcher()
        rowBinding.etQuantity.addTextChangedListener(watcher)
        rowBinding.etUnitPrice.addTextChangedListener(watcher)

        rowBinding.btnRemoveLine.setOnClickListener {
            if (lineRows.size <= 1) {
                Snackbar.make(binding.root, "At least one item is required", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.itemsContainer.removeView(rowBinding.root)
            lineRows.remove(rowBinding)
            recalcTotals()
        }

        lineRows.add(rowBinding)
        binding.itemsContainer.addView(rowBinding.root)
        recalcTotals()
    }

    private fun recalcWatcher(): TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            recalcTotals()
        }
        override fun afterTextChanged(s: Editable?) {}
    }

    private fun recalcTotals() {

        var subtotal = 0.0

        for (row in lineRows) {
            val qty = row.etQuantity.text.toString().toDoubleOrNull() ?: 0.0
            val price = row.etUnitPrice.text.toString().toDoubleOrNull() ?: 0.0
            val lineTotal = qty * price
            row.txtLineTotal.text = "₹${moneyFormat.format(lineTotal)}"
            subtotal += lineTotal
        }

        val gstPercent = binding.etGstPercent.text.toString().toDoubleOrNull() ?: 0.0
        val gstAmount = subtotal * gstPercent / 100.0
        val discount = binding.etDiscount.text.toString().toDoubleOrNull() ?: 0.0

        var total = subtotal + gstAmount - discount
        if (total < 0) total = 0.0

        val paid = binding.etPaidAmount.text.toString().toDoubleOrNull() ?: 0.0
        var balance = total - paid
        if (balance < 0) balance = 0.0

        binding.txtSubtotal.text = "₹${moneyFormat.format(subtotal)}"
        binding.txtGstAmount.text = "₹${moneyFormat.format(gstAmount)}"
        binding.txtTotalAmount.text = "₹${moneyFormat.format(total)}"
        binding.txtBalanceAmount.text = "₹${moneyFormat.format(balance)}"
    }

    // ==========================================
    // SAVE
    // ==========================================

    private fun saveInvoice() {

        val clientId = selectedClientId
        if (clientId == null) {
            Snackbar.make(binding.root, "Please select a client", Snackbar.LENGTH_LONG).show()
            return
        }

        if (invoiceDateApi.isEmpty()) {
            Snackbar.make(binding.root, "Please select an invoice date", Snackbar.LENGTH_LONG).show()
            return
        }

        if (dueDateApi.isEmpty()) {
            Snackbar.make(binding.root, "Please select a due date", Snackbar.LENGTH_LONG).show()
            return
        }

        val signatoryId = selectedSignatoryId
        if (signatoryId == null) {
            Snackbar.make(binding.root, "Please select a signatory", Snackbar.LENGTH_LONG).show()
            return
        }

        val itemNames = mutableListOf<String>()
        val descriptions = mutableListOf<String>()
        val quantities = mutableListOf<String>()
        val unitPrices = mutableListOf<String>()
        val totals = mutableListOf<String>()

        for (row in lineRows) {

            val name = row.etItemName.text.toString().trim()
            if (name.isEmpty()) continue

            val qty = row.etQuantity.text.toString().toDoubleOrNull() ?: 0.0
            val price = row.etUnitPrice.text.toString().toDoubleOrNull() ?: 0.0
            val lineTotal = qty * price

            itemNames.add(name)
            descriptions.add(row.etItemDescription.text.toString().trim())
            quantities.add(qty.toInt().toString())
            unitPrices.add(price.toString())
            totals.add(lineTotal.toString())
        }

        if (itemNames.isEmpty()) {
            Snackbar.make(binding.root, "Add at least one item with a name", Snackbar.LENGTH_LONG).show()
            return
        }

        val subtotal = totals.sumOf { it.toDoubleOrNull() ?: 0.0 }
        val gstPercent = binding.etGstPercent.text.toString().toDoubleOrNull() ?: 0.0
        val gstAmount = subtotal * gstPercent / 100.0
        val discount = binding.etDiscount.text.toString().toDoubleOrNull() ?: 0.0
        var total = subtotal + gstAmount - discount
        if (total < 0) total = 0.0

        if (total <= 0) {
            Snackbar.make(binding.root, "Invoice amount must be greater than zero", Snackbar.LENGTH_LONG).show()
            return
        }

        val paid = binding.etPaidAmount.text.toString().toDoubleOrNull() ?: 0.0
        var balance = total - paid
        if (balance < 0) balance = 0.0

        val notes = binding.etNotes.text.toString().trim()

        setLoading(true)

        ApiClient.apiService.saveInvoice(
            clientId = clientId,
            invoiceDate = invoiceDateApi,
            dueDate = dueDateApi,
            subtotal = subtotal.toString(),
            gstPercent = gstPercent.toString(),
            gstAmount = gstAmount.toString(),
            discount = discount.toString(),
            totalAmount = total.toString(),
            paidAmount = paid.toString(),
            balanceAmount = balance.toString(),
            status = selectedStatus,
            notes = notes,
            signatory = signatoryId.toString(),
            itemNames = itemNames,
            descriptions = descriptions,
            quantities = quantities,
            unitPrices = unitPrices,
            totals = totals
        ).enqueue(object : Callback<SaveInvoiceResponse> {

            override fun onResponse(
                call: Call<SaveInvoiceResponse>,
                response: Response<SaveInvoiceResponse>
            ) {

                setLoading(false)

                val body = response.body()

                if (response.isSuccessful && body?.status == "success") {

                    showNotification(
                        body.message ?: "Invoice created successfully",
                        isSuccess = true
                    )

                    binding.root.postDelayed({
                        setResult(Activity.RESULT_OK)
                        finish()
                    }, 600)

                } else {
                    showNotification(
                        body?.message ?: "Failed to create invoice",
                        isSuccess = false
                    )
                }
            }

            override fun onFailure(call: Call<SaveInvoiceResponse>, t: Throwable) {
                setLoading(false)
                showNotification(
                    t.message ?: "Something went wrong. Please try again",
                    isSuccess = false
                )
            }
        })
    }

    private fun setLoading(loading: Boolean) {

        binding.btnSaveInvoice.isEnabled = !loading
        binding.btnSaveInvoice.text = if (loading) "" else "Save Invoice"
        binding.progressSaveInvoice.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnAddItem.isEnabled = !loading
        binding.rowClient.isEnabled = !loading
        binding.rowInvoiceDate.isEnabled = !loading
        binding.rowDueDate.isEnabled = !loading
        binding.rowSignatory.isEnabled = !loading
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
