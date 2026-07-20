package com.ecommerce.asdtechcreationadmin.ui.signature

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecommerce.asdtechcreationadmin.api.ApiClient
import com.ecommerce.asdtechcreationadmin.data.model.SignaturesResponse
import com.ecommerce.asdtechcreationadmin.databinding.ActivitySignatureBinding
import com.ecommerce.asdtechcreationadmin.ui.adapter.SignatureAdapter
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignatureActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignatureBinding
    private lateinit var adapter: SignatureAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignatureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvSignatures.layoutManager = LinearLayoutManager(this)
        adapter = SignatureAdapter(emptyList())
        binding.rvSignatures.adapter = adapter

        binding.btnBack.setOnClickListener { finish() }

        binding.fabAddSignature.setOnClickListener {
            AddSignatureBottomSheet { loadSignatures() }
                .show(supportFragmentManager, "add_signature")
        }

        loadSignatures()
    }

    private fun loadSignatures() {

        binding.progressSignatures.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE

        ApiClient.apiService.getSignatures().enqueue(object : Callback<SignaturesResponse> {

            override fun onResponse(
                call: Call<SignaturesResponse>,
                response: Response<SignaturesResponse>
            ) {

                binding.progressSignatures.visibility = View.GONE

                val list = response.body()?.data ?: emptyList()

                if (response.isSuccessful && response.body()?.status == true) {
                    adapter.submitList(list)
                    binding.emptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    adapter.submitList(emptyList())
                    binding.emptyState.visibility = View.VISIBLE
                    showError(response.body()?.message ?: "Unable to load signatures")
                }
            }

            override fun onFailure(call: Call<SignaturesResponse>, t: Throwable) {
                binding.progressSignatures.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE
                showError(t.message ?: "Something went wrong")
            }
        })
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
