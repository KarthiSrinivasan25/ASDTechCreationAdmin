package com.ecommerce.asdtechcreationadmin.ui.client

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ecommerce.asdtechcreationadmin.R
import com.ecommerce.asdtechcreationadmin.api.ApiClient
import com.ecommerce.asdtechcreationadmin.data.model.Client
import com.ecommerce.asdtechcreationadmin.data.model.SimpleResponse
import com.ecommerce.asdtechcreationadmin.data.model.SingleClientResponse
import com.ecommerce.asdtechcreationadmin.databinding.ActivityAddClientBinding
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditClientActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CLIENT_ID = "extra_client_id"
    }

    private lateinit var binding: ActivityAddClientBinding

    private var clientId: Int = -1
    private var selectedStatus: String = "Active"
    private lateinit var statusChips: List<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clientId = intent.getIntExtra(EXTRA_CLIENT_ID, -1)

        if (clientId == -1) {
            Snackbar.make(binding.root, "Invalid client", Snackbar.LENGTH_LONG).show()
            finish()
            return
        }

        binding.txtScreenTitle.text = "Edit Client"
        binding.txtUploadLabel.text = "Change Logo"
        binding.btnSaveClient.text = "Update Client"

        statusChips = listOf(
            binding.statusActive,
            binding.statusCompleted,
            binding.statusOnHold
        )

        binding.btnBack.setOnClickListener { finish() }

        binding.btnUploadLogo.setOnClickListener {
            showNotification("Logo upload coming soon", isSuccess = true)
        }

        binding.statusActive.setOnClickListener { selectStatus(binding.statusActive, "Active") }
        binding.statusCompleted.setOnClickListener { selectStatus(binding.statusCompleted, "Completed") }
        binding.statusOnHold.setOnClickListener { selectStatus(binding.statusOnHold, "On Hold") }

        binding.btnSaveClient.setOnClickListener { updateClient() }

        loadClient()
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

    private fun loadClient() {

        setFormEnabled(false)
        binding.progressSave.visibility = View.VISIBLE
        binding.btnSaveClient.text = ""

        ApiClient.apiService.getClient(clientId).enqueue(object : Callback<SingleClientResponse> {

            override fun onResponse(
                call: Call<SingleClientResponse>,
                response: Response<SingleClientResponse>
            ) {

                binding.progressSave.visibility = View.GONE
                binding.btnSaveClient.text = "Update Client"
                setFormEnabled(true)

                val body = response.body()

                if (response.isSuccessful && body?.status == "success" && body.data != null) {
                    prefill(body.data)
                } else {
                    showNotification(
                        body?.message ?: "Unable to load client",
                        isSuccess = false
                    )
                }
            }

            override fun onFailure(call: Call<SingleClientResponse>, t: Throwable) {
                binding.progressSave.visibility = View.GONE
                binding.btnSaveClient.text = "Update Client"
                setFormEnabled(true)
                showNotification(
                    t.message ?: "Something went wrong",
                    isSuccess = false
                )
            }
        })
    }

    private fun prefill(client: Client) {

        binding.etClientName.setText(client.client_name)
        binding.etCompanyName.setText(client.company_name ?: "")
        binding.etEmail.setText(client.email)
        binding.etPhone.setText(client.phone)
        binding.etGstNumber.setText(client.gst_number ?: "")
        binding.etAddress.setText(client.address ?: "")
        binding.etProjectName.setText(client.project_name ?: "")
        binding.etService.setText(client.service ?: "")

        val value = client.project_value
        binding.etProjectValue.setText(
            if (value != null && value != 0.0) {
                if (value == value.toLong().toDouble()) value.toLong().toString()
                else value.toString()
            } else ""
        )

        binding.etNotes.setText(client.notes ?: "")

        val status = client.status?.trim()?.ifEmpty { "Active" } ?: "Active"
        when (status.lowercase()) {
            "completed" -> selectStatus(binding.statusCompleted, "Completed")
            "on hold", "onhold" -> selectStatus(binding.statusOnHold, "On Hold")
            else -> selectStatus(binding.statusActive, "Active")
        }
    }

    private fun updateClient() {

        val name = binding.etClientName.text.toString().trim()
        val company = binding.etCompanyName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val gstNumber = binding.etGstNumber.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val projectName = binding.etProjectName.text.toString().trim()
        val service = binding.etService.text.toString().trim()
        val projectValue = binding.etProjectValue.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()

        if (name.isEmpty()) {
            binding.etClientName.error = "Enter client name"
            return
        }

        if (email.isEmpty()) {
            binding.etEmail.error = "Enter email"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Enter a valid email"
            return
        }

        if (phone.isEmpty()) {
            binding.etPhone.error = "Enter phone number"
            return
        }

        setLoading(true)

        ApiClient.apiService.updateClient(
            id = clientId,
            clientName = name,
            companyName = company,
            email = email,
            phone = phone,
            address = address,
            gstNumber = gstNumber,
            projectName = projectName,
            service = service,
            projectValue = if (projectValue.isEmpty()) "0" else projectValue,
            status = selectedStatus,
            notes = notes
        ).enqueue(object : Callback<SimpleResponse> {

            override fun onResponse(
                call: Call<SimpleResponse>,
                response: Response<SimpleResponse>
            ) {

                setLoading(false)

                val body = response.body()

                if (response.isSuccessful && body?.status == "success") {

                    showNotification(
                        body.message ?: "Client updated successfully",
                        isSuccess = true
                    )

                    binding.root.postDelayed({
                        setResult(Activity.RESULT_OK)
                        finish()
                    }, 600)

                } else {
                    showNotification(
                        body?.message ?: "Failed to update client",
                        isSuccess = false
                    )
                }
            }

            override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                setLoading(false)
                showNotification(
                    t.message ?: "Something went wrong. Please try again",
                    isSuccess = false
                )
            }
        })
    }

    private fun setFormEnabled(enabled: Boolean) {

        binding.etClientName.isEnabled = enabled
        binding.etCompanyName.isEnabled = enabled
        binding.etEmail.isEnabled = enabled
        binding.etPhone.isEnabled = enabled
        binding.etGstNumber.isEnabled = enabled
        binding.etAddress.isEnabled = enabled
        binding.etProjectName.isEnabled = enabled
        binding.etService.isEnabled = enabled
        binding.etProjectValue.isEnabled = enabled
        binding.etNotes.isEnabled = enabled
        binding.btnSaveClient.isEnabled = enabled
    }

    private fun setLoading(loading: Boolean) {

        setFormEnabled(!loading)
        binding.btnSaveClient.text = if (loading) "" else "Update Client"
        binding.progressSave.visibility = if (loading) View.VISIBLE else View.GONE
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
