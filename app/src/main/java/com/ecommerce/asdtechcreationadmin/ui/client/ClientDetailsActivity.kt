package com.ecommerce.asdtechcreationadmin.ui.client

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ecommerce.asdtechcreationadmin.R
import com.ecommerce.asdtechcreationadmin.api.ApiClient
import com.ecommerce.asdtechcreationadmin.data.model.Client
import com.ecommerce.asdtechcreationadmin.data.model.SimpleResponse
import com.ecommerce.asdtechcreationadmin.data.model.SingleClientResponse
import com.ecommerce.asdtechcreationadmin.databinding.ActivityClientDetailsBinding
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CLIENT_ID = "extra_client_id"
    }

    private lateinit var binding: ActivityClientDetailsBinding
    private var clientId: Int = -1
    private var currentClient: Client? = null

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadClient()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityClientDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clientId = intent.getIntExtra(EXTRA_CLIENT_ID, -1)

        if (clientId == -1) {
            Snackbar.make(binding.root, "Invalid client", Snackbar.LENGTH_LONG).show()
            finish()
            return
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnEdit.setOnClickListener {
            editLauncher.launch(
                Intent(this, EditClientActivity::class.java)
                    .putExtra(EditClientActivity.EXTRA_CLIENT_ID, clientId)
            )
        }

        binding.btnDelete.setOnClickListener { confirmDelete() }

        binding.btnCall.setOnClickListener {
            currentClient?.phone?.let { phone ->
                if (phone.isNotBlank()) {
                    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                }
            }
        }

        binding.btnEmail.setOnClickListener {
            currentClient?.email?.let { email ->
                if (email.isNotBlank()) {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                    startActivity(Intent.createChooser(intent, "Send email"))
                }
            }
        }

        loadClient()
    }

    private fun loadClient() {

        binding.progressDetails.visibility = View.VISIBLE

        ApiClient.apiService.getClient(clientId).enqueue(object : Callback<SingleClientResponse> {

            override fun onResponse(
                call: Call<SingleClientResponse>,
                response: Response<SingleClientResponse>
            ) {

                binding.progressDetails.visibility = View.GONE

                val body = response.body()

                if (response.isSuccessful && body?.status == "success" && body.data != null) {
                    currentClient = body.data
                    bindClient(body.data)
                } else {
                    Snackbar.make(
                        binding.root,
                        body?.message ?: "Unable to load client",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<SingleClientResponse>, t: Throwable) {
                binding.progressDetails.visibility = View.GONE
                Snackbar.make(
                    binding.root,
                    t.message ?: "Something went wrong",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun bindClient(client: Client) {

        binding.txtClientName.text = client.client_name
        binding.txtAvatarInitial.text =
            client.client_name.trim().take(1).uppercase().ifEmpty { "?" }

        binding.txtCompanyName.text =
            client.company_name?.ifEmpty { "—" } ?: "—"

        binding.txtPhone.text = client.phone.ifEmpty { "—" }
        binding.txtEmail.text = client.email.ifEmpty { "—" }
        binding.txtAddress.text = client.address?.ifEmpty { "—" } ?: "—"
        binding.txtGst.text = client.gst_number?.ifEmpty { "—" } ?: "—"

        val project = client.project_name?.ifEmpty { null }
        val service = client.service?.ifEmpty { null }

        binding.txtProject.text = when {
            project != null && service != null -> "$project — $service"
            project != null -> project
            service != null -> service
            else -> "—"
        }

        binding.txtProjectValue.text = "₹${formatAmount(client.project_value)}"

        val status = client.status?.trim()?.ifEmpty { "Active" } ?: "Active"
        binding.txtStatusBadge.text = status

        when (status.lowercase()) {
            "active" -> {
                binding.txtStatusBadge.setBackgroundResource(R.drawable.bg_chip_green)
                binding.txtStatusBadge.setTextColor(
                    ContextCompat.getColor(this, R.color.accent_green)
                )
            }
            "completed" -> {
                binding.txtStatusBadge.setBackgroundResource(R.drawable.bg_chip_blue)
                binding.txtStatusBadge.setTextColor(
                    ContextCompat.getColor(this, R.color.accent_blue)
                )
            }
            else -> {
                binding.txtStatusBadge.setBackgroundResource(R.drawable.bg_chip_orange)
                binding.txtStatusBadge.setTextColor(
                    ContextCompat.getColor(this, R.color.accent_orange)
                )
            }
        }

        val notes = client.notes?.trim()
        if (!notes.isNullOrEmpty()) {
            binding.cardNotes.visibility = View.VISIBLE
            binding.txtNotes.text = notes
        } else {
            binding.cardNotes.visibility = View.GONE
        }
    }

    private fun formatAmount(value: Double?): String {
        val amount = value ?: 0.0
        return "%,.0f".format(amount)
    }

    private fun confirmDelete() {

        AlertDialog.Builder(this)
            .setTitle("Delete Client")
            .setMessage("Are you sure you want to delete this client? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteClient() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteClient() {

        binding.progressDetails.visibility = View.VISIBLE

        ApiClient.apiService.deleteClient(clientId).enqueue(object : Callback<SimpleResponse> {

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
                        body?.message ?: "Failed to delete client",
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
