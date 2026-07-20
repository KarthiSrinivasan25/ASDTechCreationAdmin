package com.ecommerce.asdtechcreationadmin.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ecommerce.asdtechcreationadmin.R
import com.ecommerce.asdtechcreationadmin.api.ApiClient
import com.ecommerce.asdtechcreationadmin.data.model.LoginRequest
import com.ecommerce.asdtechcreationadmin.data.model.LoginResponse
import com.ecommerce.asdtechcreationadmin.databinding.ActivityLoginBinding
import com.ecommerce.asdtechcreationadmin.session.SessionManager
import com.ecommerce.asdtechcreationadmin.ui.dashboard.DashboardActivity
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity() {


    private lateinit var binding: ActivityLoginBinding

    private var isVisible = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Ring blinking animation
        startRingAnimation()


        // Password show/hide
        binding.imgEye.setOnClickListener {

            if (isVisible) {

                binding.etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD

                binding.imgEye.setImageResource(
                    R.drawable.ic_eye
                )

            } else {

                binding.etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                binding.imgEye.setImageResource(
                    R.drawable.ic_eye_off
                )
            }


            binding.etPassword.setSelection(
                binding.etPassword.text.length
            )

            isVisible = !isVisible
        }



        binding.btnLogin.setOnClickListener {
            login()
        }

    }



    private fun startRingAnimation() {

    val blinkAnimation = android.animation.ValueAnimator.ofFloat(
        0.05f,
        0.8f,
        0.05f
    )

    blinkAnimation.duration = 2500
    blinkAnimation.repeatCount = android.animation.ValueAnimator.INFINITE

    blinkAnimation.addUpdateListener { animator ->

        val alpha =
            animator.animatedValue as Float

        binding.imgRingGlow.alpha = alpha

    }

    blinkAnimation.start()
}




    private fun login() {


        val email =
            binding.etEmail.text.toString().trim()


        val password =
            binding.etPassword.text.toString().trim()



        if (email.isEmpty()) {

            binding.etEmail.error =
                "Enter Email"

            return
        }



        if (password.isEmpty()) {

            binding.etPassword.error =
                "Enter Password"

            return
        }



        setLoading(true)



        ApiClient.apiService.login(
            LoginRequest(
                email,
                password
            )

        ).enqueue(object : Callback<LoginResponse> {



            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {


                setLoading(false)



                if (response.isSuccessful) {


                    val data =
                        response.body()



                    if (data != null && data.status) {



                        SessionManager(
                            this@LoginActivity
                        ).saveSession(

                            data.token ?: "",

                            data.admin?.name ?: "",

                            data.admin?.email ?: ""

                        )



                        showNotification(
                            data.message ?: "Login Successful",
                            true
                        )



                        binding.root.postDelayed({


                            startActivity(
                                Intent(
                                    this@LoginActivity,
                                    DashboardActivity::class.java
                                )
                            )


                            finish()


                        },600)



                    } else {


                        showNotification(
                            data?.message
                                ?: "Login Failed",
                            false
                        )

                    }



                } else {


                    showNotification(
                        "Invalid Email or Password",
                        false
                    )

                }

            }




            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {


                setLoading(false)


                showNotification(
                    t.message
                        ?: "Something went wrong",
                    false
                )

            }


        })

    }




    private fun setLoading(
        loading:Boolean
    ){


        binding.btnLogin.isEnabled =
            !loading


        binding.etEmail.isEnabled =
            !loading


        binding.etPassword.isEnabled =
            !loading


        binding.imgEye.isEnabled =
            !loading



        binding.btnLogin.text =
            if(loading)
                ""
            else
                "Login"



        binding.progressLogin.visibility =
            if(loading)
                View.VISIBLE
            else
                View.GONE

    }




    private fun showNotification(
        message:String,
        isSuccess:Boolean
    ){


        val snackbar =
            Snackbar.make(
                binding.root,
                message,
                Snackbar.LENGTH_LONG
            )



        val view =
            snackbar.view



        val color =
            if(isSuccess)
                R.color.accent_green
            else
                R.color.accent_red



        view.setBackgroundColor(
            ContextCompat.getColor(
                this,
                color
            )
        )



        val text =
            view.findViewById<TextView>(
                com.google.android.material.R.id.snackbar_text
            )



        text.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.white
            )
        )



        text.gravity =
            Gravity.CENTER



        snackbar.show()

    }


}