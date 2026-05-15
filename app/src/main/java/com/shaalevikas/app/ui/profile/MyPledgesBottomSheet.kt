package com.shaalevikas.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.shaalevikas.app.databinding.BottomSheetMyPledgesBinding
import com.shaalevikas.app.ui.halloffame.HallOfFameViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyPledgesBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetMyPledgesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MyPledgesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetMyPledgesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = MyPledgesAdapter()
        binding.recyclerMyPledges.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMyPledges.adapter = adapter

        lifecycleScope.launch {
            viewModel.myPledges.collect { pledges ->
                adapter.submitList(pledges)
                binding.tvEmpty.visibility =
                    if (pledges.isEmpty()) View.VISIBLE else View.GONE
                binding.tvPledgeCount.text = "${pledges.size} pledges made"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}