package com.ecommerce.asdtechcreationadmin.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ecommerce.asdtechcreationadmin.R
import com.ecommerce.asdtechcreationadmin.data.model.Payment
import com.ecommerce.asdtechcreationadmin.databinding.ItemPaymentBinding
import java.text.NumberFormat
import java.util.Locale

class PaymentAdapter(
    private var payments: List<Payment>,
    private val onClick: (Payment) -> Unit
) : RecyclerView.Adapter<PaymentAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPaymentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPaymentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = payments.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val payment = payments[position]
        val context = holder.binding.root.context

        holder.binding.txtReceiptNumber.text = payment.receipt_number

        val clientLabel = listOfNotNull(
            payment.client_name?.ifEmpty { null },
            payment.company_name?.ifEmpty { null }
        ).joinToString(" • ")

        holder.binding.txtClientName.text = clientLabel.ifEmpty { "—" }
        holder.binding.txtInvoiceNumber.text =
            "Invoice: ${payment.invoice_number?.ifEmpty { "—" } ?: "—"}"

        holder.binding.txtPaymentDate.text = payment.payment_date
        holder.binding.txtPaymentMethod.text =
            payment.payment_method?.ifEmpty { "—" } ?: "—"

        val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
        holder.binding.txtAmountPaid.text = "₹${formatter.format(payment.amount_paid)}"

        val status = payment.status?.trim()?.ifEmpty { "Pending" } ?: "Pending"
        holder.binding.txtPaymentStatus.text = status

        when (status.lowercase()) {

            "paid" -> {
                holder.binding.txtPaymentStatus.setBackgroundResource(R.drawable.bg_chip_green)
                holder.binding.txtPaymentStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.accent_green)
                )
            }

            "partial" -> {
                holder.binding.txtPaymentStatus.setBackgroundResource(R.drawable.bg_chip_orange)
                holder.binding.txtPaymentStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.accent_orange)
                )
            }

            else -> {
                holder.binding.txtPaymentStatus.setBackgroundResource(R.drawable.bg_chip_red)
                holder.binding.txtPaymentStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.accent_red)
                )
            }
        }

        holder.binding.root.setOnClickListener { onClick(payment) }
    }

    fun submitList(newList: List<Payment>) {
        payments = newList
        notifyDataSetChanged()
    }
}
