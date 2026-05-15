package com.shaalevikas.app.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.shaalevikas.app.R
import com.shaalevikas.app.databinding.FragmentDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var adapter: NeedCardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding
            .inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupCategoryChips()
        observeNeeds()
        observeUserRole()
        setupFab()
    }

    private fun setupFab() {
        _binding?.fabAddNeed?.setOnClickListener {
            try {
                findNavController().navigate(
                    R.id.action_dashboard_to_create
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun observeUserRole() {
        // Use repeatOnLifecycle to stop collecting when view is destroyed
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.userRoleFlow.collect { role ->
                    _binding?.let {
                        if (role == "admin") {
                            it.fabAddNeed.show()
                        } else {
                            it.fabAddNeed.hide()
                        }
                        adapter.updateAdminStatus(role == "admin")
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = NeedCardAdapter(
            isAdmin     = false,
            onCardClick = { need ->
                try {
                    findNavController().navigate(
                        R.id.action_dashboard_to_detail,
                        bundleOf("needId" to need.id)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            onDeleteClick = { need ->
                showDeleteDialog(need.id, need.title)
            }
        )
        _binding?.recyclerNeeds?.layoutManager =
            LinearLayoutManager(requireContext())
        _binding?.recyclerNeeds?.adapter = adapter
    }

    private fun showDeleteDialog(needId: String, title: String) {
        if (!isAdded || isDetached) return
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Need")
            .setMessage("Delete \"$title\"?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteNeed(needId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupSearch() {
        _binding?.searchView
            ?.setOnQueryTextListener(
                object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(
                        query: String?
                    ) = true.also {
                        viewModel.setSearchQuery(query ?: "")
                    }
                    override fun onQueryTextChange(
                        newText: String?
                    ) = true.also {
                        viewModel.setSearchQuery(newText ?: "")
                    }
                }
            )
    }

    private fun setupCategoryChips() {
        val categories = listOf(
            "All", "Furniture", "Sanitation",
            "Classroom", "Sports", "Misc"
        )
        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text       = category
                isCheckable = true
                isChecked  = category == "All"
                setOnCheckedChangeListener { _, checked ->
                    if (checked) viewModel.setCategory(category)
                }
            }
            _binding?.chipGroupCategory?.addView(chip)
        }
    }

    private fun observeNeeds() {
        // repeatOnLifecycle STOPS collecting when fragment
        // view is destroyed and RESTARTS when it comes back
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.filteredNeeds.collect { needs ->
                    // _binding is guaranteed non-null here
                    // because repeatOnLifecycle stops when
                    // view is destroyed
                    _binding?.let { b ->
                        adapter.submitList(needs)
                        b.tvEmpty.visibility =
                            if (needs.isEmpty()) View.VISIBLE
                            else View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}