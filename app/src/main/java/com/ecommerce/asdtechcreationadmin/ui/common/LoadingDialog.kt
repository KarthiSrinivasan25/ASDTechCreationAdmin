package com.ecommerce.asdtechcreationadmin.ui.common

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import com.ecommerce.asdtechcreationadmin.R

/**
 * Small reusable "in progress" popup — a dimmed overlay with a centered
 * card, spinner, and message. Used for anything that takes a moment and
 * blocks the screen: signing in, generating/downloading a PDF, sending an
 * email, etc. Non-cancelable so the person can't dismiss it mid-operation.
 */
class LoadingDialog(activity: Activity) {

    private val dialog: Dialog = Dialog(activity)
    private lateinit var txtMessage: TextView
    private lateinit var txtSubtext: TextView

    init {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_loading, null)
        txtMessage = view.findViewById(R.id.txtLoadingMessage)
        txtSubtext = view.findViewById(R.id.txtLoadingSubtext)

        dialog.setContentView(view)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.55f)
            setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    fun show(message: String, subtext: String = "Please wait a moment") {
        txtMessage.text = message
        txtSubtext.text = subtext
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    fun dismiss() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
}
