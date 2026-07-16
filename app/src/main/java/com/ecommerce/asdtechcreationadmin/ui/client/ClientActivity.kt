package com.ecommerce.asdtechcreationadmin.ui.client

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecommerce.asdtechcreationadmin.R
import com.ecommerce.asdtechcreationadmin.api.ApiClient
import com.ecommerce.asdtechcreationadmin.data.model.Client
import com.ecommerce.asdtechcreationadmin.data.model.ClientsResponse
import com.ecommerce.asdtechcreationadmin.databinding.ActivityClientBinding
import com.ecommerce.asdtechcreationadmin.ui.adapter.ClientAdapter
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientBinding
    private lateinit var adapter: ClientAdapter

    private var allClients: List<Client> = emptyList()
    private var selectedStatus: String = "All"
    private var searchQuery: String = ""

    private lateinit var tabs: List<TextView>

    private val addClientLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadClients()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tabs = listOf(
            binding.tabAll,
            binding.tabActive,
            binding.tabCompleted,
            binding.tabOnHold
        )

        binding.rvClients.layoutManager = LinearLayoutManager(this)
        adapter = ClientAdapter(emptyList()) { client, anchor ->
            showClientMenu(client, anchor)
        }
        binding.rvClients.adapter = adapter

        binding.btnBack.setOnClickListener { finish() }

        binding.fabAddClient.setOnClickListener {
            addClientLauncher.launch(
                Intent(this, AddClientActivity::class.java)
            )
        }

        setupSearch()
        setupTabs()
        loadClients()
    }

    override fun onResume() {
        super.onResume()
        loadClients()
    }

    private fun setupSearch() {

        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
                searchQuery = s?.toString()?.trim() ?: ""
                applyFilter()
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupTabs() {

        binding.tabAll.setOnClickListener { selectTab(binding.tabAll, "All") }
        binding.tabActive.setOnClickListener { selectTab(binding.tabActive, "Active") }
        binding.tabCompleted.setOnClickListener { selectTab(binding.tabCompleted, "Completed") }
        binding.tabOnHold.setOnClickListener { selectTab(binding.tabOnHold, "On Hold") }
    }

    private fun selectTab(selected: TextView, status: String) {

        selectedStatus = status

        for (tab in tabs) {

            if (tab == selected) {
                tab.setBackgroundResource(R.drawable.bg_filter_chip_selected)
                tab.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                tab.setBackgroundResource(R.drawable.bg_filter_chip_unselected)
                tab.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            }
        }

        applyFilter()
    }

    private fun loadClients() {

        binding.progressClients.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE

        ApiClient.apiService.getClients().enqueue(object : Callback<ClientsResponse> {

            override fun onResponse(
                call: Call<ClientsResponse>,
                response: Response<ClientsResponse>
            ) {

                binding.progressClients.visibility = View.GONE

                if (response.isSuccessful && response.body()?.status == "success") {
                    allClients = response.body()?.data ?: emptyList()
                    applyFilter()
                } else {
                    allClients = emptyList()
                    applyFilter()
                    showError("Unable to load clients")
                }
            }

            override fun onFailure(call: Call<ClientsResponse>, t: Throwable) {

                binding.progressClients.visibility = View.GONE
                showError(t.message ?: "Something went wrong")
            }
        })
    }

    private fun applyFilter() {

        var filtered = allClients

        if (selectedStatus != "All") {
            filtered = filtered.filter {
                val status = it.status?.trim()?.ifEmpty { "Active" } ?: "Active"
                status.equals(selectedStatus, ignoreCase = true) ||
                        (selectedStatus == "On Hold" && status.equals("OnHold", ignoreCase = true))
            }
        }

        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter {
                it.client_name.contains(searchQuery, ignoreCase = true) ||
                        it.email.contains(searchQuery, ignoreCase = true) ||
                        it.phone.contains(searchQuery, ignoreCase = true)
            }
        }

        adapter.submitList(filtered)
        binding.emptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showClientMenu(client: Client, anchor: View) {

        val popup = PopupMenu(this, anchor)
        popup.menu.add("View Details")
        popup.menu.add("Edit")
        popup.menu.add("Delete")

        popup.setOnMenuItemClickListener {
            Snackbar.make(
                binding.root,
                "\"${it.title}\" — coming soon",
                Snackbar.LENGTH_SHORT
            ).show()
            true
        }

        popup.show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .show()
    }
}
