package com.ecommerce.asdtechcreationadmin.ui.payment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.ecommerce.asdtechcreationadmin.R
import com.ecommerce.asdtechcreationadmin.databinding.ActivityPaymentBinding
import com.ecommerce.asdtechcreationadmin.ui.common.BottomNavHelper
import com.ecommerce.asdtechcreationadmin.ui.common.NavDrawerHelper

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        BottomNavHelper.setup(this, binding.bottomNavigation, R.id.nav_payment) {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        NavDrawerHelper.setup(this, binding.drawerLayout, binding.navDrawer, R.id.nav_payment)
    }
}
