package com.ecommerce.asdtechcreationadmin.ui.client

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
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
import com.ecommerce.asdtechcreationadmin.data.model.SimpleResponse
import com.ecommerce.asdtechcreationadmin.databinding.ActivityClientBinding
import com.ecommerce.asdtechcreationadmin.ui.adapter.ClientAdapter
import com.ecommerce.asdtechcreationadmin.ui.common.BottomNavHelper
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.ceil
import kotlin.math.min

class ClientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientBinding
    private lateinit var adapter: ClientAdapter

    private var allClients: List<Client> = emptyList()
    private var filteredClients: List<Client> = emptyList()

    private var selectedStatus: String = "All"
    private var searchQuery: String = ""
    private var currentPage: Int = 1

    private val pageSize = 10

    private lateinit var tabs: List<TextView>

    private val addClientLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadClients()
        }
    }

    private val detailsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadClients()
        }
    }

    private val editLauncher = registerForActivityResult(
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

        binding.btnRefresh.setOnClickListener {
            it.animate()
                .rotationBy(360f)
                .setDuration(500)
                .start()
            loadClients()
        }

        BottomNavHelper.setup(this, binding.bottomNavigation, R.id.nav_clients) {
            binding.drawerLayout.openDrawer(androidx.core.view.GravityCompat.START)
        }

        com.ecommerce.asdtechcreationadmin.ui.common.NavDrawerHelper.setup(
            this, binding.drawerLayout, binding.navDrawer, R.id.nav_clients
        )
        

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
                currentPage = 1
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
        currentPage = 1

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
                    binding.txtTotalClientsCount.text = allClients.size.toString()
                    applyFilter()
                } else {
                    allClients = emptyList()
                    binding.txtTotalClientsCount.text = "0"
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

        filteredClients = filtered

        val totalPages = maxOf(1, ceil(filteredClients.size / pageSize.toDouble()).toInt())
        if (currentPage > totalPages) currentPage = totalPages

        renderPage()
    }

    private fun renderPage() {

        val totalItems = filteredClients.size
        val totalPages = maxOf(1, ceil(totalItems / pageSize.toDouble()).toInt())

        val startIndex = (currentPage - 1) * pageSize
        val endIndex = min(startIndex + pageSize, totalItems)

        val pageItems = if (totalItems == 0) emptyList()
        else filteredClients.subList(startIndex, endIndex)

        adapter.submitList(pageItems)
        binding.emptyState.visibility = if (totalItems == 0) View.VISIBLE else View.GONE

        binding.txtResultsInfo.text = if (totalItems == 0) {
            "Showing 0-0 of 0 clients"
        } else {
            "Showing ${startIndex + 1}-$endIndex of $totalItems clients"
        }

        buildPagination(totalPages)
    }

    private fun buildPagination(totalPages: Int) {

        val container = binding.paginationContainer
        container.removeAllViews()

        container.addView(createArrowButton(R.drawable.ic_back, currentPage > 1) {
            currentPage--
            renderPage()
        })

        val pagesToShow = buildPageWindow(currentPage, totalPages)

        for (page in pagesToShow) {

            if (page == -1) {
                container.addView(createEllipsis())
            } else {
                container.addView(createPageButton(page))
            }
        }

        container.addView(createArrowButton(R.drawable.ic_forward, currentPage < totalPages) {
            currentPage++
            renderPage()
        })
    }

    /** Builds a compact page-number window like: 1 … 4 5 6 … 12 */
    private fun buildPageWindow(current: Int, total: Int): List<Int> {

        if (total <= 5) {
            return (1..total).toList()
        }

        val pages = mutableListOf<Int>()
        pages.add(1)

        if (current > 3) pages.add(-1)

        val start = maxOf(2, current - 1)
        val end = min(total - 1, current + 1)

        for (p in start..end) pages.add(p)

        if (current < total - 2) pages.add(-1)

        pages.add(total)

        return pages
    }

    private fun createPageButton(page: Int): TextView {

        val size = dp(36)

        val textView = TextView(this)
        textView.layoutParams = LinearLayout.LayoutParams(size, size).apply {
            marginStart = dp(4)
            marginEnd = dp(4)
        }
        textView.gravity = Gravity.CENTER
        textView.text = page.toString()
        textView.textSize = 12f
        textView.setTypeface(textView.typeface, android.graphics.Typeface.BOLD)

        if (page == currentPage) {
            textView.setBackgroundResource(R.drawable.bg_filter_chip_selected)
            textView.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            textView.setBackgroundResource(R.drawable.bg_filter_chip_unselected)
            textView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            textView.setOnClickListener {
                currentPage = page
                renderPage()
            }
        }

        return textView
    }

    private fun createEllipsis(): TextView {

        val textView = TextView(this)
        textView.layoutParams = LinearLayout.LayoutParams(
            dp(24), LinearLayout.LayoutParams.WRAP_CONTENT
        )
        textView.gravity = Gravity.CENTER
        textView.text = "…"
        textView.textSize = 14f
        textView.setTextColor(ContextCompat.getColor(this, R.color.text_muted))
        return textView
    }

    private fun createArrowButton(iconRes: Int, enabled: Boolean, onClick: () -> Unit): View {

        val size = dp(36)

        val imageView = android.widget.ImageView(this)
        imageView.layoutParams = LinearLayout.LayoutParams(size, size).apply {
            marginStart = dp(4)
            marginEnd = dp(4)
        }
        imageView.setPadding(dp(8), dp(8), dp(8), dp(8))
        imageView.setImageResource(iconRes)
        imageView.setBackgroundResource(R.drawable.bg_filter_chip_unselected)
        imageView.alpha = if (enabled) 1f else 0.4f
        imageView.isEnabled = enabled
        imageView.imageTintList = ContextCompat.getColorStateList(this, R.color.text_secondary)

        if (enabled) {
            imageView.setOnClickListener { onClick() }
        }

        return imageView
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun showClientMenu(client: Client, anchor: View) {

        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, 1, 0, "View Details")
        popup.menu.add(0, 2, 1, "Edit")
        popup.menu.add(0, 3, 2, "Delete")

        popup.setOnMenuItemClickListener { item ->

            when (item.itemId) {

                1 -> {
                    detailsLauncher.launch(
                        Intent(this, ClientDetailsActivity::class.java)
                            .putExtra(ClientDetailsActivity.EXTRA_CLIENT_ID, client.id)
                    )
                }

                2 -> {
                    editLauncher.launch(
                        Intent(this, EditClientActivity::class.java)
                            .putExtra(EditClientActivity.EXTRA_CLIENT_ID, client.id)
                    )
                }

                3 -> {
                    confirmDelete(client)
                }
            }

            true
        }

        popup.show()
    }

    private fun confirmDelete(client: Client) {

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Client")
            .setMessage("Are you sure you want to delete \"${client.client_name}\"? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteClient(client) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteClient(client: Client) {

        ApiClient.apiService.deleteClient(client.id).enqueue(object : Callback<SimpleResponse> {

            override fun onResponse(
                call: Call<SimpleResponse>,
                response: Response<SimpleResponse>
            ) {

                val body = response.body()

                if (response.isSuccessful && body?.status == "success") {
                    Snackbar.make(binding.root, "Client deleted", Snackbar.LENGTH_SHORT).show()
                    loadClients()
                } else {
                    showError(body?.message ?: "Failed to delete client")
                }
            }

            override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                showError(t.message ?: "Something went wrong")
            }
        })
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .show()
    }
}
