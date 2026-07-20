package com.ecommerce.asdtechcreationadmin.ui.signature

import android.content.Context
import android.view.LayoutInflater
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecommerce.asdtechcreationadmin.api.ApiClient
import com.ecommerce.asdtechcreationadmin.data.model.Signature
import com.ecommerce.asdtechcreationadmin.data.model.SignaturesResponse
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Simple single-select dialog listing all active signatures.
 * Used by the Company Profile screen to assign Signature 1 / Signature 2.
 */
object SignaturePickerDialog {

    fun show(
        context: Context,
        rootView: android.view.View,
        currentSelectedId: Int?,
        onSelected: (Signature) -> Unit
    ) {

        val container = android.widget.FrameLayout(context)
        val progress = ProgressBar(context)
        val recyclerView = RecyclerView(context)

        val padding = (16 * context.resources.displayMetrics.density).toInt()
        recyclerView.setPadding(padding, padding, padding, padding)
        recyclerView.clipToPadding = false
        recyclerView.layoutManager = LinearLayoutManager(context)

        val progressParams = android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
        )
        progressParams.gravity = android.view.Gravity.CENTER
        progressParams.topMargin = padding * 3

        container.addView(recyclerView)
        container.addView(progress, progressParams)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Select Signature")
            .setView(container)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        ApiClient.apiService.getSignatures().enqueue(object : Callback<SignaturesResponse> {

            override fun onResponse(
                call: Call<SignaturesResponse>,
                response: Response<SignaturesResponse>
            ) {

                progress.visibility = android.view.View.GONE

                val list = response.body()?.data ?: emptyList()

                if (response.isSuccessful && response.body()?.status == true && list.isNotEmpty()) {

                    val adapter = com.ecommerce.asdtechcreationadmin.ui.adapter.SignatureAdapter(
                        signatures = list,
                        selectable = true,
                        selectedId = currentSelectedId
                    ) { signature ->
                        onSelected(signature)
                        dialog.dismiss()
                    }

                    recyclerView.adapter = adapter

                } else {
                    dialog.dismiss()
                    Snackbar.make(
                        rootView,
                        "No signatures found. Add one from the Signatures screen first.",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<SignaturesResponse>, t: Throwable) {
                progress.visibility = android.view.View.GONE
                dialog.dismiss()
                Snackbar.make(
                    rootView,
                    t.message ?: "Unable to load signatures",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        })
    }
}
