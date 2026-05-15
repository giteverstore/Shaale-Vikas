package com.shaalevikas.app.ui.halloffame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.shaalevikas.app.R
import com.shaalevikas.app.data.model.User
import com.shaalevikas.app.databinding.ItemHallOfFameBinding

class HallOfFameAdapter(
    private val onShare: (User) -> Unit
) : ListAdapter<User, HallOfFameAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(a: User, b: User) = a.uid == b.uid
        override fun areContentsTheSame(a: User, b: User) = a == b
    }

    inner class ViewHolder(
        private val binding: ItemHallOfFameBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User, position: Int) {
            val rank = user.rank
            val pp   = user.pledgePoints

            // Rank number
            binding.tvRank.text = "${position + 1}"

            // Avatar - photo or letter
            if (user.profileImageUrl.isNotEmpty()) {
                binding.ivAvatarPhoto.visibility  = View.VISIBLE
                binding.tvAvatarLetter.visibility = View.GONE
                Glide.with(binding.root.context)
                    .load(user.profileImageUrl)
                    .transform(CircleCrop())
                    .placeholder(R.drawable.circle_avatar_green)
                    .into(binding.ivAvatarPhoto)
            } else {
                binding.ivAvatarPhoto.visibility  = View.GONE
                binding.tvAvatarLetter.visibility = View.VISIBLE
                binding.tvAvatarLetter.text = user.displayName
                    .firstOrNull()
                    ?.uppercaseChar()
                    ?.toString() ?: "?"
            }

            // Name
            binding.tvName.text = user.displayName

            // Rank emoji
            binding.tvRankEmoji.text = rank.emoji

            // PP value
            binding.tvPp.text = "$pp PP"

            // Share
            binding.btnShare.setOnClickListener { onShare(user) }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemHallOfFameBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position), position)
    }
}