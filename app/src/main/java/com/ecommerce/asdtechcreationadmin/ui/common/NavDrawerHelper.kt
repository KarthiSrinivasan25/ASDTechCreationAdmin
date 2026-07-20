package com.ecommerce.asdtechcreationadmin.ui.common

import android.content.Intent
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.ecommerce.asdtechcreationadmin.databinding.LayoutNavDrawerContentBinding
import com.ecommerce.asdtechcreationadmin.session.SessionManager
import com.ecommerce.asdtechcreationadmin.ui.client.ClientActivity
import com.ecommerce.asdtechcreationadmin.ui.dashboard.DashboardActivity
import com.ecommerce.asdtechcreationadmin.ui.invoice.InvoiceActivity
import com.ecommerce.asdtechcreationadmin.ui.login.LoginActivity
import com.ecommerce.asdtechcreationadmin.ui.payment.PaymentActivity
import com.ecommerce.asdtechcreationadmin.ui.profile.ProfileActivity

/**
 * Wires up the shared navigation-drawer content (company header + nav rows +
 * logout) that every top-level screen includes via layout_nav_drawer_content.xml.
 */
object NavDrawerHelper {

    fun setup(
        activity: AppCompatActivity,
        drawerLayout: DrawerLayout,
        drawer: LayoutNavDrawerContentBinding,
        currentTabId: Int
    ) {

        highlightCurrentItem(drawer, currentTabId)

        drawer.drawerDashboard.setOnClickListener {
            navigateTo(activity, drawerLayout, DashboardActivity::class.java)
        }

        drawer.drawerClients.setOnClickListener {
            navigateTo(activity, drawerLayout, ClientActivity::class.java)
        }

        drawer.drawerInvoice.setOnClickListener {
            navigateTo(activity, drawerLayout, InvoiceActivity::class.java)
        }

        drawer.drawerPayment.setOnClickListener {
            navigateTo(activity, drawerLayout, PaymentActivity::class.java)
        }

        drawer.drawerProfile.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            activity.startActivity(Intent(activity, ProfileActivity::class.java))
        }

        drawer.drawerSignatures.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            activity.startActivity(
                Intent(
                    activity,
                    com.ecommerce.asdtechcreationadmin.ui.signature.SignatureActivity::class.java
                )
            )
        }

        drawer.drawerLogout.setOnClickListener {
            confirmLogout(activity, drawerLayout)
        }

        activity.onBackPressedDispatcher.addCallback(activity) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                activity.finish()
            }
        }
    }

    private fun highlightCurrentItem(
        drawer: LayoutNavDrawerContentBinding,
        currentTabId: Int
    ) {

        val activeBg = com.ecommerce.asdtechcreationadmin.R.drawable.bg_drawer_item_active

        when (currentTabId) {
            com.ecommerce.asdtechcreationadmin.R.id.nav_dashboard ->
                drawer.drawerDashboard.setBackgroundResource(activeBg)

            com.ecommerce.asdtechcreationadmin.R.id.nav_clients ->
                drawer.drawerClients.setBackgroundResource(activeBg)

            com.ecommerce.asdtechcreationadmin.R.id.nav_invoice ->
                drawer.drawerInvoice.setBackgroundResource(activeBg)

            com.ecommerce.asdtechcreationadmin.R.id.nav_payment ->
                drawer.drawerPayment.setBackgroundResource(activeBg)
        }
    }

    private fun navigateTo(
        activity: AppCompatActivity,
        drawerLayout: DrawerLayout,
        target: Class<*>
    ) {

        drawerLayout.closeDrawer(GravityCompat.START)

        if (activity::class.java == target) {
            return
        }

        activity.startActivity(Intent(activity, target))
        activity.overridePendingTransition(0, 0)

        if (activity !is DashboardActivity) {
            activity.finish()
        }
    }

    private fun confirmLogout(activity: AppCompatActivity, drawerLayout: DrawerLayout) {

        drawerLayout.closeDrawer(GravityCompat.START)

        AlertDialog.Builder(activity)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->

                SessionManager(activity).logout()

                val intent = Intent(activity, LoginActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                activity.startActivity(intent)
                activity.finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
