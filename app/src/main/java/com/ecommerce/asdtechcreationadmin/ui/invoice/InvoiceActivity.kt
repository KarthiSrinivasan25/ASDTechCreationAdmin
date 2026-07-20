package com.ecommerce.asdtechcreationadmin.ui.invoice

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.ecommerce.asdtechcreationadmin.R
import com.ecommerce.asdtechcreationadmin.databinding.ActivityInvoiceBinding
import com.ecommerce.asdtechcreationadmin.ui.common.BottomNavHelper
import com.ecommerce.asdtechcreationadmin.ui.common.NavDrawerHelper

class InvoiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInvoiceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInvoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        BottomNavHelper.setup(this, binding.bottomNavigation, R.id.nav_invoice) {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        NavDrawerHelper.setup(this, binding.drawerLayout, binding.navDrawer, R.id.nav_invoice)
    }
}
