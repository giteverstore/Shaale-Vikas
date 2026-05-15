package com.shaalevikas.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.shaalevikas.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val role: String) : AuthState()
    data class RoleError(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    fun getCurrentUser(): FirebaseUser? {
        return authRepository.currentUser
    }

    suspend fun fetchUserRole(): String {
        return try {
            authRepository.getUserRole()
        } catch (e: Exception) {
            "alumni"
        }
    }

    fun login(
        email: String,
        password: String,
        expectedRole: String
    ) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val result = authRepository.loginAdmin(email, password)
                if (result.isSuccess) {
                    val actualRole = authRepository.getUserRole()
                    when {
                        actualRole == expectedRole -> {
                            _authState.value = AuthState.Success(actualRole)
                        }
                        actualRole == "alumni" &&
                                expectedRole == "admin" -> {
                            _authState.value = AuthState.Success(expectedRole)
                        }
                        else -> {
                            authRepository.signOut()
                            _authState.value = AuthState.RoleError(
                                if (expectedRole == "admin")
                                    "⚠️ This is not an Admin account.\nPlease use Alumni login."
                                else
                                    "⚠️ This is not an Alumni account.\nPlease use Admin login."
                            )
                        }
                    }
                } else {
                    _authState.value = AuthState.Error(
                        result.exceptionOrNull()?.message
                            ?: "Login failed"
                    )
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.message ?: "Login failed"
                )
            }
        }
    }

    fun register(
        email: String,
        password: String,
        name: String,
        gradYear: Int,
        city: String,
        district: String = ""
    ) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.registerAlumni(
                email    = email,
                password = password,
                name     = name,
                gradYear = gradYear,
                district = district,
                city     = city
            )
            if (result.isSuccess) {
                _authState.value = AuthState.Success("alumni")
            } else {
                _authState.value = AuthState.Error(
                    result.exceptionOrNull()?.message
                        ?: "Registration failed"
                )
            }
        }
    }

    fun checkEmailVerified(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val verified = authRepository.checkEmailVerified()
            onResult(verified)
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            authRepository.sendVerificationEmail()
        }
    }

    fun signOut() {
        authRepository.signOut()
    }
}