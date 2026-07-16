package com.ecommerce.asdtechcreationadmin.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ecommerce.asdtechcreationadmin.data.model.PendingClient
import com.ecommerce.asdtechcreationadmin.databinding.ItemPendingClientBinding

class PendingClientAdapter(
    private var pendingList: List<PendingClient>
) : RecyclerView.Adapter<PendingClientAdapter.PendingViewHolder>() {

    inner class PendingViewHolder(
        val binding: ItemPendingClientBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PendingViewHolder {

        val binding = ItemPendingClientBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return PendingViewHolder(binding)

    }

    override fun onBindViewHolder(
        holder: PendingViewHolder,
        position: Int
    ) {

        val client = pendingList[position]

        holder.binding.txtPendingClient.text = client.client_name
        holder.binding.txtPendingInvoice.text = client.invoice_number
        holder.binding.txtPendingAmount.text = "₹${client.balance_amount}"
        holder.binding.txtDueDate.text = client.due_date

    }

    override fun getItemCount(): Int {

        return pendingList.size

    }

    fun updateData(list: List<PendingClient>) {

        pendingList = list

        notifyDataSetChanged()

    }

}