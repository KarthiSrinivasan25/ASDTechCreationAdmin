package com.ecommerce.asdtechcreationadmin.ui.common

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.ecommerce.asdtechcreationadmin.api.ApiClient
import com.ecommerce.asdtechcreationadmin.data.model.ClientInvoiceOption
import com.ecommerce.asdtechcreationadmin.data.model.ClientInvoiceOptionsResponse
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

/**
 * Single-select dialog listing a client's outstanding (Pending/Partial)
 * invoices, used when recording a payment against one of them.
 */
object InvoicePickerDialog {

    fun show(
        context: Context,
        rootView: View,
        clientId: Int,
        onSelected: (ClientInvoiceOption) -> Unit
    ) {

        val loadingDialog = AlertDialog.Builder(context)
            .setTitle("Loading invoices…")
            .setCancelable(true)
            .create()
        loadingDialog.show()

        ApiClient.apiService.getClientInvoices(clientId)
            .enqueue(object : Callback<ClientInvoiceOptionsResponse> {

                override fun onResponse(
                    call: Call<ClientInvoiceOptionsResponse>,
                    response: Response<ClientInvoiceOptionsResponse>
                ) {

                    loadingDialog.dismiss()

                    val invoices = response.body()?.data ?: emptyList()

                    if (!response.isSuccessful || response.body()?.status != "success" || invoices.isEmpty()) {
                        Snackbar.make(
                            rootView,
                            "This client has no pending invoices",
                            Snackbar.LENGTH_LONG
                        ).show()
                        return
                    }

                    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))

                    val labels = invoices.map {
                        "${it.invoice_number}  •  Balance ₹${formatter.format(it.balance_amount)}  •  Due ${it.due_date}"
                    }.toTypedArray()

                    AlertDialog.Builder(context)
                        .setTitle("Select Invoice")
                        .setItems(labels) { _, index ->
                            onSelected(invoices[index])
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }

                override fun onFailure(call: Call<ClientInvoiceOptionsResponse>, t: Throwable) {
                    loadingDialog.dismiss()
                    Snackbar.make(
                        rootView,
                        t.message ?: "Unable to load invoices",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            })
    }
}
