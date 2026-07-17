package com.ecommerce.asdtechcreationadmin.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ecommerce.asdtechcreationadmin.R
import com.ecommerce.asdtechcreationadmin.databinding.ActivitySettingsBinding
import com.ecommerce.asdtechcreationadmin.ui.common.BottomNavHelper

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        BottomNavHelper.setup(this, binding.bottomNavigation, R.id.nav_settings)
    }
}
