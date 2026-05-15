package com.shaalevikas.app.ui.profile

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.shaalevikas.app.databinding.BottomSheetProfilePhotoBinding

class ProfilePhotoBottomSheet(
    private val hasPhoto: Boolean,
    private val onViewPhoto: () -> Unit,
    private val onEditPhoto: () -> Unit,
    private val onRemovePhoto: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetProfilePhotoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetProfilePhotoBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOptions()
        setupCancel()
    }

    private fun setupOptions() {

        // ── VIEW PHOTO ────────────────────────────────
        // ALWAYS available (shows custom photo OR letter avatar)
        applyOptionStyle(
            layout        = binding.layoutViewPhoto,
            iconBg        = binding.viewViewPhotoBg,
            icon          = binding.ivViewPhoto,
            label         = binding.tvViewPhoto,
            subLabel      = binding.tvViewPhotoSub,
            bgColor       = "#1565C0",   // dark blue
            iconColor     = "#FFFFFF",   // white icon
            labelColor    = "#212121",   // dark text
            subLabelColor = "#9E9E9E",
            enabled       = true         // always enabled
        )

        // ── UPLOAD PHOTO ──────────────────────────────
        // ALWAYS available
        applyOptionStyle(
            layout        = binding.layoutEditPhoto,
            iconBg        = binding.viewEditPhotoBg,
            icon          = binding.ivEditPhoto,
            label         = binding.tvEditPhoto,
            subLabel      = binding.tvEditPhotoSub,
            bgColor       = "#2E7D32",   // dark green
            iconColor     = "#FFFFFF",   // white icon
            labelColor    = "#212121",   // dark text
            subLabelColor = "#9E9E9E",
            enabled       = true         // always enabled
        )

        // ── REMOVE PHOTO ──────────────────────────────
        // Only available when custom photo exists
        if (hasPhoto) {
            applyOptionStyle(
                layout        = binding.layoutRemovePhoto,
                iconBg        = binding.viewRemovePhotoBg,
                icon          = binding.ivRemovePhoto,
                label         = binding.tvRemovePhoto,
                subLabel      = binding.tvRemovePhotoSub,
                bgColor       = "#C62828",   // dark red
                iconColor     = "#FFFFFF",   // white icon
                labelColor    = "#C62828",   // dark red text
                subLabelColor = "#9E9E9E",
                enabled       = true
            )
        } else {
            applyOptionStyle(
                layout        = binding.layoutRemovePhoto,
                iconBg        = binding.viewRemovePhotoBg,
                icon          = binding.ivRemovePhoto,
                label         = binding.tvRemovePhoto,
                subLabel      = binding.tvRemovePhotoSub,
                bgColor       = "#FFCDD2",   // light red
                iconColor     = "#EF9A9A",   // light red icon
                labelColor    = "#EF9A9A",   // light red text
                subLabelColor = "#BDBDBD",
                enabled       = false
            )
        }

        // ── CLICK LISTENERS ───────────────────────────

        // View → always clickable
        binding.layoutViewPhoto.setOnClickListener {
            dismiss()
            onViewPhoto()
        }

        // Upload → always clickable
        binding.layoutEditPhoto.setOnClickListener {
            dismiss()
            onEditPhoto()
        }

        // Remove → only clickable when photo exists
        binding.layoutRemovePhoto.setOnClickListener {
            if (hasPhoto) {
                dismiss()
                onRemovePhoto()
            }
        }
    }

    private fun applyOptionStyle(
        layout        : View,
        iconBg        : View,
        icon          : android.widget.ImageView,
        label         : android.widget.TextView,
        subLabel      : android.widget.TextView,
        bgColor       : String,
        iconColor     : String,
        labelColor    : String,
        subLabelColor : String,
        enabled       : Boolean
    ) {
        // Circle background color
        iconBg.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(
                Color.parseColor(bgColor)
            )
        )

        // Icon color
        icon.setColorFilter(
            Color.parseColor(iconColor),
            android.graphics.PorterDuff.Mode.SRC_IN
        )

        // Label color
        label.setTextColor(Color.parseColor(labelColor))

        // Sub label color
        subLabel.setTextColor(Color.parseColor(subLabelColor))

        // Enable/disable
        layout.isEnabled   = enabled
        layout.isClickable = enabled
    }

    private fun setupCancel() {
        binding.btnCancel.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}