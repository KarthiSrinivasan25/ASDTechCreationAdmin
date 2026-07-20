package com.ecommerce.asdtechcreationadmin.ui.login

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.animation.LinearInterpolator
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
import android.view.animation.AnimationUtils


class LoginActivity : AppCompatActivity() {


    private lateinit var binding: ActivityLoginBinding

    private var isVisible = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        supportActionBar?.hide()


        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val icons = arrayOf(
    binding.iconWebsite,
    binding.iconWhatsapp,
    binding.iconEmail,
    binding.iconCall
)


icons.forEachIndexed { index, imageView ->

    imageView.alpha = 0f

    imageView.translationY = 40f


    imageView.animate()

        .alpha(1f)

        .translationY(0f)

        .setDuration(400)

        .setStartDelay(index * 150L)

        .start()
}



        // Company name shimmer animation
        startCompanyNameAnimation()



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




        // Login button

        binding.btnLogin.setOnClickListener {

            login()

        }


    }



    // ASD TechCreation Gradient Animation

    private fun startCompanyNameAnimation() {


        val textView = binding.txtCompanyName


        textView.post {


            val paint = textView.paint


            val textWidth = paint.measureText(
                textView.text.toString()
            )



            val animator = ValueAnimator.ofFloat(
                -textWidth,
                textWidth
            )



            animator.duration = 2500

            animator.repeatCount =
                ValueAnimator.INFINITE

            animator.interpolator =
                LinearInterpolator()



            animator.addUpdateListener {


                val x =
                    it.animatedValue as Float



                val shader = LinearGradient(

                    x,
                    0f,

                    x + textWidth,
                    0f,


                    intArrayOf(

                        Color.WHITE,

                        Color.parseColor("#80FFD8"),

                        Color.parseColor("#00A86B"),

                        Color.WHITE

                    ),


                    null,


                    Shader.TileMode.CLAMP

                )



                paint.shader = shader


                textView.invalidate()


            }



            animator.start()


        }


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



                    val body =
                        response.body()



                    if (body != null && body.status) {



                        SessionManager(
                            this@LoginActivity
                        ).saveSession(

                            body.token!!,

                            body.admin!!.name,

                            body.admin.email

                        )



                        showNotification(

                            body.message
                                ?: "Login successful",

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

                            body?.message
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
                        ?: "Something went wrong. Please try again",

                    false

                )

            }



        })



    }





    private fun setLoading(
        loading: Boolean
    ) {


        binding.btnLogin.isEnabled =
            !loading


        binding.etEmail.isEnabled =
            !loading


        binding.etPassword.isEnabled =
            !loading


        binding.imgEye.isEnabled =
            !loading



        binding.btnLogin.text =
            if (loading) "" else "Login"



        binding.progressLogin.visibility =
            if (loading)
                View.VISIBLE
            else
                View.GONE


    }





    private fun showNotification(

        message: String,

        isSuccess: Boolean

    ) {



        val snackbar = Snackbar.make(

            binding.root,

            message,

            Snackbar.LENGTH_LONG

        )



        val snackbarView =
            snackbar.view



        val colorRes =

            if (isSuccess)

                R.color.accent_green

            else

                R.color.accent_red





        snackbarView.setBackgroundColor(

            ContextCompat.getColor(
                this,
                colorRes
            )

        )




        val textView =
            snackbarView.findViewById<TextView>(

                com.google.android.material.R.id.snackbar_text

            )



        textView.setTextColor(

            ContextCompat.getColor(
                this,
                R.color.white
            )

        )



        textView.gravity =
            Gravity.CENTER



        snackbar.show()


    }


}