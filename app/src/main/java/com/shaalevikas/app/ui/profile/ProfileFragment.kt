package com.shaalevikas.app.ui.profile

import android.Manifest
import android.content.Intent
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.shaalevikas.app.R
import com.shaalevikas.app.data.model.User
import com.shaalevikas.app.databinding.FragmentProfileBinding
import com.shaalevikas.app.ui.auth.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    // Image picker
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { uploadImage(it) }
    }

    // Permission launcher
    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) pickImage.launch("image/*")
        else showSnackbar("Permission required to pick photo")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding
            .inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        // Show cached data immediately
        viewModel.userData.value?.let { cachedUser ->
            showContent()
            displayUserData(cachedUser)
        } ?: showLoading()

        observeData()
        setupClickListeners()
    }

    private fun observeData() {
        // userData + isLoading
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                launch {
                    viewModel.userData.collect { user ->
                        user ?: return@collect
                        _binding?.let {
                            showContent()
                            displayUserData(user)
                        }
                    }
                }

                launch {
                    viewModel.isLoading.collect { loading ->
                        _binding?.let {
                            if (viewModel.userData.value == null) {
                                if (loading) showLoading()
                                else showContent()
                            }
                        }
                    }
                }

                launch {
                    viewModel.profileImageUrl.collect { url ->
                        _binding?.let {
                            updateProfileImage(url)
                        }
                    }
                }
            }
        }

        // Image upload state
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.imageUploadState.collect { state ->
                    _binding?.let {
                        when (state) {
                            is ImageUploadState.Loading -> {
                                it.ivEditAvatar.isEnabled    = false
                                it.viewAvatarClick.isEnabled = false
                            }
                            is ImageUploadState.Success -> {
                                it.ivEditAvatar.isEnabled    = true
                                it.viewAvatarClick.isEnabled = true
                                showSnackbar(state.message)
                            }
                            is ImageUploadState.Error -> {
                                it.ivEditAvatar.isEnabled    = true
                                it.viewAvatarClick.isEnabled = true
                                showSnackbar("Error: ${state.message}")
                            }
                            else -> {
                                it.ivEditAvatar.isEnabled    = true
                                it.viewAvatarClick.isEnabled = true
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showLoading() {
        _binding?.progressBar?.visibility  = View.VISIBLE
        _binding?.contentLayout?.visibility = View.GONE
    }

    private fun showContent() {
        _binding?.progressBar?.visibility  = View.GONE
        _binding?.contentLayout?.visibility = View.VISIBLE
    }

    private fun displayUserData(user: User) {
        _binding?.let { b ->
            // Avatar first letter
            val firstLetter = user.displayName
                .firstOrNull()
                ?.uppercaseChar()
                ?.toString() ?: "?"
            b.tvAvatar.text = firstLetter

            b.tvName.text = user.displayName.ifEmpty { "No Name" }
            b.tvEmail.text = user.email

            b.tvGradYear.text = if (user.graduationYear > 0)
                "${user.graduationYear}" else "N/A"

            b.tvCity.text = buildString {
                if (user.district.isNotEmpty()) {
                    append(user.district)
                    if (user.city.isNotEmpty()) append(", ")
                }
                if (user.city.isNotEmpty()) append(user.city)
                if (isEmpty()) append("N/A")
            }

            b.tvRole.text = if (user.role == "admin")
                "👑 Admin" else "🎓 Alumni"

            val rank = user.rank
            b.tvBadge.text = "${rank.emoji} ${rank.displayName}"
            b.tvBadge.setBackgroundResource(
                when (rank.tier) {
                    "bronze"   -> R.drawable.badge_bronze
                    "silver"   -> R.drawable.badge_silver
                    "gold"     -> R.drawable.badge_gold
                    "platinum" -> R.drawable.badge_platinum
                    "diamond"  -> R.drawable.badge_diamond
                    "ruby"     -> R.drawable.badge_ruby
                    else       -> R.drawable.badge_unranked
                }
            )

            b.tvTotalPledge.text =
                "₹${String.format("%,.0f", user.totalPledgeValue)}"

            updateProfileImage(user.profileImageUrl)
        }
    }

    private fun updateProfileImage(url: String?) {
        _binding?.let { b ->
            if (!url.isNullOrEmpty()) {
                b.ivProfileImage.visibility = View.VISIBLE
                b.tvAvatar.visibility       = View.GONE
                if (isAdded && !isDetached) {
                    Glide.with(this)
                        .load(url)
                        .transform(CircleCrop())
                        .placeholder(R.drawable.circle_avatar_white)
                        .error(R.drawable.circle_avatar_white)
                        .into(b.ivProfileImage)
                }
            } else {
                b.ivProfileImage.visibility = View.GONE
                b.tvAvatar.visibility       = View.VISIBLE
            }
        }
    }

    private fun showAvatarOptions() {
        val hasPhoto = !viewModel.profileImageUrl.value.isNullOrEmpty()
        ProfilePhotoBottomSheet(
            hasPhoto     = hasPhoto,
            onViewPhoto  = { viewProfilePhoto() },
            onEditPhoto  = { editProfilePhoto() },
            onRemovePhoto = { confirmRemovePhoto() }
        ).show(parentFragmentManager, "ProfilePhoto")
    }

    private fun viewProfilePhoto() {
        val url        = viewModel.profileImageUrl.value
        val userName   = viewModel.userData.value?.displayName ?: ""
        val firstLetter = userName.firstOrNull()
            ?.uppercaseChar()?.toString() ?: "?"

        val dialogView = layoutInflater.inflate(
            R.layout.dialog_view_photo, null
        )
        val ivFullPhoto = dialogView
            .findViewById<android.widget.ImageView>(R.id.iv_full_photo)
        val tvLetter = dialogView
            .findViewById<android.widget.TextView>(R.id.tv_letter_avatar)

        if (!url.isNullOrEmpty()) {
            ivFullPhoto.visibility = View.VISIBLE
            tvLetter.visibility    = View.GONE
            if (isAdded && !isDetached) {
                Glide.with(this).load(url).into(ivFullPhoto)
            }
        } else {
            ivFullPhoto.visibility = View.GONE
            tvLetter.visibility    = View.VISIBLE
            tvLetter.text          = firstLetter
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(userName)
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun editProfilePhoto() {
        checkPermissionAndPick()
    }

    private fun confirmRemovePhoto() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Remove Photo")
            .setMessage(
                "Are you sure you want to remove your profile photo?"
            )
            .setPositiveButton("Remove") { _, _ ->
                viewModel.removeProfileImage()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun uploadImage(uri: Uri) {
        viewModel.uploadProfileImage(
            uri,
            requireContext().contentResolver
        )
    }

    private fun checkPermissionAndPick() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == android.content.pm.PackageManager
                        .PERMISSION_GRANTED
                ) {
                    pickImage.launch("image/*")
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
                    pickImage.launch("image/*")
                } else {
                    requestPermission.launch(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                }
            }
        }
    }

    private fun setupClickListeners() {
        _binding?.viewAvatarClick?.setOnClickListener {
            showAvatarOptions()
        }
        _binding?.ivEditAvatar?.setOnClickListener {
            showAvatarOptions()
        }
        _binding?.layoutMyPledges?.setOnClickListener {
            MyPledgesBottomSheet()
                .show(parentFragmentManager, "MyPledges")
        }
        _binding?.layoutSettings?.setOnClickListener {
            SettingsBottomSheet()
                .show(parentFragmentManager, "Settings")
        }
        _binding?.layoutAbout?.setOnClickListener {
            AboutBottomSheet()
                .show(parentFragmentManager, "About")
        }
        _binding?.layoutShareApp?.setOnClickListener {
            shareApp()
        }
        _binding?.btnLogout?.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun shareApp() {
        val shareText = "🏫 I'm using Shaale-Vikas!\n" +
                "Helping rural schools grow.\n" +
                "#ShaaleVikas #GiveBack"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Share"))
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                viewModel.logout()
                val intent = Intent(
                    requireActivity(),
                    LoginActivity::class.java
                )
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSnackbar(message: String) {
        _binding?.root?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}