package com.shaalevikas.app.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shaalevikas.app.R
import com.shaalevikas.app.data.model.Need
import com.shaalevikas.app.databinding.ItemNeedCardBinding

class NeedCardAdapter(
    private var isAdmin: Boolean,
    private val onCardClick: (Need) -> Unit,
    private val onDeleteClick: (Need) -> Unit
) : ListAdapter<Need, NeedCardAdapter.NeedViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Need>() {
        override fun areItemsTheSame(a: Need, b: Need) = a.id == b.id
        override fun areContentsTheSame(a: Need, b: Need) = a == b
    }

    // Update admin status and refresh list
    fun updateAdminStatus(admin: Boolean) {
        isAdmin = admin
        notifyDataSetChanged()
    }

    inner class NeedViewHolder(
        private val binding: ItemNeedCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(need: Need) {
            binding.tvTitle.text = need.title
            binding.tvCategory.text = need.category
            binding.tvCostEstimate.text =
                "💰 ₹${String.format("%,.0f", need.costEstimate)}"
            binding.tvPledgeCount.text =
                "👥 ${need.pledgeCount} alumni pledged"
            binding.progressBar.progress = need.progressPercent
            binding.tvProgress.text =
                "${need.progressPercent}% of funds collected for ${need.title}"

            // Urgency badge
            binding.tvUrgency.text = need.urgency
            binding.tvUrgency.setBackgroundResource(
                when (need.urgency) {
                    "Critical" -> R.drawable.badge_critical
                    "High" -> R.drawable.badge_high
                    else -> R.drawable.badge_medium
                }
            )

            // Category icon
            binding.ivCategoryIcon.setImageResource(
                when (need.category) {
                    "Furniture" -> R.drawable.ic_chair
                    "Sanitation" -> R.drawable.ic_water
                    "Classroom" -> R.drawable.ic_school
                    "Sports" -> R.drawable.ic_sports
                    else -> R.drawable.ic_misc
                }
            )

            // Card click
            binding.root.setOnClickListener { onCardClick(need) }

            // Admin delete button
            binding.btnDelete.visibility =
                if (isAdmin) View.VISIBLE else View.GONE
            binding.btnDelete.setOnClickListener { onDeleteClick(need) }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NeedViewHolder {
        val binding = ItemNeedCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NeedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NeedViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}