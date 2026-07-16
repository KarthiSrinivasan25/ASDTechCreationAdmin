package com.ecommerce.asdtechcreationadmin.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ecommerce.asdtechcreationadmin.data.model.RecentInvoice
import com.ecommerce.asdtechcreationadmin.databinding.ItemRecentInvoiceBinding

class RecentInvoiceAdapter(
    private var invoiceList: List<RecentInvoice>
) : RecyclerView.Adapter<RecentInvoiceAdapter.InvoiceViewHolder>() {

    inner class InvoiceViewHolder(val binding: ItemRecentInvoiceBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {

        val binding = ItemRecentInvoiceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return InvoiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {

        val invoice = invoiceList[position]

        holder.binding.txtInvoiceNo.text = invoice.invoice_number
        holder.binding.txtClientName.text = invoice.client_name
        holder.binding.txtInvoiceDate.text = invoice.invoice_date
        holder.binding.txtInvoiceAmount.text = "₹${invoice.total_amount}"
        holder.binding.txtStatus.text = invoice.status
    }

    override fun getItemCount(): Int {
        return invoiceList.size
    }

    fun updateData(list: List<RecentInvoice>) {
        invoiceList = list
        notifyDataSetChanged()
    }
}