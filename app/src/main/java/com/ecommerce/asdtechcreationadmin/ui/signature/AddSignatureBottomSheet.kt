package com.ecommerce.asdtechcreationadmin.ui.signature

import android.app.Dialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.ecommerce.asdtechcreationadmin.R
import com.ecommerce.asdtechcreationadmin.api.ApiClient
import com.ecommerce.asdtechcreationadmin.data.model.AddSignatureResponse
import com.ecommerce.asdtechcreationadmin.databinding.BottomsheetAddSignatureBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class AddSignatureBottomSheet(
    private val onSaved: () -> Unit
) : DialogFragment() {

    private var _binding: BottomsheetAddSignatureBinding? = null
    private val binding get() = _binding!!

    private var selectedImageFile: File? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handlePickedImage(it) }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        _binding = BottomsheetAddSignatureBinding.inflate(LayoutInflater.from(requireContext()))

        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(binding.root)

        binding.imagePickerContainer.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSaveSignature.setOnClickListener {
            saveSignature()
        }

        return dialog
    }

    private fun handlePickedImage(uri: Uri) {

        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            inputStream?.close()

            val file = File(requireContext().cacheDir, "signature_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            selectedImageFile = file

            binding.imgPreview.setImageBitmap(bitmap)
            binding.imgPreview.visibility = View.VISIBLE
            binding.imagePlaceholder.visibility = View.GONE

        } catch (e: Exception) {
            showNotification(e.message ?: "Unable to load image", isSuccess = false)
        }
    }

    private fun saveSignature() {

        val name = binding.etSignatureName.text.toString().trim()

        if (name.isEmpty()) {
            binding.etSignatureName.error = "Enter a name"
            return
        }

        val imageFile = selectedImageFile
        if (imageFile == null) {
            showNotification("Please select a signature image", isSuccess = false)
            return
        }

        setLoading(true)

        val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val imageRequestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData(
            "image", imageFile.name, imageRequestBody
        )

        ApiClient.apiService.addSignature(namePart, imagePart)
            .enqueue(object : Callback<AddSignatureResponse> {

                override fun onResponse(
                    call: Call<AddSignatureResponse>,
                    response: Response<AddSignatureResponse>
                ) {

                    setLoading(false)

                    val body = response.body()

                    if (response.isSuccessful && body?.status == true) {
                        onSaved()
                        dismiss()
                    } else {
                        showNotification(
                            body?.message ?: "Failed to add signature",
                            isSuccess = false
                        )
                    }
                }

                override fun onFailure(call: Call<AddSignatureResponse>, t: Throwable) {
                    setLoading(false)
                    showNotification(
                        t.message ?: "Something went wrong. Please try again",
                        isSuccess = false
                    )
                }
            })
    }

    private fun setLoading(loading: Boolean) {
        binding.btnSaveSignature.isEnabled = !loading
        binding.btnSaveSignature.text = if (loading) "" else "Save Signature"
        binding.progressSaveSignature.visibility = if (loading) View.VISIBLE else View.GONE
        binding.etSignatureName.isEnabled = !loading
        binding.imagePickerContainer.isEnabled = !loading
        isCancelable = !loading
    }

    private fun showNotification(message: String, isSuccess: Boolean) {

        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view

        val colorRes = if (isSuccess) R.color.accent_green else R.color.accent_red
        snackbarView.setBackgroundColor(ContextCompat.getColor(requireContext(), colorRes))

        val textView = snackbarView.findViewById<TextView>(
            com.google.android.material.R.id.snackbar_text
        )
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        textView.gravity = Gravity.CENTER

        snackbar.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
