package com.shaalevikas.app.ui.pledge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.shaalevikas.app.databinding.BottomSheetPledgeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PledgeBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPledgeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PledgeViewModel by viewModels()
    private var isFundsSelected = true

    companion object {
        fun newInstance(
            needId: String,
            needTitle: String
        ) = PledgeBottomSheet().apply {
            arguments = Bundle().apply {
                putString("needId", needId)
                putString("needTitle", needTitle)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetPledgeBinding
            .inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val needId    = arguments?.getString("needId") ?: return
        val needTitle = arguments?.getString("needTitle") ?: ""

        _binding?.tvNeedTitle?.text = "Pledging for: $needTitle"

        viewModel.resetState()
        selectFunds()

        _binding?.btnFunds?.setOnClickListener { selectFunds() }
        _binding?.btnItem?.setOnClickListener  { selectItem()  }
        _binding?.btnConfirmPledge?.setOnClickListener {
            submitPledge(needId)
        }

        // SingleLiveEvent fires only ONCE
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PledgeState.Loading -> {
                    _binding?.progressBar?.visibility     = View.VISIBLE
                    _binding?.btnConfirmPledge?.isEnabled = false
                }
                is PledgeState.Success -> {
                    _binding?.progressBar?.visibility     = View.GONE
                    _binding?.btnConfirmPledge?.isEnabled = true
                    onPledgeSuccess()
                }
                is PledgeState.Error -> {
                    _binding?.progressBar?.visibility     = View.GONE
                    _binding?.btnConfirmPledge?.isEnabled = true
                    _binding?.root?.let {
                        Snackbar.make(
                            it,
                            state.message,
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
                else -> {
                    _binding?.progressBar?.visibility     = View.GONE
                    _binding?.btnConfirmPledge?.isEnabled = true
                }
            }
        }
    }

    private fun onPledgeSuccess() {
        // Show snackbar via parent activity
        activity?.let { act ->
            act.window.decorView
                .findViewById<View>(android.R.id.content)
                ?.let { root ->
                    Snackbar.make(
                        root,
                        "🎉 Pledge recorded! Thank you!",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
        }
        // Dismiss
        dismissAllowingStateLoss()
    }

    private fun selectFunds() {
        isFundsSelected = true
        _binding?.apply {
            btnFunds.setBackgroundResource(
                com.shaalevikas.app.R.drawable.toggle_selected
            )
            btnFunds.setTextColor(
                requireContext().getColor(android.R.color.white)
            )
            btnItem.setBackgroundColor(
                android.graphics.Color.TRANSPARENT
            )
            btnItem.setTextColor(
                requireContext().getColor(
                    com.shaalevikas.app.R.color.primary_green
                )
            )
            sectionFunds.visibility = View.VISIBLE
            sectionItem.visibility  = View.GONE
        }
    }

    private fun selectItem() {
        isFundsSelected = false
        _binding?.apply {
            btnItem.setBackgroundResource(
                com.shaalevikas.app.R.drawable.toggle_selected
            )
            btnItem.setTextColor(
                requireContext().getColor(android.R.color.white)
            )
            btnFunds.setBackgroundColor(
                android.graphics.Color.TRANSPARENT
            )
            btnFunds.setTextColor(
                requireContext().getColor(
                    com.shaalevikas.app.R.color.primary_green
                )
            )
            sectionFunds.visibility = View.GONE
            sectionItem.visibility  = View.VISIBLE
        }
    }

    private fun submitPledge(needId: String) {
        if (isFundsSelected) {
            val amount = _binding?.etAmount?.text
                .toString().toDoubleOrNull() ?: 0.0
            val message = _binding?.etMessageFunds?.text
                .toString().trim()
            if (amount <= 0) {
                _binding?.etAmount?.error = "Enter valid amount"
                return
            }
            viewModel.submitPledge(
                needId          = needId,
                pledgeType      = "Funds",
                amount          = amount,
                itemDescription = "",
                message         = message
            )
        } else {
            val itemDesc = _binding?.etItemDescription?.text
                .toString().trim()
            val message = _binding?.etMessageItem?.text
                .toString().trim()
            if (itemDesc.isEmpty()) {
                _binding?.etItemDescription?.error =
                    "Enter item description"
                return
            }
            viewModel.submitPledge(
                needId          = needId,
                pledgeType      = "Item",
                amount          = 0.0,
                itemDescription = itemDesc,
                message         = message
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}