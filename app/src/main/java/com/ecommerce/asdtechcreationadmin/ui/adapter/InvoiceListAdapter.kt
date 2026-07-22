package com.ecommerce.asdtechcreationadmin.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ecommerce.asdtechcreationadmin.R
import com.ecommerce.asdtechcreationadmin.data.model.InvoiceListItem
import com.ecommerce.asdtechcreationadmin.databinding.ItemInvoiceBinding
import java.text.NumberFormat
import java.util.Locale

class InvoiceListAdapter(
    private var invoices: List<InvoiceListItem>,
    private val onClick: (InvoiceListItem) -> Unit
) : RecyclerView.Adapter<InvoiceListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemInvoiceBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInvoiceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = invoices.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val invoice = invoices[position]
        val context = holder.binding.root.context
        val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))

        holder.binding.txtInvoiceNumber.text = invoice.invoice_number

        val clientLabel = listOfNotNull(
            invoice.client_name?.ifEmpty { null },
            invoice.company_name?.ifEmpty { null }
        ).joinToString(" • ")

        holder.binding.txtClientName.text = clientLabel.ifEmpty { "—" }

        holder.binding.txtInvoiceDates.text =
            "${invoice.invoice_date} → Due ${invoice.due_date}"

        holder.binding.txtTotalAmount.text = "₹${formatter.format(invoice.total_amount)}"
        holder.binding.txtBalanceAmount.text =
            "Balance: ₹${formatter.format(invoice.balance_amount)}"

        val status = invoice.status?.trim()?.ifEmpty { "Pending" } ?: "Pending"
        holder.binding.txtInvoiceStatus.text = status

        when (status.lowercase()) {

            "paid" -> {
                holder.binding.txtInvoiceStatus.setBackgroundResource(R.drawable.bg_chip_green)
                holder.binding.txtInvoiceStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.accent_green)
                )
            }

            "partial" -> {
                holder.binding.txtInvoiceStatus.setBackgroundResource(R.drawable.bg_chip_blue)
                holder.binding.txtInvoiceStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.accent_blue)
                )
            }

            else -> {
                holder.binding.txtInvoiceStatus.setBackgroundResource(R.drawable.bg_chip_orange)
                holder.binding.txtInvoiceStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.accent_orange)
                )
            }
        }

        holder.binding.txtBalanceAmount.visibility =
            if (invoice.balance_amount > 0) android.view.View.VISIBLE else android.view.View.GONE

        holder.binding.root.setOnClickListener { onClick(invoice) }
    }

    fun submitList(newList: List<InvoiceListItem>) {
        invoices = newList
        notifyDataSetChanged()
    }
}
