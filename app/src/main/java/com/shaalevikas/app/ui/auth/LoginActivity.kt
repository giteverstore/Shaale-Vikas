package com.shaalevikas.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.shaalevikas.app.R
import com.shaalevikas.app.databinding.ActivityLoginBinding
import com.shaalevikas.app.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private var isAdminLogin = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (viewModel.getCurrentUser() != null) {
            navigateToMain("alumni")
            return
        }

        // Set default selection
        setAdminSelected(true)

        binding.btnToggleAdmin.setOnClickListener {
            setAdminSelected(true)
        }

        binding.btnToggleAlumni.setOnClickListener {
            setAdminSelected(false)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty()) {
                binding.etEmail.error = "Enter email"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.etPassword.error = "Enter password"
                return@setOnClickListener
            }

            viewModel.login(
                email = email,
                password = password,
                expectedRole = if (isAdminLogin) "admin" else "alumni"
            )
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        observeAuthState()
    }

    private fun setAdminSelected(adminSelected: Boolean) {
        isAdminLogin = adminSelected

        val greenColor = ContextCompat.getColor(this, R.color.primary_green)
        val whiteColor = ContextCompat.getColor(this, android.R.color.white)
        val transparentColor = ContextCompat.getColor(
            this, android.R.color.transparent
        )

        if (adminSelected) {
            // Admin button → filled green background, white text
            binding.btnToggleAdmin.apply {
                backgroundTintList = android.content.res.ColorStateList
                    .valueOf(greenColor)
                setTextColor(whiteColor)
                strokeWidth = 0
            }
            // Alumni button → white background, green text
            binding.btnToggleAlumni.apply {
                backgroundTintList = android.content.res.ColorStateList
                    .valueOf(whiteColor)
                setTextColor(greenColor)
                strokeWidth = 2
                strokeColor = android.content.res.ColorStateList
                    .valueOf(greenColor)
            }
            binding.tvLoginAs.text = "Logging in as Admin 👑"
            binding.tvRegister.visibility = View.GONE
        } else {
            // Alumni button → filled green background, white text
            binding.btnToggleAlumni.apply {
                backgroundTintList = android.content.res.ColorStateList
                    .valueOf(greenColor)
                setTextColor(whiteColor)
                strokeWidth = 0
            }
            // Admin button → white background, green text
            binding.btnToggleAdmin.apply {
                backgroundTintList = android.content.res.ColorStateList
                    .valueOf(whiteColor)
                setTextColor(greenColor)
                strokeWidth = 2
                strokeColor = android.content.res.ColorStateList
                    .valueOf(greenColor)
            }
            binding.tvLoginAs.text = "Logging in as Alumni 🎓"
            binding.tvRegister.visibility = View.VISIBLE
        }
    }

    private fun observeAuthState() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                    binding.btnToggleAdmin.isEnabled = false
                    binding.btnToggleAlumni.isEnabled = false
                }
                is AuthState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    navigateToMain(state.role)
                }
                is AuthState.RoleError -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    binding.btnToggleAdmin.isEnabled = true
                    binding.btnToggleAlumni.isEnabled = true
                    Snackbar.make(
                        binding.root,
                        state.message,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                is AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    binding.btnToggleAdmin.isEnabled = true
                    binding.btnToggleAlumni.isEnabled = true
                    Snackbar.make(
                        binding.root,
                        state.message,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                else -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    binding.btnToggleAdmin.isEnabled = true
                    binding.btnToggleAlumni.isEnabled = true
                }
            }
        }
    }

    private fun navigateToMain(role: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(MainActivity.EXTRA_USER_ROLE, role)
        startActivity(intent)
        finish()
    }
}