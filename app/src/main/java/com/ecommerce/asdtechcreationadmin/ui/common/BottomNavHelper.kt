package com.ecommerce.asdtechcreationadmin.ui.common

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ecommerce.asdtechcreationadmin.R
import com.ecommerce.asdtechcreationadmin.ui.client.ClientActivity
import com.ecommerce.asdtechcreationadmin.ui.dashboard.DashboardActivity
import com.ecommerce.asdtechcreationadmin.ui.invoice.InvoiceActivity
import com.ecommerce.asdtechcreationadmin.ui.payment.PaymentActivity
import com.ecommerce.asdtechcreationadmin.ui.settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarItemView
import com.google.android.material.navigation.NavigationBarMenuView

object BottomNavHelper {

    fun setup(
        activity: AppCompatActivity,
        bottomNav: BottomNavigationView,
        currentTabId: Int
    ) {

        // Preserve icon colors
        bottomNav.itemIconTintList = null

        bottomNav.selectedItemId = currentTabId

        // Apply background for currently selected tab
        applySelectedIconStyle(bottomNav)

        bottomNav.setOnItemSelectedListener { item ->

            if (item.itemId == currentTabId) {
                return@setOnItemSelectedListener true
            }

            // Animate selected icon
            animateSelectedIcon(bottomNav, item.itemId)

            val targetClass: Class<*>? = when (item.itemId) {

                R.id.nav_dashboard -> DashboardActivity::class.java

                R.id.nav_clients -> ClientActivity::class.java

                R.id.nav_invoice -> InvoiceActivity::class.java

                R.id.nav_payment -> PaymentActivity::class.java

                R.id.nav_settings -> SettingsActivity::class.java

                else -> null
            }

            if (targetClass != null) {

                // Small delay so animation is visible
                Handler(Looper.getMainLooper()).postDelayed({

                    activity.startActivity(
                        Intent(activity, targetClass)
                    )

                    activity.overridePendingTransition(0, 0)

                    if (currentTabId != R.id.nav_dashboard) {
                        activity.finish()
                    }

                }, 250)
            }

            true
        }
    }

    private fun applySelectedIconStyle(
        bottomNav: BottomNavigationView
    ) {

        val menuView =
            bottomNav.getChildAt(0) as NavigationBarMenuView

        for (i in 0 until menuView.childCount) {

            val itemView =
                menuView.getChildAt(i) as NavigationBarItemView

            val iconView =
                itemView.findViewById<ImageView>(
                    com.google.android.material.R.id.navigation_bar_item_icon_view
                )

       if (itemView.itemData?.isChecked == true) {

                iconView.background =
                    ContextCompat.getDrawable(
                        bottomNav.context,
                        R.drawable.bg_nav_selected
                    )

                val padding = dpToPx(bottomNav, 10)

                iconView.setPadding(
                    padding,
                    padding,
                    padding,
                    padding
                )

            } else {

                iconView.background = null
                iconView.setPadding(0, 0, 0, 0)
            }
        }
    }

    private fun animateSelectedIcon(
        bottomNav: BottomNavigationView,
        selectedItemId: Int
    ) {

        val menuView =
            bottomNav.getChildAt(0) as NavigationBarMenuView

        for (i in 0 until menuView.childCount) {

            val itemView =
                menuView.getChildAt(i) as NavigationBarItemView

          if (itemView.itemData?.itemId == selectedItemId) {

                val iconView =
                    itemView.findViewById<ImageView>(
                        com.google.android.material.R.id.navigation_bar_item_icon_view
                    )

                // Circular background
                iconView.background =
                    ContextCompat.getDrawable(
                        bottomNav.context,
                        R.drawable.bg_nav_selected
                    )

                val padding = dpToPx(bottomNav, 10)

                iconView.setPadding(
                    padding,
                    padding,
                    padding,
                    padding
                )

                // Right to Left rotation
                iconView.rotation = 360f

                iconView.animate()
                    .rotation(0f)
                    .setDuration(250)
                    .start()
            }
        }
    }

    private fun dpToPx(
        bottomNav: BottomNavigationView,
        dp: Int
    ): Int {

        return (dp *
                bottomNav.resources.displayMetrics.density)
            .toInt()
    }
}