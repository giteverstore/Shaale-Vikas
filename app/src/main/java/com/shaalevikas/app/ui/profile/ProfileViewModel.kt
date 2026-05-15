package com.shaalevikas.app.ui.profile

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.shaalevikas.app.data.model.User
import com.shaalevikas.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _profileImageUrl = MutableStateFlow<String?>(null)
    val profileImageUrl: StateFlow<String?> = _profileImageUrl

    private val _imageUploadState = MutableStateFlow<ImageUploadState>(
        ImageUploadState.Idle
    )
    val imageUploadState: StateFlow<ImageUploadState> = _imageUploadState

    init {
        loadUserData()
    }

    fun loadUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = authRepository.getCurrentUserData()
                if (user != null) {
                    _userData.value = user
                    _profileImageUrl.value = user.profileImageUrl
                    _isLoading.value = false
                }
                authRepository.getUserDataFlow().collect { flowUser ->
                    if (flowUser != null) {
                        _userData.value = flowUser
                        _profileImageUrl.value = flowUser.profileImageUrl
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }

    fun uploadProfileImage(
        uri: Uri,
        contentResolver: ContentResolver
    ) {
        val uid = authRepository.currentUser?.uid ?: return
        _imageUploadState.value = ImageUploadState.Loading
        viewModelScope.launch {
            try {
                val compressedBytes = compressImageTo640(
                    uri, contentResolver
                )
                val ref = storage.reference
                    .child("profile_images/$uid.jpg")
                ref.putBytes(compressedBytes).await()
                val downloadUrl = ref.downloadUrl.await().toString()
                firestore.collection("users")
                    .document(uid)
                    .update("profileImageUrl", downloadUrl)
                    .await()
                _profileImageUrl.value = downloadUrl
                _imageUploadState.value = ImageUploadState.Success(
                    "Profile photo updated!"
                )
            } catch (e: Exception) {
                _imageUploadState.value = ImageUploadState.Error(
                    e.message ?: "Upload failed"
                )
            }
        }
    }

    fun removeProfileImage() {
        val uid = authRepository.currentUser?.uid ?: return
        _imageUploadState.value = ImageUploadState.Loading
        viewModelScope.launch {
            try {
                try {
                    storage.reference
                        .child("profile_images/$uid.jpg")
                        .delete()
                        .await()
                } catch (e: Exception) {
                    // File may not exist
                }
                firestore.collection("users")
                    .document(uid)
                    .update("profileImageUrl", "")
                    .await()
                _profileImageUrl.value = null
                _imageUploadState.value = ImageUploadState.Success(
                    "Profile photo removed!"
                )
            } catch (e: Exception) {
                _imageUploadState.value = ImageUploadState.Error(
                    e.message ?: "Remove failed"
                )
            }
        }
    }

    private fun compressImageTo640(
        uri: Uri,
        contentResolver: ContentResolver
    ): ByteArray {
        val inputStream = contentResolver.openInputStream(uri)
        val original = android.graphics.BitmapFactory
            .decodeStream(inputStream)
        inputStream?.close()
        val scaled = android.graphics.Bitmap.createScaledBitmap(
            original, 640, 640, true
        )
        val outputStream = java.io.ByteArrayOutputStream()
        scaled.compress(
            android.graphics.Bitmap.CompressFormat.JPEG,
            85,
            outputStream
        )
        if (original != scaled) original.recycle()
        scaled.recycle()
        return outputStream.toByteArray()
    }

    fun logout() {
        authRepository.signOut()
    }
}

sealed class ImageUploadState {
    object Idle : ImageUploadState()
    object Loading : ImageUploadState()
    data class Success(val message: String) : ImageUploadState()
    data class Error(val message: String) : ImageUploadState()
}