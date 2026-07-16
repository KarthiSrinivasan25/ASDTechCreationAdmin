package com.ecommerce.asdtechcreationadmin.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ecommerce.asdtechcreationadmin.R
import com.ecommerce.asdtechcreationadmin.api.ApiClient
import com.ecommerce.asdtechcreationadmin.data.model.LoginRequest
import com.ecommerce.asdtechcreationadmin.data.model.LoginResponse
import com.ecommerce.asdtechcreationadmin.databinding.ActivityLoginBinding
import com.ecommerce.asdtechcreationadmin.session.SessionManager
import com.ecommerce.asdtechcreationadmin.ui.dashboard.DashboardActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var isVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide ActionBar
        supportActionBar?.hide()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgEye.setOnClickListener {

            if (isVisible) {

                binding.etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD

                binding.imgEye.setImageResource(R.drawable.ic_eye)

            } else {

                binding.etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                binding.imgEye.setImageResource(R.drawable.ic_eye_off)

            }

            binding.etPassword.setSelection(binding.etPassword.text.length)
            isVisible = !isVisible
        }

        binding.btnLogin.setOnClickListener {
            login()
        }
    }

    private fun login() {

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.etEmail.error = "Enter Email"
            return
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Enter Password"
            return
        }

        binding.btnLogin.isEnabled = false

        ApiClient.apiService.login(
            LoginRequest(email, password)
        ).enqueue(object : Callback<LoginResponse> {

            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {

                binding.btnLogin.isEnabled = true

                if (response.isSuccessful) {

                    val body = response.body()

                    if (body != null && body.status) {

                        SessionManager(this@LoginActivity).saveSession(
                            body.token!!,
                            body.admin!!.name,
                            body.admin.email
                        )

                        Toast.makeText(
                            this@LoginActivity,
                            body.message,
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(
                            Intent(
                                this@LoginActivity,
                                DashboardActivity::class.java
                            )
                        )

                        finish()

                    } else {

                        Toast.makeText(
                            this@LoginActivity,
                            body?.message ?: "Login Failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {

                    Toast.makeText(
                        this@LoginActivity,
                        "Invalid Email or Password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {

                binding.btnLogin.isEnabled = true

                Toast.makeText(
                    this@LoginActivity,
                    t.message ?: "Something went wrong",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}