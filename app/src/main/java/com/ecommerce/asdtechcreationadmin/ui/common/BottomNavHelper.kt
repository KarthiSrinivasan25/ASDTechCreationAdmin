package com.ecommerce.asdtechcreationadmin.ui.common

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.ecommerce.asdtechcreationadmin.R
import com.ecommerce.asdtechcreationadmin.ui.client.ClientActivity
import com.ecommerce.asdtechcreationadmin.ui.dashboard.DashboardActivity
import com.ecommerce.asdtechcreationadmin.ui.invoice.InvoiceActivity
import com.ecommerce.asdtechcreationadmin.ui.payment.PaymentActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Shared bottom navigation wiring so every top-level screen
 * (Dashboard, Clients, Invoice, Payments) behaves the same way.
 *
 * "Settings" is no longer its own screen — tapping it opens the
 * navigation drawer (see NavDrawerHelper) instead of navigating away,
 * so the bottom bar's checked state is left untouched for that tap.
 *
 * Dashboard is treated as the root of the stack, so switching tabs from
 * any other screen finishes that screen first (keeps the back stack shallow
 * and Back always lands you on Dashboard). Tapping the tab you're already on
 * does nothing.
 */
object BottomNavHelper {

    fun setup(
        activity: AppCompatActivity,
        bottomNav: BottomNavigationView,
        currentTabId: Int,
        onOpenDrawer: () -> Unit = {}
    ) {

        bottomNav.selectedItemId = currentTabId

        bottomNav.setOnItemSelectedListener { item ->

            if (item.itemId == R.id.nav_settings) {
                onOpenDrawer()
                return@setOnItemSelectedListener false
            }

            if (item.itemId == currentTabId) {
                return@setOnItemSelectedListener true
            }

            val targetClass: Class<*>? = when (item.itemId) {
                R.id.nav_dashboard -> DashboardActivity::class.java
                R.id.nav_clients -> ClientActivity::class.java
                R.id.nav_invoice -> InvoiceActivity::class.java
                R.id.nav_payment -> PaymentActivity::class.java
                else -> null
            }

            if (targetClass != null) {

                activity.startActivity(Intent(activity, targetClass))
                activity.overridePendingTransition(0, 0)

                if (currentTabId != R.id.nav_dashboard) {
                    activity.finish()
                }
            }

            true
        }
    }
}
