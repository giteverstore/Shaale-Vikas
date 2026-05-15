package com.shaalevikas.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.shaalevikas.app.R
import com.shaalevikas.app.databinding.ActivityMainBinding
import com.shaalevikas.app.ui.auth.AuthViewModel
import com.shaalevikas.app.ui.auth.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val authViewModel: AuthViewModel by viewModels()

    companion object {
        const val EXTRA_USER_ROLE = "extra_user_role"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()

        // Get role passed from LoginActivity
        val roleFromIntent = intent.getStringExtra(EXTRA_USER_ROLE)

        if (roleFromIntent != null) {
            // Role passed directly → use it immediately
            android.util.Log.d("MainActivity", "Role from intent: $roleFromIntent")
            updateNavForRole(roleFromIntent)
        } else {
            // No role in intent → fetch from Firestore
            checkRole()
        }
    }

    private fun setupNavigation() {
        try {
            val navHost = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            navController = navHost.navController
            binding.bottomNavigation.setupWithNavController(navController)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkRole() {
        val user = authViewModel.getCurrentUser()
        if (user == null) {
            navigateToLogin()
            return
        }
        lifecycleScope.launch {
            val role = authViewModel.fetchUserRole()
            android.util.Log.d("MainActivity", "Fetched role: $role")
            updateNavForRole(role)
        }
    }

    private fun updateNavForRole(role: String) {
        val menu = binding.bottomNavigation.menu
        val addNeedItem = menu.findItem(R.id.createNeedFragment)
        addNeedItem?.isVisible = (role == "admin")
        android.util.Log.d(
            "MainActivity",
            "Add Need visible: ${role == "admin"}"
        )
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}