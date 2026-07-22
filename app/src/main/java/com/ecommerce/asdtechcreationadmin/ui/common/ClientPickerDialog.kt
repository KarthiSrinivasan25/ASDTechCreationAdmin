package com.ecommerce.asdtechcreationadmin.ui.common

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.ecommerce.asdtechcreationadmin.api.ApiClient
import com.ecommerce.asdtechcreationadmin.data.model.Client
import com.ecommerce.asdtechcreationadmin.data.model.ClientsResponse
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Simple single-select dialog listing all clients, used when
 * picking which client a payment belongs to.
 */
object ClientPickerDialog {

    fun show(
        context: Context,
        rootView: View,
        onSelected: (Client) -> Unit
    ) {

        val loadingDialog = AlertDialog.Builder(context)
            .setTitle("Loading clients…")
            .setCancelable(true)
            .create()
        loadingDialog.show()

        ApiClient.apiService.getClients().enqueue(object : Callback<ClientsResponse> {

            override fun onResponse(
                call: Call<ClientsResponse>,
                response: Response<ClientsResponse>
            ) {

                loadingDialog.dismiss()

                val clients = response.body()?.data ?: emptyList()

                if (!response.isSuccessful || response.body()?.status != "success" || clients.isEmpty()) {
                    Snackbar.make(
                        rootView,
                        "No clients found. Add a client first.",
                        Snackbar.LENGTH_LONG
                    ).show()
                    return
                }

                val labels = clients.map {
                    val company = it.company_name?.takeIf { c -> c.isNotEmpty() }
                    if (company != null) "${it.client_name} — $company" else it.client_name
                }.toTypedArray()

                AlertDialog.Builder(context)
                    .setTitle("Select Client")
                    .setItems(labels) { _, index ->
                        onSelected(clients[index])
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            override fun onFailure(call: Call<ClientsResponse>, t: Throwable) {
                loadingDialog.dismiss()
                Snackbar.make(
                    rootView,
                    t.message ?: "Unable to load clients",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        })
    }
}
