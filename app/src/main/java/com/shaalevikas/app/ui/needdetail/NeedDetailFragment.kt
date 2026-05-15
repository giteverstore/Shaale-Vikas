package com.shaalevikas.app.ui.needdetail

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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.shaalevikas.app.R
import com.shaalevikas.app.data.model.Need
import com.shaalevikas.app.databinding.FragmentNeedDetailBinding
import com.shaalevikas.app.ui.admin.CreateNeedState
import com.shaalevikas.app.ui.admin.CreateNeedViewModel
import com.shaalevikas.app.ui.pledge.PledgeBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NeedDetailFragment : Fragment() {

    private var _binding: FragmentNeedDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NeedDetailViewModel by viewModels()
    private val createNeedViewModel: CreateNeedViewModel by viewModels()

    private var selectedAfterPhotoUri: Uri? = null

    // Store current need for use across functions
    private var currentNeed: Need? = null

    private val pickAfterPhoto = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedAfterPhotoUri = it
            _binding?.ivAfterPhoto?.setImageURI(it)
            _binding?.ivAfterPhoto?.visibility      = View.VISIBLE
            _binding?.tvAfterLabel?.visibility      = View.VISIBLE
            _binding?.tvAfterPhotoStatus?.text      = "✅ After photo selected"
            _binding?.tvAfterPhotoStatus?.visibility = View.VISIBLE
            _binding?.btnConfirmFulfill?.visibility  = View.VISIBLE
        }
    }

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) pickAfterPhoto.launch("image/*")
        else showSnackbar("Permission required")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNeedDetailBinding
            .inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val needId = arguments?.getString("needId") ?: return
        viewModel.loadNeed(needId)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        observeNeed()
        observePledges()
        observeFulfillState()
        setupStaticClickListeners()
    }

    // Setup click listeners that don't depend on need data
    private fun setupStaticClickListeners() {
        // Pick after photo button
        _binding?.btnPickAfterPhoto?.setOnClickListener {
            checkPermissionAndPickAfterPhoto()
        }

        // Confirm fulfill button
        _binding?.btnConfirmFulfill?.setOnClickListener {
            val need = currentNeed
            if (need == null) {
                showSnackbar("Need data not loaded")
                return@setOnClickListener
            }
            if (selectedAfterPhotoUri == null) {
                showSnackbar("Please select an after photo first")
                return@setOnClickListener
            }
            createNeedViewModel.markFulfilled(
                needId          = need.id,
                afterPhotoUri   = selectedAfterPhotoUri,
                contentResolver = requireContext().contentResolver
            )
        }

        // Pledge button
        _binding?.btnPledge?.setOnClickListener {
            val need = currentNeed ?: return@setOnClickListener
            try {
                PledgeBottomSheet
                    .newInstance(need.id, need.title)
                    .show(parentFragmentManager, "PledgeSheet")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun observeNeed() {
        lifecycleScope.launch {
            try {
                viewModel.need.collect { need ->
                    need ?: return@collect
                    if (!isAdded || isDetached) return@collect

                    // Store in class variable
                    currentNeed = need

                    // Basic info
                    _binding?.tvTitle?.text       = need.title
                    _binding?.tvCategory?.text    = need.category
                    _binding?.tvDescription?.text = need.description
                    _binding?.tvUrgency?.text     = "Urgency: ${need.urgency}"

                    // Funding info
                    _binding?.tvCostEstimate?.text =
                        "Cost Estimate: ₹${
                            String.format("%,.0f", need.costEstimate)
                        }"
                    _binding?.tvTarget?.text =
                        "Target: ₹${
                            String.format("%,.0f", need.targetAmount)
                        }"
                    _binding?.tvPledged?.text =
                        "Pledged: ₹${
                            String.format("%,.0f", need.pledgedAmount)
                        }"

                    // Progress
                    _binding?.progressBar?.progress = need.progressPercent
                    _binding?.tvProgress?.text =
                        "${need.progressPercent}% of funds " +
                                "collected for ${need.title}"

                    // Before Photo
                    android.util.Log.d(
                        "NeedDetailFragment",
                        "Before URL: '${need.beforePhotoUrl}'"
                    )
                    loadBeforePhoto(need.beforePhotoUrl)

                    // After Photo
                    if (need.afterPhotoUrl.isNotEmpty()) {
                        loadAfterPhoto(need.afterPhotoUrl)
                    }

                    // Admin / Alumni Controls
                    updateControlsForRole(need)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadBeforePhoto(url: String) {
        _binding?.tvBeforeLabel?.visibility = View.VISIBLE
        _binding?.ivBeforePhoto?.visibility = View.VISIBLE

        if (url.isNotEmpty()) {
            _binding?.ivBeforePhoto?.let { iv ->
                Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .centerCrop()
                    .into(iv)
            }
        } else {
            _binding?.ivBeforePhoto?.setImageResource(
                R.drawable.ic_placeholder
            )
        }
    }

    private fun loadAfterPhoto(url: String) {
        _binding?.tvAfterLabel?.visibility = View.VISIBLE
        _binding?.ivAfterPhoto?.visibility = View.VISIBLE

        _binding?.ivAfterPhoto?.let { iv ->
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .centerCrop()
                .into(iv)
        }
    }

    private fun updateControlsForRole(need: Need) {
        lifecycleScope.launch {
            try {
                val role = viewModel.getUserRole()
                if (!isAdded || isDetached) return@launch

                when {
                    role == "admin" && need.status == "active" -> {
                        _binding?.adminFulfillSection?.visibility =
                            View.VISIBLE
                        _binding?.btnPledge?.visibility =
                            View.GONE
                        _binding?.tvFulfilledBadge?.visibility =
                            View.GONE
                    }
                    need.status == "active" -> {
                        _binding?.btnPledge?.visibility =
                            View.VISIBLE
                        _binding?.adminFulfillSection?.visibility =
                            View.GONE
                        _binding?.tvFulfilledBadge?.visibility =
                            View.GONE
                    }
                    else -> {
                        // Fulfilled
                        _binding?.btnPledge?.visibility =
                            View.GONE
                        _binding?.adminFulfillSection?.visibility =
                            View.GONE
                        _binding?.tvFulfilledBadge?.visibility =
                            View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun observePledges() {
        lifecycleScope.launch {
            try {
                viewModel.pledges.collect { pledges ->
                    if (!isAdded || isDetached) return@collect
                    _binding?.tvPledgerCount?.text =
                        "${pledges.size} alumni have pledged"
                    val names = pledges
                        .joinToString(", ") { it.alumniName }
                    _binding?.tvPledgers?.text = names.ifEmpty {
                        "Be the first to pledge!"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun observeFulfillState() {
        createNeedViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CreateNeedState.Loading -> {
                    _binding?.fulfillProgress?.visibility  = View.VISIBLE
                    _binding?.btnConfirmFulfill?.isEnabled = false
                }
                is CreateNeedState.Success -> {
                    _binding?.fulfillProgress?.visibility  = View.GONE
                    _binding?.btnConfirmFulfill?.isEnabled = true
                    createNeedViewModel.resetState()
                    showSnackbar("✅ Need marked as fulfilled!")
                    try {
                        if (isAdded && !isDetached) {
                            findNavController().popBackStack()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                is CreateNeedState.Error -> {
                    _binding?.fulfillProgress?.visibility  = View.GONE
                    _binding?.btnConfirmFulfill?.isEnabled = true
                    showSnackbar(state.message)
                }
                else -> {
                    _binding?.fulfillProgress?.visibility  = View.GONE
                    _binding?.btnConfirmFulfill?.isEnabled = true
                }
            }
        }
    }

    private fun checkPermissionAndPickAfterPhoto() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == android.content.pm.PackageManager
                        .PERMISSION_GRANTED
                ) {
                    pickAfterPhoto.launch("image/*")
                } else {
                    requestPermission.launch(
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                }
            }
            else -> {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == android.content.pm.PackageManager
                        .PERMISSION_GRANTED
                ) {
                    pickAfterPhoto.launch("image/*")
                } else {
                    requestPermission.launch(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                }
            }
        }
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