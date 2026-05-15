package com.shaalevikas.app.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shaalevikas.app.data.model.Pledge
import com.shaalevikas.app.databinding.ItemMyPledgeBinding
import java.text.SimpleDateFormat
import java.util.Locale

class MyPledgesAdapter : ListAdapter<Pledge, MyPledgesAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Pledge>() {
        override fun areItemsTheSame(a: Pledge, b: Pledge) = a.id == b.id
        override fun areContentsTheSame(a: Pledge, b: Pledge) = a == b
    }

    inner class ViewHolder(private val binding: ItemMyPledgeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pledge: Pledge) {
            binding.tvPledgeType.text = pledge.pledgeType
            binding.tvAmount.text = if (pledge.pledgeType == "Funds")
                "₹${String.format("%,.0f", pledge.amount)}"
            else pledge.itemDescription

            binding.tvMessage.text = if (pledge.message.isNotEmpty())
                "\"${pledge.message}\"" else ""

            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.tvDate.text = sdf.format(pledge.timestamp.toDate())

            binding.tvPledgeType.setBackgroundResource(
                if (pledge.pledgeType == "Funds")
                    com.shaalevikas.app.R.drawable.badge_gold
                else
                    com.shaalevikas.app.R.drawable.badge_medium
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMyPledgeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}