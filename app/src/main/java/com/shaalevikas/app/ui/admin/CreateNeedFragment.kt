package com.shaalevikas.app.ui.admin

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.shaalevikas.app.R
import com.shaalevikas.app.databinding.FragmentCreateNeedBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateNeedFragment : Fragment() {

    private var _binding: FragmentCreateNeedBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreateNeedViewModel by viewModels()
    private var selectedPhotoUri: Uri? = null

    // Track selected categories
    private val selectedCategories = mutableListOf<String>()

    private val allCategories = listOf(
        "Furniture",
        "Sanitation",
        "Classroom",
        "Sports",
        "Electrical",
        "Plumbing",
        "Painting",
        "Roofing",
        "Misc"
    )

    private val urgencyLevels = listOf(
        "Critical",
        "High",
        "Medium"
    )

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedPhotoUri = it
            binding.ivSelectedPhoto.setImageURI(it)
            binding.ivSelectedPhoto.visibility = View.VISIBLE
            binding.tvPhotoSelected.text       = "✅ Photo selected"
            binding.tvPhotoSelected.visibility = View.VISIBLE
        }
    }

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openImagePicker()
        else showSnackbar("Permission required")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateNeedBinding
            .inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupCategoryChips()
        setupUrgencyDropdown()
        setupAiButtons()
        setupOtherButtons()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupCategoryChips() {
        // Add option chips
        allCategories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true
                isChecked = false
                setChipBackgroundColorResource(android.R.color.white)
                setTextColor(
                    requireContext().getColor(R.color.primary_green)
                )
                chipStrokeWidth = 2f
                setChipStrokeColorResource(R.color.primary_green)

                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        addSelectedCategory(category)
                    } else {
                        removeSelectedCategory(category)
                    }
                }
            }
            binding.chipGroupCategories.addView(chip)
        }
    }

    private fun addSelectedCategory(category: String) {
        if (selectedCategories.contains(category)) return
        selectedCategories.add(category)
        addSelectedChip(category)
    }

    private fun removeSelectedCategory(category: String) {
        selectedCategories.remove(category)

        // Uncheck in option chips
        for (i in 0 until binding.chipGroupCategories.childCount) {
            val chip = binding.chipGroupCategories
                .getChildAt(i) as? Chip
            if (chip?.text == category) {
                chip.isChecked = false
                break
            }
        }

        // Remove from selected chips
        for (i in 0 until binding.chipGroupSelected.childCount) {
            val chip = binding.chipGroupSelected
                .getChildAt(i) as? Chip
            if (chip?.tag == category) {
                binding.chipGroupSelected.removeView(chip)
                break
            }
        }
    }

    private fun addSelectedChip(category: String) {
        val chip = Chip(requireContext()).apply {
            text = category
            tag = category
            isCloseIconVisible = true
            setChipBackgroundColorResource(R.color.primary_green)
            setTextColor(
                requireContext().getColor(android.R.color.white)
            )
            closeIconTint =
                android.content.res.ColorStateList.valueOf(
                    requireContext().getColor(android.R.color.white)
                )

            setOnCloseIconClickListener {
                removeSelectedCategory(category)
            }
        }
        binding.chipGroupSelected.addView(chip)
    }

    private fun setupUrgencyDropdown() {
        val adapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            urgencyLevels
        )
        binding.etUrgency.apply {
            setAdapter(adapter)
            setText(urgencyLevels[2], false)
            setOnClickListener { showDropDown() }
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) showDropDown()
            }
            threshold = 0
        }
    }

    private fun setupAiButtons() {
        binding.btnAiTitle.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            if (title.isEmpty()) {
                showSnackbar("Type something in title first")
                return@setOnClickListener
            }
            animateAiButton(binding.btnAiTitle)
            viewModel.generateAiTitle(title)
        }

        binding.btnAiDescription.setOnClickListener {
            val title   = binding.etTitle.text.toString().trim()
            val urgency = binding.etUrgency.text.toString().trim()

            when {
                title.isEmpty() -> {
                    showSnackbar("Please enter a title first")
                    binding.etTitle.requestFocus()
                }
                selectedCategories.isEmpty() -> {
                    showSnackbar("Please select at least one category")
                }
                else -> {
                    animateAiButton(binding.btnAiDescription)
                    viewModel.generateAiDescription(
                        title      = title,
                        categories = selectedCategories.toList(),
                        urgency    = urgency.ifEmpty { "Medium" }
                    )
                }
            }
        }
    }

    private fun animateAiButton(button: View) {
        button.animate()
            .scaleX(0.85f).scaleY(0.85f)
            .setDuration(100)
            .withEndAction {
                button.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(100).start()
            }.start()
    }

    private fun setupOtherButtons() {
        binding.btnPickPhoto.setOnClickListener {
            checkPermissionAndPick()
        }
        binding.btnPublish.setOnClickListener {
            publishNeed()
        }
    }

    private fun publishNeed() {
        val title = binding.etTitle.text
            .toString().trim()
        val description = binding.etDescription.text
            .toString().trim()
        val urgency = binding.etUrgency.text
            .toString().trim()
        val costEstimate = binding.etCostEstimate.text
            .toString().toDoubleOrNull() ?: 0.0
        val targetAmount = binding.etTargetAmount.text
            .toString().toDoubleOrNull() ?: costEstimate

        // Validate
        if (title.isEmpty()) {
            binding.etTitle.error = "Title is required"
            return
        }
        if (description.isEmpty()) {
            binding.etDescription.error = "Description is required"
            return
        }
        if (selectedCategories.isEmpty()) {
            showSnackbar("Please select at least one category")
            return
        }
        if (costEstimate <= 0) {
            binding.etCostEstimate.error = "Enter a valid cost"
            return
        }

        viewModel.publishNeed(
            title           = title,
            categories      = selectedCategories.toList(),
            description     = description,
            costEstimate    = costEstimate,
            targetAmount    = targetAmount,
            urgency         = urgency.ifEmpty { "Medium" },
            photoUri        = selectedPhotoUri,
            contentResolver = requireContext().contentResolver
        )
    }

    private fun observeViewModel() {
        // SingleLiveEvent fires only ONCE
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CreateNeedState.Loading -> {
                    _binding?.progressBar?.visibility = View.VISIBLE
                    _binding?.btnPublish?.isEnabled   = false
                    setAiButtonsEnabled(false)
                }
                is CreateNeedState.Success -> {
                    _binding?.progressBar?.visibility = View.GONE
                    _binding?.btnPublish?.isEnabled   = true
                    setAiButtonsEnabled(true)
                    onPublishSuccess()
                }
                is CreateNeedState.AiTitleSuggestion -> {
                    _binding?.progressBar?.visibility = View.GONE
                    _binding?.btnPublish?.isEnabled   = true
                    setAiButtonsEnabled(true)
                    showTitleSuggestionDialog(state.title)
                }
                is CreateNeedState.AiDescriptionSuggestion -> {
                    _binding?.progressBar?.visibility = View.GONE
                    _binding?.btnPublish?.isEnabled   = true
                    setAiButtonsEnabled(true)
                    _binding?.etDescription?.setText(state.description)
                    if (state.costEstimate.isNotEmpty()) {
                        _binding?.etCostEstimate?.setText(
                            state.costEstimate
                        )
                    }
                    showSnackbar("✨ AI suggestion applied!")
                }
                is CreateNeedState.Error -> {
                    _binding?.progressBar?.visibility = View.GONE
                    _binding?.btnPublish?.isEnabled   = true
                    setAiButtonsEnabled(true)
                    showSnackbar(state.message)
                }
                else -> {
                    _binding?.progressBar?.visibility = View.GONE
                    _binding?.btnPublish?.isEnabled   = true
                    setAiButtonsEnabled(true)
                }
            }
        }
    }

    private fun onPublishSuccess() {
        showSnackbar("✅ Need published successfully!")
        // Navigate back
        try {
            findNavController().popBackStack()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showTitleSuggestionDialog(suggestedTitle: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("✨ AI Title Suggestion")
            .setMessage(suggestedTitle)
            .setPositiveButton("Use This") { _, _ ->
                binding.etTitle.setText(suggestedTitle)
            }
            .setNegativeButton("Keep Mine", null)
            .show()
    }

    private fun setAiButtonsEnabled(enabled: Boolean) {
        binding.btnAiTitle.isEnabled       = enabled
        binding.btnAiDescription.isEnabled = enabled
        binding.btnAiTitle.alpha           = if (enabled) 1f else 0.5f
        binding.btnAiDescription.alpha     = if (enabled) 1f else 0.5f
    }

    private fun checkPermissionAndPick() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == android.content.pm.PackageManager
                        .PERMISSION_GRANTED
                ) openImagePicker()
                else requestPermission.launch(
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            }
            else -> {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == android.content.pm.PackageManager
                        .PERMISSION_GRANTED
                ) openImagePicker()
                else requestPermission.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
        }
    }

    private fun openImagePicker() {
        pickImage.launch("image/*")
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}