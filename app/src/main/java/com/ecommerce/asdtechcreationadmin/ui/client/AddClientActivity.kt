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
import com.ecommerce.asdtechcreationadmin.data.model.SaveClientResponse
import com.ecommerce.asdtechcreationadmin.databinding.ActivityAddClientBinding
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddClientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddClientBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnUploadLogo.setOnClickListener {
            showNotification("Logo upload coming soon", isSuccess = true)
        }

        binding.btnSaveClient.setOnClickListener {
            saveClient()
        }
    }

    private fun saveClient() {

        val name = binding.etClientName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val company = binding.etCompanyName.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val website = binding.etWebsite.text.toString().trim()

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

        val notes = if (website.isNotEmpty()) "Website: $website" else ""

        setLoading(true)

        ApiClient.apiService.saveClient(
            clientName = name,
            companyName = company,
            email = email,
            phone = phone,
            address = address,
            notes = notes,
            status = "Active"
        ).enqueue(object : Callback<SaveClientResponse> {

            override fun onResponse(
                call: Call<SaveClientResponse>,
                response: Response<SaveClientResponse>
            ) {

                setLoading(false)

                val body = response.body()

                if (response.isSuccessful && body?.status == "success") {

                    showNotification(
                        body.message ?: "Client added successfully",
                        isSuccess = true
                    )

                    binding.root.postDelayed({
                        setResult(Activity.RESULT_OK)
                        finish()
                    }, 600)

                } else {
                    showNotification(
                        body?.message ?: "Failed to save client",
                        isSuccess = false
                    )
                }
            }

            override fun onFailure(call: Call<SaveClientResponse>, t: Throwable) {
                setLoading(false)
                showNotification(
                    t.message ?: "Something went wrong. Please try again",
                    isSuccess = false
                )
            }
        })
    }

    private fun setLoading(loading: Boolean) {

        binding.btnSaveClient.isEnabled = !loading
        binding.etClientName.isEnabled = !loading
        binding.etEmail.isEnabled = !loading
        binding.etPhone.isEnabled = !loading
        binding.etCompanyName.isEnabled = !loading
        binding.etAddress.isEnabled = !loading
        binding.etWebsite.isEnabled = !loading

        binding.btnSaveClient.text = if (loading) "" else "Save Client"
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
