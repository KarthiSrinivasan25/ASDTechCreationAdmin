package com.ecommerce.asdtechcreationadmin.ui.login

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
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



        // Floating glow animation
        startGlowAnimation()



        // Password eye toggle

        binding.imgEye.setOnClickListener {


            if(isVisible){


                binding.etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD


                binding.imgEye.setImageResource(
                    R.drawable.ic_eye
                )


            }else{


                binding.etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD


                binding.imgEye.setImageResource(
                    R.drawable.ic_eye_off
                )


            }



            binding.etPassword.setSelection(
                binding.etPassword.length()
            )


            isVisible = !isVisible

        }




        binding.btnLogin.setOnClickListener {

            login()

        }


    }




    private fun startGlowAnimation(){


        val animator1 =
            ValueAnimator.ofFloat(
                0.15f,
                0.7f,
                0.15f
            )


        animator1.duration = 3500

        animator1.repeatCount =
            ValueAnimator.INFINITE



        animator1.addUpdateListener {


            binding.glowOrb1.alpha =
                it.animatedValue as Float


        }



        animator1.start()




        val animator2 =
            ValueAnimator.ofFloat(
                0.2f,
                0.6f,
                0.2f
            )


        animator2.duration = 4500

        animator2.repeatCount =
            ValueAnimator.INFINITE



        animator2.startDelay = 1000



        animator2.addUpdateListener {


            binding.glowOrb2.alpha =
                it.animatedValue as Float


        }



        animator2.start()

    }






    private fun login(){


        val email =
            binding.etEmail.text.toString().trim()



        val password =
            binding.etPassword.text.toString().trim()




        if(email.isEmpty()){

            binding.etEmail.error =
                "Enter Email"

            return

        }



        if(password.isEmpty()){

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


        ).enqueue(object : Callback<LoginResponse>{



            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ){


                setLoading(false)



                val body =
                    response.body()



                if(response.isSuccessful &&
                    body != null &&
                    body.status){



                    SessionManager(
                        this@LoginActivity
                    ).saveSession(


                        body.token ?: "",


                        body.admin?.name ?: "",


                        body.admin?.email ?: ""

                    )



                    showNotification(

                        body.message
                            ?: "Login Successful",

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




                }else{


                    showNotification(

                        body?.message
                            ?: "Invalid Login",

                        false

                    )


                }



            }




            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ){


                setLoading(false)



                showNotification(

                    t.message
                        ?: "Network Error",

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



        snackbar.view.setBackgroundColor(


            ContextCompat.getColor(

                this,

                if(isSuccess)

                    R.color.accent_green

                else

                    R.color.accent_red

            )

        )




        val textView =

            snackbar.view.findViewById<TextView>(

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