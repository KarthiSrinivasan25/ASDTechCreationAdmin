package com.ecommerce.asdtechcreationadmin.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ecommerce.asdtechcreationadmin.R
import com.ecommerce.asdtechcreationadmin.data.model.Client
import com.ecommerce.asdtechcreationadmin.databinding.ItemClientBinding

class ClientAdapter(
    private var clients: List<Client>,
    private val onMenuClick: (Client, android.view.View) -> Unit
) : RecyclerView.Adapter<ClientAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemClientBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemClientBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = clients.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val client = clients[position]
        val context = holder.binding.root.context

        holder.binding.txtClientName.text = client.client_name
        holder.binding.txtClientEmail.text = client.email
        holder.binding.txtClientPhone.text = client.phone

        holder.binding.txtAvatarInitial.text =
            client.client_name.trim().take(1).uppercase().ifEmpty { "?" }

        val status = client.status?.trim()?.ifEmpty { "Active" } ?: "Active"
        holder.binding.txtClientStatus.text = status

        when (status.lowercase()) {

            "active" -> {
                holder.binding.txtClientStatus.setBackgroundResource(R.drawable.bg_chip_green)
                holder.binding.txtClientStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.accent_green)
                )
            }

            "completed" -> {
                holder.binding.txtClientStatus.setBackgroundResource(R.drawable.bg_chip_blue)
                holder.binding.txtClientStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.accent_blue)
                )
            }

            "on hold", "onhold" -> {
                holder.binding.txtClientStatus.setBackgroundResource(R.drawable.bg_chip_orange)
                holder.binding.txtClientStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.accent_orange)
                )
            }

            else -> {
                holder.binding.txtClientStatus.setBackgroundResource(R.drawable.bg_chip_green)
                holder.binding.txtClientStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.accent_green)
                )
            }
        }

        holder.binding.btnClientMenu.setOnClickListener {
            onMenuClick(client, it)
        }
    }

    fun submitList(newList: List<Client>) {
        clients = newList
        notifyDataSetChanged()
    }
}
