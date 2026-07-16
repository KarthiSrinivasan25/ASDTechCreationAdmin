package com.ecommerce.asdtechcreationadmin.data.model

data class DashboardResponse(

    val status: Boolean,

    val dashboard: Dashboard,

    val recent_invoices: List<RecentInvoice>,

    val recent_payments: List<RecentPayment>,

    val charts: Charts,

    val pending_clients: List<PendingClient>

)