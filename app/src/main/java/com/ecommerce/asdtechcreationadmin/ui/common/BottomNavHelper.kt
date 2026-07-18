package com.ecommerce.asdtechcreationadmin.ui.common

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.ecommerce.asdtechcreationadmin.R
import com.ecommerce.asdtechcreationadmin.ui.client.ClientActivity
import com.ecommerce.asdtechcreationadmin.ui.dashboard.DashboardActivity
import com.ecommerce.asdtechcreationadmin.ui.invoice.InvoiceActivity
import com.ecommerce.asdtechcreationadmin.ui.payment.PaymentActivity
import com.ecommerce.asdtechcreationadmin.ui.settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

object BottomNavHelper {

    fun setup(
        activity: AppCompatActivity,
        bottomNav: BottomNavigationView,
        currentTabId: Int
    ) {

        bottomNav.selectedItemId = currentTabId

        // Initial selected color
        setSelectedColor(activity, bottomNav, currentTabId)


        bottomNav.setOnItemSelectedListener { item ->

            if (item.itemId == currentTabId) {
                return@setOnItemSelectedListener true
            }


            // Change selected icon color
            setSelectedColor(
                activity,
                bottomNav,
                item.itemId
            )


            val targetClass: Class<*>? = when (item.itemId) {

                R.id.nav_dashboard ->
                    DashboardActivity::class.java

                R.id.nav_clients ->
                    ClientActivity::class.java

                R.id.nav_invoice ->
                    InvoiceActivity::class.java

                R.id.nav_payment ->
                    PaymentActivity::class.java

                R.id.nav_settings ->
                    SettingsActivity::class.java

                else -> null
            }


            if (targetClass != null) {

                activity.startActivity(
                    Intent(activity, targetClass)
                )

                activity.overridePendingTransition(0, 0)


                if (currentTabId != R.id.nav_dashboard) {
                    activity.finish()
                }
            }


            true
        }
    }


    private fun setSelectedColor(
        activity: AppCompatActivity,
        bottomNav: BottomNavigationView,
        id: Int
    ) {

        val color = when(id) {

            R.id.nav_dashboard ->
                R.color.bottom_dashboard_color

            R.id.nav_clients ->
                R.color.bottom_clients_color

            R.id.nav_invoice ->
                R.color.bottom_invoice_color

            R.id.nav_payment ->
                R.color.bottom_payment_color

            R.id.nav_settings ->
                R.color.bottom_settings_color

            else ->
                R.color.bottom_dashboard_color
        }


        bottomNav.itemIconTintList =
            activity.getColorStateList(color)

        bottomNav.itemTextColor =
            activity.getColorStateList(color)
    }
}