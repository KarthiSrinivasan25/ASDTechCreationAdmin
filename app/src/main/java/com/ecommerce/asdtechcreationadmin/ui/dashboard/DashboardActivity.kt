package com.ecommerce.asdtechcreationadmin.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecommerce.asdtechcreationadmin.R
import com.ecommerce.asdtechcreationadmin.api.ApiClient
import com.ecommerce.asdtechcreationadmin.api.ApiService
import com.ecommerce.asdtechcreationadmin.data.model.DashboardResponse
import com.ecommerce.asdtechcreationadmin.data.model.MonthlyRevenue
import com.ecommerce.asdtechcreationadmin.databinding.ActivityDashboardBinding
import com.ecommerce.asdtechcreationadmin.session.SessionManager
import com.ecommerce.asdtechcreationadmin.ui.adapter.PendingClientAdapter
import com.ecommerce.asdtechcreationadmin.ui.adapter.RecentInvoiceAdapter
import com.ecommerce.asdtechcreationadmin.ui.adapter.RecentPaymentAdapter
import com.ecommerce.asdtechcreationadmin.ui.common.BottomNavHelper
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import android.os.Handler
import android.os.Looper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager

    private lateinit var invoiceAdapter: RecentInvoiceAdapter
    private lateinit var paymentAdapter: RecentPaymentAdapter
    private lateinit var pendingAdapter: PendingClientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = ApiClient.apiService
        sessionManager = SessionManager(this)

        setupHeader()
        setupRecyclerViews()
        setupBottomNavigation()
        loadDashboard()
    }


    private val timeHandler = Handler(Looper.getMainLooper())

private val timeRunnable = object : Runnable {
    override fun run() {

        val dateFormat = SimpleDateFormat(
            "EEE, dd MMM\nhh:mm:ss a",
            Locale.getDefault()
        )

        binding.dashboardHeader.txtDateTime.text =
            dateFormat.format(Date())

        timeHandler.postDelayed(this, 1000)
    }
}

    private fun setupHeader() {

    val name = sessionManager.getName()
    val displayName = if (name.isNotBlank()) name else "Admin"

    binding.dashboardHeader.txtWelcome.text =
        "Welcome, $displayName"

      binding.dashboardHeader.imgCompanyLogo.setImageResource(
        R.drawable.company_logo
    )



    val hour = Calendar.getInstance()
        .get(Calendar.HOUR_OF_DAY)

   binding.dashboardHeader.txtGreetingTime.text =
    when {
        hour < 12 -> "Good Morning 👋"
        hour < 17 -> "Good Afternoon 👋"
        hour < 21 -> "Good Evening 👋"
        else -> "Good Night 🌙"
    }


    // Start live clock
    timeHandler.post(timeRunnable)
}

    private fun setupRecyclerViews() {

        invoiceAdapter = RecentInvoiceAdapter(ArrayList())
        paymentAdapter = RecentPaymentAdapter(ArrayList())
        pendingAdapter = PendingClientAdapter(ArrayList())

        binding.recentActivities.rvRecentInvoices.layoutManager =
            LinearLayoutManager(this)
        binding.recentActivities.rvRecentInvoices.adapter =
            invoiceAdapter

        binding.recentActivities.rvRecentPayments.layoutManager =
            LinearLayoutManager(this)
        binding.recentActivities.rvRecentPayments.adapter =
            paymentAdapter

        binding.recentActivities.rvPendingClients.layoutManager =
            LinearLayoutManager(this)
        binding.recentActivities.rvPendingClients.adapter =
            pendingAdapter
    }

    private fun setupBottomNavigation() {

    BottomNavHelper.setup(
        this,
        binding.bottomNavigation,
        R.id.nav_dashboard
    )

}

    private fun loadDashboard() {

        apiService.getDashboard().enqueue(object : Callback<DashboardResponse> {

            override fun onResponse(
                call: Call<DashboardResponse>,
                response: Response<DashboardResponse>
            ) {

                if (response.isSuccessful &&
                    response.body() != null &&
                    response.body()!!.status
                ) {

                    val dashboard = response.body()!!

                    // Dashboard Cards

                    binding.dashboardCards.txtTotalClients.text =
                        dashboard.dashboard.total_clients.toString()

                    binding.dashboardCards.txtTotalInvoices.text =
                        dashboard.dashboard.total_invoices.toString()

                    binding.dashboardCards.txtPendingPayments.text =
                        "₹${dashboard.dashboard.pending_amount}"

                    binding.dashboardCards.txtPaidPayments.text =
                        "₹${dashboard.dashboard.paid_amount}"

                    binding.revenueChart.txtRevenue.text =
                        "₹${dashboard.dashboard.invoice_amount}"

                    // RecyclerViews

                    invoiceAdapter.updateData(
                        dashboard.recent_invoices
                    )

                    paymentAdapter.updateData(
                        dashboard.recent_payments
                    )

                    pendingAdapter.updateData(
                        dashboard.pending_clients
                    )

                    // Revenue Chart

                    loadRevenueChart(
                        dashboard.charts.monthly_revenue
                    )

                } else {

                    Toast.makeText(
                        this@DashboardActivity,
                        "Failed to load dashboard",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(
                call: Call<DashboardResponse>,
                t: Throwable
            ) {

                Toast.makeText(
                    this@DashboardActivity,
                    t.localizedMessage ?: "Something went wrong",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun loadRevenueChart(
        revenueList: List<MonthlyRevenue>
    ) {

        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        for (i in revenueList.indices) {

            entries.add(
                BarEntry(
                    i.toFloat(),
                    revenueList[i].amount.toFloat()
                )
            )

            labels.add(revenueList[i].month)
        }

        val dataSet = BarDataSet(entries, "Revenue")

        dataSet.valueTextSize = 10f
        dataSet.color = Color.parseColor("#2563EB")
        dataSet.valueTextColor = Color.parseColor("#111827")
        dataSet.setDrawValues(true)

        val data = BarData(dataSet)
        data.barWidth = 0.55f

        val chart = binding.revenueChart.barChart

        chart.data = data

        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setFitBars(true)
        chart.setExtraOffsets(0f, 4f, 0f, 4f)
        chart.animateY(1000)

        chart.axisRight.isEnabled = false

        chart.axisLeft.setDrawGridLines(true)
        chart.axisLeft.gridColor = Color.parseColor("#EEF0F5")
        chart.axisLeft.axisLineColor = Color.parseColor("#EEF0F5")
        chart.axisLeft.textColor = Color.parseColor("#6B7280")

        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.axisLineColor = Color.parseColor("#EEF0F5")
        chart.xAxis.textColor = Color.parseColor("#6B7280")
        chart.xAxis.granularity = 1f
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        chart.invalidate()
    }

override fun onDestroy() {
    super.onDestroy()
    timeHandler.removeCallbacks(timeRunnable)
}

}