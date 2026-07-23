package com.ecommerce.asdtechcreationadmin.ui.common

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.core.content.FileProvider
import com.ecommerce.asdtechcreationadmin.api.ApiClient
import com.google.android.material.snackbar.Snackbar
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

/**
 * Downloads a PDF (invoice or payment receipt) and either:
 *  - opens it in an external PDF viewer,
 *  - saves it to the public Downloads folder, or
 *  - shares it (e.g. via an email app) with the client's email prefilled.
 *
 * Generic over the source Call so it works for both
 * generate_invoice_pdf.php and generate_receipt_pdf.php.
 */
object PdfHelper {

    private fun cacheFile(activity: Activity, fileNameStem: String): File {
        val dir = File(activity.cacheDir, "pdfs")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "$fileNameStem.pdf")
    }

    private fun download(
        call: Call<ResponseBody>,
        activity: Activity,
        fileNameStem: String,
        rootView: View,
        onLoading: (Boolean) -> Unit,
        onReady: (File) -> Unit
    ) {

        onLoading(true)

        call.enqueue(object : Callback<ResponseBody> {

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                onLoading(false)

                val body = response.body()

                if (!response.isSuccessful || body == null) {
                    Snackbar.make(rootView, "Unable to generate PDF", Snackbar.LENGTH_LONG).show()
                    return
                }

                try {
                    val file = cacheFile(activity, fileNameStem)
                    FileOutputStream(file).use { out ->
                        body.byteStream().use { input ->
                            input.copyTo(out)
                        }
                    }
                    onReady(file)
                } catch (e: Exception) {
                    Snackbar.make(
                        rootView,
                        e.message ?: "Unable to save PDF",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onLoading(false)
                Snackbar.make(
                    rootView,
                    t.message ?: "Unable to generate PDF",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun openFile(activity: Activity, file: File, rootView: View) {

        val uri = FileProvider.getUriForFile(
            activity, "${activity.packageName}.fileprovider", file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            activity.startActivity(intent)
        } catch (e: Exception) {
            Snackbar.make(
                rootView,
                "No PDF viewer app found on this device",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun shareFile(
        activity: Activity,
        file: File,
        subject: String,
        body: String,
        clientEmail: String?
    ) {

        val uri = FileProvider.getUriForFile(
            activity, "${activity.packageName}.fileprovider", file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            if (!clientEmail.isNullOrBlank()) {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(clientEmail))
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        activity.startActivity(Intent.createChooser(intent, "Send via"))
    }

    private fun downloadFileToDevice(
        activity: Activity,
        file: File,
        displayName: String,
        rootView: View
    ) {

        try {
            saveToDownloads(activity, file, displayName)
            Snackbar.make(
                rootView,
                "Saved to Downloads as $displayName",
                Snackbar.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Snackbar.make(
                rootView,
                e.message ?: "Unable to download PDF",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun saveToDownloads(activity: Activity, file: File, displayName: String) {

        val resolver = activity.contentResolver

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Files.getContentUri("external")
        }

        val uri: Uri = resolver.insert(collection, values)
            ?: throw Exception("Unable to create download entry")

        resolver.openOutputStream(uri)?.use { out ->
            file.inputStream().use { input ->
                input.copyTo(out)
            }
        } ?: throw Exception("Unable to write PDF")
    }

    // ==========================================
    // INVOICES
    // ==========================================

    fun viewInvoice(
        activity: Activity, invoiceId: Int, invoiceNumber: String,
        rootView: View, onLoading: (Boolean) -> Unit
    ) {
        download(
            ApiClient.apiService.downloadInvoicePdf(invoiceId),
            activity, invoiceNumber, rootView, onLoading
        ) { file -> openFile(activity, file, rootView) }
    }

    fun downloadInvoiceToDevice(
        activity: Activity, invoiceId: Int, invoiceNumber: String,
        rootView: View, onLoading: (Boolean) -> Unit
    ) {
        download(
            ApiClient.apiService.downloadInvoicePdf(invoiceId),
            activity, invoiceNumber, rootView, onLoading
        ) { file -> downloadFileToDevice(activity, file, "$invoiceNumber.pdf", rootView) }
    }

    // ==========================================
    // RECEIPTS
    // ==========================================

    fun viewReceipt(
        activity: Activity, paymentId: Int, receiptNumber: String,
        rootView: View, onLoading: (Boolean) -> Unit
    ) {
        download(
            ApiClient.apiService.downloadReceiptPdf(paymentId),
            activity, receiptNumber, rootView, onLoading
        ) { file -> openFile(activity, file, rootView) }
    }

    fun downloadReceiptToDevice(
        activity: Activity, paymentId: Int, receiptNumber: String,
        rootView: View, onLoading: (Boolean) -> Unit
    ) {
        download(
            ApiClient.apiService.downloadReceiptPdf(paymentId),
            activity, receiptNumber, rootView, onLoading
        ) { file -> downloadFileToDevice(activity, file, "$receiptNumber.pdf", rootView) }
    }
}
