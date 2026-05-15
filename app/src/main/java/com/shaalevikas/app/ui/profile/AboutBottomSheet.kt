package com.shaalevikas.app.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.shaalevikas.app.databinding.BottomSheetAboutBinding

class AboutBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvVersion.text = "Version 1.0.0"
        binding.tvDescription.text =
            "Shaale-Vikas (ಶಾಲೆ-ವಿಕಾಸ) is a School-Alumni Bridge app " +
                    "that connects rural school headmasters with their alumni network. " +
                    "Together we build better schools for future generations."

        binding.btnClose.setOnClickListener { dismiss() }

        binding.tvPrivacyPolicy.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://shaalevikas.com/privacy")
            )
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}