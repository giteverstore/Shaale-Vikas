package com.shaalevikas.app.ui.profile

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.shaalevikas.app.databinding.ActivityFullScreenImageBinding

class FullScreenImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullScreenImageBinding
    private var isToolbarVisible = true

    companion object {
        const val EXTRA_IMAGE_URL = "extra_image_url"
        const val EXTRA_USER_NAME = "extra_user_name"
        const val EXTRA_LETTER    = "extra_letter"

        // WhatsApp standard profile photo size
        const val PHOTO_SIZE = 640
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullScreenImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        makeFullScreen()

        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)
        val userName = intent.getStringExtra(EXTRA_USER_NAME) ?: ""
        val letter   = intent.getStringExtra(EXTRA_LETTER) ?: "?"

        setupToolbar(userName)
        loadContent(imageUrl, letter)
        setupTapToToggle()
    }

    private fun makeFullScreen() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        )
        window.statusBarColor     = android.graphics.Color.BLACK
        window.navigationBarColor = android.graphics.Color.BLACK
    }

    private fun setupToolbar(userName: String) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title    = userName
            subtitle = "Profile Photo"
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadContent(imageUrl: String?, letter: String) {
        if (!imageUrl.isNullOrEmpty()) {
            // Custom photo → load at 640x640
            binding.ivFullPhoto.visibility    = View.VISIBLE
            binding.tvLetterAvatar.visibility = View.GONE

            Glide.with(this)
                .load(imageUrl)
                .apply(
                    RequestOptions()
                        .override(PHOTO_SIZE, PHOTO_SIZE) // 640x640 pixels
                        .centerCrop()
                )
                .placeholder(android.R.color.black)
                .error(android.R.color.black)
                .into(binding.ivFullPhoto)

        } else {
            // No custom photo → show letter on green bg
            binding.ivFullPhoto.visibility    = View.GONE
            binding.tvLetterAvatar.visibility = View.VISIBLE
            binding.tvLetter.text             = letter
        }
    }

    private fun setupTapToToggle() {
        binding.ivFullPhoto.setOnClickListener {
            toggleToolbar()
        }
        binding.tvLetterAvatar.setOnClickListener {
            toggleToolbar()
        }
    }

    private fun toggleToolbar() {
        isToolbarVisible = !isToolbarVisible
        binding.toolbar.visibility =
            if (isToolbarVisible) View.VISIBLE else View.GONE
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}