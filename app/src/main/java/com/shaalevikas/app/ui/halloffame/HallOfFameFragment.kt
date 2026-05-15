package com.shaalevikas.app.ui.halloffame

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.shaalevikas.app.R
import com.shaalevikas.app.data.model.User
import com.shaalevikas.app.databinding.FragmentHallOfFameBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HallOfFameFragment : Fragment() {

    private var _binding: FragmentHallOfFameBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HallOfFameViewModel by viewModels()
    private lateinit var adapter: HallOfFameAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHallOfFameBinding
            .inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        observeData()
    }

    private fun setupAdapter() {
        adapter = HallOfFameAdapter { user ->
            shareUser(user)
        }
        _binding?.recyclerHallOfFame?.layoutManager =
            LinearLayoutManager(requireContext())
        _binding?.recyclerHallOfFame?.adapter = adapter
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                // Observe top pledgers
                launch {
                    viewModel.topPledgers.collect { users ->
                        _binding?.let { b ->
                            adapter.submitList(users)

                            b.tvEmpty.visibility =
                                if (users.isEmpty()) View.VISIBLE
                                else View.GONE

                            b.podiumCard.visibility =
                                if (users.isNotEmpty()) View.VISIBLE
                                else View.GONE

                            updatePodium(users)
                        }
                    }
                }
            }
        }
    }

    private fun updatePodium(users: List<User>) {
        if (!isAdded || isDetached) return

        // 1st place
        if (users.isNotEmpty()) {
            val u = users[0]
            _binding?.tvRank1Name?.text = u.displayName
            _binding?.tvRank1Pp?.text   = "⭐ ${u.pledgePoints} PP"
            loadPodiumPhoto(
                user       = u,
                photoView  = _binding?.ivRank1Photo,
                letterView = _binding?.tvRank1Letter,
                bgDrawable = R.drawable.circle_avatar_gold
            )
        }

        // 2nd place
        if (users.size >= 2) {
            val u = users[1]
            _binding?.tvRank2Name?.text = u.displayName
            _binding?.tvRank2Pp?.text   = "⭐ ${u.pledgePoints} PP"
            loadPodiumPhoto(
                user       = u,
                photoView  = _binding?.ivRank2Photo,
                letterView = _binding?.tvRank2Letter,
                bgDrawable = R.drawable.circle_avatar_silver
            )
        }

        // 3rd place
        if (users.size >= 3) {
            val u = users[2]
            _binding?.tvRank3Name?.text = u.displayName
            _binding?.tvRank3Pp?.text   = "⭐ ${u.pledgePoints} PP"
            loadPodiumPhoto(
                user       = u,
                photoView  = _binding?.ivRank3Photo,
                letterView = _binding?.tvRank3Letter,
                bgDrawable = R.drawable.circle_avatar_bronze
            )
        }
    }

    private fun loadPodiumPhoto(
        user: User,
        photoView: android.widget.ImageView?,
        letterView: android.widget.TextView?,
        bgDrawable: Int
    ) {
        if (!isAdded || isDetached) return
        if (photoView == null || letterView == null) return

        if (user.profileImageUrl.isNotEmpty()) {
            photoView.visibility  = View.VISIBLE
            letterView.visibility = View.GONE
            Glide.with(this)
                .load(user.profileImageUrl)
                .transform(CircleCrop())
                .placeholder(bgDrawable)
                .error(bgDrawable)
                .into(photoView)
        } else {
            photoView.visibility  = View.GONE
            letterView.visibility = View.VISIBLE
            letterView.text = user.displayName
                .firstOrNull()
                ?.uppercaseChar()
                ?.toString() ?: "?"
            letterView.setBackgroundResource(bgDrawable)
        }
    }

    private fun shareUser(user: User) {
        val shareText = buildString {
            appendLine("🏆 I'm on the Shaale-Vikas Hall of Fame!")
            appendLine()
            appendLine("${user.rank.emoji} ${user.rank.displayName}")
            appendLine("⭐ ${user.pledgePoints} PP")
            appendLine(
                "💰 ₹${
                    String.format("%,.0f", user.totalPledgeValue)
                }"
            )
            appendLine()
            appendLine("Support your school! #ShaaleVikas")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Share"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}