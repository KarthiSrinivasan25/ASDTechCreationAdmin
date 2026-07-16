package com.ecommerce.asdtechcreationadmin.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ecommerce.asdtechcreationadmin.data.model.RecentPayment
import com.ecommerce.asdtechcreationadmin.databinding.ItemRecentPaymentBinding

class RecentPaymentAdapter(
    private var paymentList: List<RecentPayment>
) : RecyclerView.Adapter<RecentPaymentAdapter.PaymentViewHolder>() {

    inner class PaymentViewHolder(
        val binding: ItemRecentPaymentBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PaymentViewHolder {

        val binding = ItemRecentPaymentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return PaymentViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: PaymentViewHolder,
        position: Int
    ) {

        val payment = paymentList[position]

        holder.binding.txtReceiptNo.text = payment.receipt_number
        holder.binding.txtPaymentClient.text = payment.client_name
        holder.binding.txtPaymentDate.text = payment.payment_date
        holder.binding.txtPaymentAmount.text = "₹${payment.amount_paid}"
        holder.binding.txtPaymentMethod.text = payment.payment_method

    }

    override fun getItemCount(): Int {

        return paymentList.size

    }

    fun updateData(list: List<RecentPayment>) {

        paymentList = list

        notifyDataSetChanged()

    }

}