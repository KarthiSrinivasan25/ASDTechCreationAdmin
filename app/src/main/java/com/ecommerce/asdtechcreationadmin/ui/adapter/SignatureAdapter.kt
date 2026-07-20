package com.ecommerce.asdtechcreationadmin.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ecommerce.asdtechcreationadmin.R
import com.ecommerce.asdtechcreationadmin.data.model.Signature
import com.ecommerce.asdtechcreationadmin.databinding.ItemSignatureBinding

/**
 * Reused in two places:
 *  - SignatureActivity: plain read-only list (selectable = false)
 *  - SignaturePickerDialog: tap-to-select list (selectable = true), highlights selectedId
 */
class SignatureAdapter(
    private var signatures: List<Signature>,
    private val selectable: Boolean = false,
    private var selectedId: Int? = null,
    private val onClick: ((Signature) -> Unit)? = null
) : RecyclerView.Adapter<SignatureAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSignatureBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSignatureBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = signatures.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val signature = signatures[position]

        holder.binding.txtSignatureName.text = signature.name

        Glide.with(holder.binding.imgSignature)
            .load(signature.image_url)
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_image_placeholder)
            .into(holder.binding.imgSignature)

        holder.binding.imgSelectedCheck.visibility =
            if (selectable && signature.id == selectedId) View.VISIBLE else View.GONE

        if (selectable) {
            holder.binding.root.setOnClickListener {
                val previousSelected = selectedId
                selectedId = signature.id
                onClick?.invoke(signature)
                notifyItemChanged(position)
                if (previousSelected != null) {
                    val previousIndex = signatures.indexOfFirst { it.id == previousSelected }
                    if (previousIndex != -1) notifyItemChanged(previousIndex)
                }
            }
        }
    }

    fun submitList(newList: List<Signature>) {
        signatures = newList
        notifyDataSetChanged()
    }
}
