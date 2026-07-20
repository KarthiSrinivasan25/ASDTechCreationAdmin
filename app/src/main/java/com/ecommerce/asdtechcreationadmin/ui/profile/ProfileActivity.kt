package com.ecommerce.asdtechcreationadmin.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.ecommerce.asdtechcreationadmin.R
import com.ecommerce.asdtechcreationadmin.api.ApiClient
import com.ecommerce.asdtechcreationadmin.data.model.CompanyProfile
import com.ecommerce.asdtechcreationadmin.data.model.CompanyProfileGetResponse
import com.ecommerce.asdtechcreationadmin.data.model.CompanyProfileSaveResponse
import com.ecommerce.asdtechcreationadmin.databinding.ActivityProfileBinding
import com.ecommerce.asdtechcreationadmin.ui.signature.SignatureActivity
import com.ecommerce.asdtechcreationadmin.ui.signature.SignaturePickerDialog
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    // Signature IDs used when saving the profile.
    private var signature1Id: Int? = null
    private var signature2Id: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnSaveProfile.setOnClickListener { saveProfile() }

        binding.btnManageSignatures.setOnClickListener {
            startActivity(Intent(this, SignatureActivity::class.java))
        }

        binding.rowSignature1.setOnClickListener {
            SignaturePickerDialog.show(this, binding.root, signature1Id) { signature ->
                signature1Id = signature.id
                binding.txtSignature1Name.text = signature.name
                binding.txtSignature1Name.setTextColor(
                    ContextCompat.getColor(this, R.color.text_primary)
                )
                Glide.with(this)
                    .load(signature.image_url)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .into(binding.imgSignature1)
            }
        }

        binding.rowSignature2.setOnClickListener {
            SignaturePickerDialog.show(this, binding.root, signature2Id) { signature ->
                signature2Id = signature.id
                binding.txtSignature2Name.text = signature.name
                binding.txtSignature2Name.setTextColor(
                    ContextCompat.getColor(this, R.color.text_primary)
                )
                Glide.with(this)
                    .load(signature.image_url)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .into(binding.imgSignature2)
            }
        }

        loadProfile()
    }

    private fun loadProfile() {

        setLoading(true)

        ApiClient.apiService.getCompanyProfile()
            .enqueue(object : Callback<CompanyProfileGetResponse> {

                override fun onResponse(
                    call: Call<CompanyProfileGetResponse>,
                    response: Response<CompanyProfileGetResponse>
                ) {

                    setLoading(false)

                    val body = response.body()

                    if (response.isSuccessful && body?.status == true && body.data != null) {
                        bindProfile(body.data)
                    } else if (response.isSuccessful && body?.status == false) {
                        // No profile saved yet — leave the form empty for first-time setup.
                    } else {
                        showNotification(
                            body?.message ?: "Unable to load company profile",
                            isSuccess = false
                        )
                    }
                }

                override fun onFailure(call: Call<CompanyProfileGetResponse>, t: Throwable) {
                    setLoading(false)
                    showNotification(
                        t.message ?: "Something went wrong",
                        isSuccess = false
                    )
                }
            })
    }

    private fun bindProfile(profile: CompanyProfile) {

        binding.etCompanyName.setText(profile.company_name ?: "")
        binding.etOwnerName.setText(profile.owner_name ?: "")
        binding.etEmail.setText(profile.email ?: "")
        binding.etPhone.setText(profile.phone ?: "")
        binding.etAddress.setText(profile.address ?: "")
        binding.etCity.setText(profile.city ?: "")
        binding.etState.setText(profile.state ?: "")
        binding.etPincode.setText(profile.pincode ?: "")
        binding.etGstNumber.setText(profile.gst_number ?: "")
        binding.etPanNumber.setText(profile.pan_number ?: "")
        binding.etWebsite.setText(profile.website ?: "")

        binding.etBankName.setText(profile.bank_name ?: "")
        binding.etAccountHolder.setText(profile.account_holder ?: "")
        binding.etAccountNumber.setText(profile.account_number ?: "")
        binding.etIfscCode.setText(profile.ifsc_code ?: "")
        binding.etUpiId.setText(profile.upi_id ?: "")

        binding.etInvoicePrefix.setText(profile.invoice_prefix ?: "INV")
        binding.etInvoiceDueDays.setText(
            (profile.invoice_due_days ?: 15).toString()
        )

        signature1Id = profile.signature_1_id
        signature2Id = profile.signature_2_id

        if (!profile.signature_1_name.isNullOrEmpty()) {
            binding.txtSignature1Name.text = profile.signature_1_name
            binding.txtSignature1Name.setTextColor(
                ContextCompat.getColor(this, R.color.text_primary)
            )
            Glide.with(this)
                .load(profile.signature_1_image_url)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(binding.imgSignature1)
        }

        if (!profile.signature_2_name.isNullOrEmpty()) {
            binding.txtSignature2Name.text = profile.signature_2_name
            binding.txtSignature2Name.setTextColor(
                ContextCompat.getColor(this, R.color.text_primary)
            )
            Glide.with(this)
                .load(profile.signature_2_image_url)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(binding.imgSignature2)
        }
    }

    private fun saveProfile() {

        val companyName = binding.etCompanyName.text.toString().trim()

        if (companyName.isEmpty()) {
            binding.etCompanyName.error = "Enter company name"
            return
        }

        val email = binding.etEmail.text.toString().trim()
        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Enter a valid email"
            return
        }

        val request = CompanyProfile(
            company_name = companyName,
            owner_name = binding.etOwnerName.text.toString().trim(),
            email = email,
            phone = binding.etPhone.text.toString().trim(),
            address = binding.etAddress.text.toString().trim(),
            city = binding.etCity.text.toString().trim(),
            state = binding.etState.text.toString().trim(),
            pincode = binding.etPincode.text.toString().trim(),
            gst_number = binding.etGstNumber.text.toString().trim(),
            pan_number = binding.etPanNumber.text.toString().trim(),
            website = binding.etWebsite.text.toString().trim(),
            bank_name = binding.etBankName.text.toString().trim(),
            account_holder = binding.etAccountHolder.text.toString().trim(),
            account_number = binding.etAccountNumber.text.toString().trim(),
            ifsc_code = binding.etIfscCode.text.toString().trim(),
            upi_id = binding.etUpiId.text.toString().trim(),
            invoice_prefix = binding.etInvoicePrefix.text.toString().trim().ifEmpty { "INV" },
            invoice_due_days = binding.etInvoiceDueDays.text.toString().trim()
                .toIntOrNull() ?: 15,
            signature_1_id = signature1Id,
            signature_2_id = signature2Id
        )

        setLoading(true)

        ApiClient.apiService.saveCompanyProfile(request)
            .enqueue(object : Callback<CompanyProfileSaveResponse> {

                override fun onResponse(
                    call: Call<CompanyProfileSaveResponse>,
                    response: Response<CompanyProfileSaveResponse>
                ) {

                    setLoading(false)

                    val body = response.body()

                    if (response.isSuccessful && body?.status == true) {
                        showNotification(
                            body.message ?: "Company profile saved successfully",
                            isSuccess = true
                        )
                    } else {
                        showNotification(
                            body?.message ?: "Failed to save company profile",
                            isSuccess = false
                        )
                    }
                }

                override fun onFailure(
                    call: Call<CompanyProfileSaveResponse>,
                    t: Throwable
                ) {
                    setLoading(false)
                    showNotification(
                        t.message ?: "Something went wrong. Please try again",
                        isSuccess = false
                    )
                }
            })
    }

    private fun setLoading(loading: Boolean) {

        binding.btnSaveProfile.isEnabled = !loading
        binding.btnSaveProfile.text = if (loading) "" else "Save Profile"
        binding.progressProfile.visibility = if (loading) View.VISIBLE else View.GONE

        val fields = listOf(
            binding.etCompanyName, binding.etOwnerName, binding.etEmail, binding.etPhone,
            binding.etAddress, binding.etCity, binding.etState, binding.etPincode,
            binding.etGstNumber, binding.etPanNumber, binding.etWebsite,
            binding.etBankName, binding.etAccountHolder, binding.etAccountNumber,
            binding.etIfscCode, binding.etUpiId, binding.etInvoicePrefix, binding.etInvoiceDueDays
        )

        fields.forEach { it.isEnabled = !loading }
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
