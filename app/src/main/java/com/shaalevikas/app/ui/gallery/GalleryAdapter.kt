package com.shaalevikas.app.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shaalevikas.app.data.model.Need
import com.shaalevikas.app.databinding.ItemGalleryCardBinding

class GalleryAdapter : ListAdapter<Need, GalleryAdapter.GalleryViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Need>() {
        override fun areItemsTheSame(a: Need, b: Need) = a.id == b.id
        override fun areContentsTheSame(a: Need, b: Need) = a == b
    }

    inner class GalleryViewHolder(private val binding: ItemGalleryCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(need: Need) {
            binding.tvTitle.text = need.title
            binding.tvCategory.text = need.category
            binding.tvPledgeCount.text = "${need.pledgeCount} pledgers"
            binding.tvAmount.text = "₹${String.format("%,.0f", need.pledgedAmount)} raised"

            Glide.with(binding.root.context)
                .load(need.beforePhotoUrl)
                .placeholder(com.shaalevikas.app.R.drawable.ic_placeholder)
                .into(binding.ivBeforePhoto)

            Glide.with(binding.root.context)
                .load(need.afterPhotoUrl)
                .placeholder(com.shaalevikas.app.R.drawable.ic_placeholder)
                .into(binding.ivAfterPhoto)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val binding = ItemGalleryCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GalleryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}