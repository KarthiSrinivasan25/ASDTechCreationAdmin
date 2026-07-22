package com.ecommerce.asdtechcreationadmin.ui.invoice

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecommerce.asdtechcreationadmin.R
import com.ecommerce.asdtechcreationadmin.api.ApiClient
import com.ecommerce.asdtechcreationadmin.data.model.InvoiceListItem
import com.ecommerce.asdtechcreationadmin.data.model.InvoicesResponse
import com.ecommerce.asdtechcreationadmin.databinding.ActivityInvoiceBinding
import com.ecommerce.asdtechcreationadmin.ui.adapter.InvoiceListAdapter
import com.ecommerce.asdtechcreationadmin.ui.common.BottomNavHelper
import com.ecommerce.asdtechcreationadmin.ui.common.NavDrawerHelper
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.ceil
import kotlin.math.min

class InvoiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInvoiceBinding
    private lateinit var adapter: InvoiceListAdapter

    private var allInvoices: List<InvoiceListItem> = emptyList()
    private var filteredInvoices: List<InvoiceListItem> = emptyList()

    private var selectedStatus: String = "All"
    private var searchQuery: String = ""
    private var currentPage: Int = 1

    private val pageSize = 10

    private lateinit var tabs: List<TextView>

    private val addInvoiceLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadInvoices()
        }
    }

    private val detailsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadInvoices()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInvoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tabs = listOf(
            binding.tabAll,
            binding.tabPaid,
            binding.tabPartial,
            binding.tabPending
        )

        binding.rvInvoices.layoutManager = LinearLayoutManager(this)
        adapter = InvoiceListAdapter(emptyList()) { invoice ->
            detailsLauncher.launch(
                Intent(this, InvoiceDetailsActivity::class.java)
                    .putExtra(InvoiceDetailsActivity.EXTRA_INVOICE_ID, invoice.id)
            )
        }
        binding.rvInvoices.adapter = adapter

        binding.btnBack.setOnClickListener { finish() }

        binding.fabAddInvoice.setOnClickListener {
            addInvoiceLauncher.launch(
                Intent(this, AddInvoiceActivity::class.java)
            )
        }

        binding.btnRefresh.setOnClickListener {
            it.animate().rotationBy(360f).setDuration(500).start()
            loadInvoices()
        }

        BottomNavHelper.setup(this, binding.bottomNavigation, R.id.nav_invoice) {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        NavDrawerHelper.setup(this, binding.drawerLayout, binding.navDrawer, R.id.nav_invoice)

        setupSearch()
        setupTabs()
        loadInvoices()
    }

    override fun onResume() {
        super.onResume()
        loadInvoices()
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
        binding.tabPaid.setOnClickListener { selectTab(binding.tabPaid, "Paid") }
        binding.tabPartial.setOnClickListener { selectTab(binding.tabPartial, "Partial") }
        binding.tabPending.setOnClickListener { selectTab(binding.tabPending, "Pending") }
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

    private fun loadInvoices() {

        binding.progressInvoices.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE

        ApiClient.apiService.getInvoices().enqueue(object : Callback<InvoicesResponse> {

            override fun onResponse(
                call: Call<InvoicesResponse>,
                response: Response<InvoicesResponse>
            ) {

                binding.progressInvoices.visibility = View.GONE

                if (response.isSuccessful && response.body()?.status == "success") {
                    allInvoices = response.body()?.data ?: emptyList()
                    binding.txtTotalInvoicesCount.text = allInvoices.size.toString()
                    applyFilter()
                } else {
                    allInvoices = emptyList()
                    binding.txtTotalInvoicesCount.text = "0"
                    applyFilter()
                    showError("Unable to load invoices")
                }
            }

            override fun onFailure(call: Call<InvoicesResponse>, t: Throwable) {

                binding.progressInvoices.visibility = View.GONE
                showError(t.message ?: "Something went wrong")
            }
        })
    }

    private fun applyFilter() {

        var filtered = allInvoices

        if (selectedStatus != "All") {
            filtered = filtered.filter {
                val status = it.status?.trim()?.ifEmpty { "Pending" } ?: "Pending"
                status.equals(selectedStatus, ignoreCase = true)
            }
        }

        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter {
                it.invoice_number.contains(searchQuery, ignoreCase = true) ||
                        (it.client_name?.contains(searchQuery, ignoreCase = true) == true) ||
                        (it.company_name?.contains(searchQuery, ignoreCase = true) == true)
            }
        }

        filteredInvoices = filtered

        val totalPages = maxOf(1, ceil(filteredInvoices.size / pageSize.toDouble()).toInt())
        if (currentPage > totalPages) currentPage = totalPages

        renderPage()
    }

    private fun renderPage() {

        val totalItems = filteredInvoices.size
        val totalPages = maxOf(1, ceil(totalItems / pageSize.toDouble()).toInt())

        val startIndex = (currentPage - 1) * pageSize
        val endIndex = min(startIndex + pageSize, totalItems)

        val pageItems = if (totalItems == 0) emptyList()
        else filteredInvoices.subList(startIndex, endIndex)

        adapter.submitList(pageItems)
        binding.emptyState.visibility = if (totalItems == 0) View.VISIBLE else View.GONE

        binding.txtResultsInfo.text = if (totalItems == 0) {
            "Showing 0-0 of 0 invoices"
        } else {
            "Showing ${startIndex + 1}-$endIndex of $totalItems invoices"
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

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
