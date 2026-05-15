package com.shaalevikas.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.shaalevikas.app.R
import com.shaalevikas.app.data.KarnatakaData
import com.shaalevikas.app.databinding.ActivityRegisterBinding
import com.shaalevikas.app.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    private var currentStep  = 1
    private val totalSteps   = 4
    private var resendTimer: CountDownTimer? = null

    private var regName     = ""
    private var regGradYear = 0
    private var regDistrict = ""
    private var regCity     = ""
    private var regEmail    = ""
    private var regPassword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupStep1()
        setupPasswordChecks()
        setupButtons()

        binding.tvLogin.setOnClickListener { finish() }
    }

    // ── STEP 1 ────────────────────────────────────────
    private fun setupStep1() {
        setupDistrictDropdown()
    }

    private fun setupDistrictDropdown() {
        val districtAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            KarnatakaData.districts
        )
        binding.etDistrict.apply {
            setAdapter(districtAdapter)
            setOnClickListener { showDropDown() }
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) showDropDown()
            }
            threshold = 0
            setOnItemClickListener { _, _, position, _ ->
                val selected = KarnatakaData.districts[position]
                loadCitiesForDistrict(selected)
            }
        }
    }

    private fun loadCitiesForDistrict(district: String) {
        val cities = KarnatakaData.citiesByDistrict[district]
            ?: emptyList()
        val cityAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            cities
        )
        binding.etCity.apply {
            isEnabled = true
            setText("", false)
            setAdapter(cityAdapter)
            setOnClickListener { showDropDown() }
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) showDropDown()
            }
            threshold = 0
        }
    }

    // ── STEP 2 ────────────────────────────────────────
    private fun setupPasswordChecks() {
        binding.etPassword.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, st: Int, c: Int, a: Int
                ) {}
                override fun onTextChanged(
                    s: CharSequence?, st: Int, c: Int, a: Int
                ) {}
                override fun afterTextChanged(s: Editable?) {
                    updatePasswordChecks(s.toString())
                }
            }
        )
    }

    private fun updatePasswordChecks(password: String) {
        val hasLength = password.length >= 8
        val hasLetter = password.any { it.isLetter() }
        val hasNumber = password.any { it.isDigit() }
        val hasSymbol = password.any { !it.isLetterOrDigit() }

        setCheck(binding.tvCheckLength, hasLength, "At least 8 characters")
        setCheck(binding.tvCheckLetter, hasLetter, "At least one letter")
        setCheck(binding.tvCheckNumber, hasNumber, "At least one number")
        setCheck(binding.tvCheckSymbol, hasSymbol, "At least one symbol")
    }

    private fun setCheck(
        textView: android.widget.TextView,
        passed: Boolean,
        text: String
    ) {
        textView.text = if (passed) "✅  $text" else "○  $text"
        textView.setTextColor(
            getColor(
                if (passed) R.color.primary_green
                else R.color.text_secondary
            )
        )
    }

    // ── BUTTONS ───────────────────────────────────────
    private fun setupButtons() {
        binding.btnNext.setOnClickListener {
            when (currentStep) {
                1 -> validateAndGoToStep2()
                2 -> validateAndGoToStep3()
                3 -> checkVerificationAndProceed()
                4 -> goToMain()
            }
        }
        binding.btnBack.setOnClickListener {
            goToPreviousStep()
        }
    }

    // ── STEP 1 VALIDATION ─────────────────────────────
    private fun validateAndGoToStep2() {
        val name     = binding.etName.text.toString().trim()
        val gradYear = binding.etGradYear.text.toString().trim()
        val district = binding.etDistrict.text.toString().trim()
        val city     = binding.etCity.text.toString().trim()
        val email    = binding.etEmail.text.toString().trim()

        when {
            name.isEmpty() -> {
                binding.etName.error = "Enter your full name"
                return
            }
            gradYear.isEmpty() -> {
                binding.etGradYear.error = "Enter graduation year"
                return
            }
            gradYear.toIntOrNull() == null ||
                    gradYear.toInt() < 1950 ||
                    gradYear.toInt() > 2030 -> {
                binding.etGradYear.error = "Enter a valid year"
                return
            }
            district.isEmpty() -> {
                showSnackbar("Please select a district")
                return
            }
            city.isEmpty() -> {
                showSnackbar("Please select a city")
                return
            }
            email.isEmpty() ||
                    !android.util.Patterns.EMAIL_ADDRESS
                        .matcher(email).matches() -> {
                binding.etEmail.error = "Enter a valid email"
                return
            }
        }

        regName     = name
        regGradYear = gradYear.toInt()
        regDistrict = district
        regCity     = city
        regEmail    = email

        goToNextStep()
    }

    // ── STEP 2 VALIDATION ─────────────────────────────
    private fun validateAndGoToStep3() {
        val password = binding.etPassword.text.toString()
        val confirm  = binding.etConfirmPassword.text.toString()

        val hasLength = password.length >= 8
        val hasLetter = password.any { it.isLetter() }
        val hasNumber = password.any { it.isDigit() }
        val hasSymbol = password.any { !it.isLetterOrDigit() }

        when {
            !hasLength || !hasLetter ||
                    !hasNumber || !hasSymbol -> {
                showSnackbar("Password does not meet requirements")
                return
            }
            password != confirm -> {
                binding.etConfirmPassword.error =
                    "Passwords do not match"
                return
            }
        }

        regPassword = password
        registerAndSendEmail()
    }

    // ── REGISTER + SEND EMAIL ─────────────────────────
    private fun registerAndSendEmail() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnNext.isEnabled      = false
        binding.btnBack.isEnabled      = false

        viewModel.register(
            email    = regEmail,
            password = regPassword,
            name     = regName,
            gradYear = regGradYear,
            city     = regCity,
            district = regDistrict
        )

        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnNext.isEnabled      = false
                }
                is AuthState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnNext.isEnabled      = true
                    binding.btnBack.isEnabled      = true

                    // Update email display
                    binding.tvVerificationEmail.text =
                        "We sent a verification link to\n$regEmail"

                    goToNextStep()
                    startResendTimer()
                }
                is AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnNext.isEnabled      = true
                    binding.btnBack.isEnabled      = true
                    showSnackbar(state.message)
                }
                else -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnNext.isEnabled      = true
                    binding.btnBack.isEnabled      = true
                }
            }
        }
    }

    // ── STEP 3: CHECK VERIFICATION ────────────────────
    private fun checkVerificationAndProceed() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnNext.isEnabled      = false
        binding.btnNext.text           = "Checking..."

        viewModel.checkEmailVerified { verified ->
            binding.progressBar.visibility = View.GONE
            binding.btnNext.isEnabled      = true
            binding.btnNext.text           = "I've Verified My Email ✅"

            if (verified) {
                // Email verified → go to success screen
                resendTimer?.cancel()
                binding.tvWelcomeName.text =
                    "Welcome, $regName! 🎓\n\n" +
                            "You are now part of the\n" +
                            "Shaale-Vikas alumni network!"
                goToNextStep()
            } else {
                // Not verified yet
                showSnackbar(
                    "Email not verified yet.\n" +
                            "Please check your inbox and click the link."
                )
            }
        }
    }

    // ── RESEND TIMER ──────────────────────────────────
    private fun startResendTimer() {
        setupResendButton()
        binding.tvResendOtp.isEnabled   = false
        binding.tvResendTimer.visibility = View.VISIBLE

        resendTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvResendTimer.text =
                    "Resend in ${millisUntilFinished / 1000}s"
            }
            override fun onFinish() {
                binding.tvResendOtp.isEnabled   = true
                binding.tvResendTimer.visibility = View.GONE
                binding.tvResendTimer.text       = ""
            }
        }.start()
    }

    private fun setupResendButton() {
        binding.tvResendOtp.setOnClickListener {
            resendTimer?.cancel()
            viewModel.resendVerificationEmail()
            showSnackbar("✅ Verification email resent!")
            startResendTimer()
        }
    }

    // ── STEP 4: GO TO MAIN ────────────────────────────
    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(MainActivity.EXTRA_USER_ROLE, "alumni")
        }
        startActivity(intent)
        finish()
    }

    // ── STEP NAVIGATION ───────────────────────────────
    private fun goToNextStep() {
        currentStep++
        updateStepUI()
        binding.viewFlipper.setInAnimation(
            this, R.anim.slide_in_right
        )
        binding.viewFlipper.setOutAnimation(
            this, R.anim.slide_out_left
        )
        binding.viewFlipper.showNext()
    }

    private fun goToPreviousStep() {
        currentStep--
        updateStepUI()
        binding.viewFlipper.setInAnimation(
            this, R.anim.slide_in_left
        )
        binding.viewFlipper.setOutAnimation(
            this, R.anim.slide_out_right
        )
        binding.viewFlipper.showPrevious()
    }

    private fun updateStepUI() {
        binding.stepProgress.progress = currentStep
        binding.tvStepTitle.text      = "Step $currentStep of $totalSteps"

        // Back button visibility
        binding.btnBack.visibility =
            if (currentStep > 1) View.VISIBLE else View.GONE

        // Next button text
        binding.btnNext.text = when (currentStep) {
            1    -> "Next →"
            2    -> "Create Account"
            3    -> "I've Verified My Email ✅"
            4    -> "Go to Dashboard 🎓"
            else -> "Next →"
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        resendTimer?.cancel()
    }
}