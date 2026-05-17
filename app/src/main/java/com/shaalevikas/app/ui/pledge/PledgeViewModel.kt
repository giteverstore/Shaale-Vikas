package com.shaalevikas.app.ui.pledge

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.shaalevikas.app.data.model.Pledge
import com.shaalevikas.app.data.repository.AuthRepository
import com.shaalevikas.app.data.repository.NeedsRepository
import com.shaalevikas.app.data.repository.PledgeRepository
import com.shaalevikas.app.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PledgeState {
    object Idle    : PledgeState()
    object Loading : PledgeState()
    object Success : PledgeState()
    data class Error(val message: String) : PledgeState()
}

@HiltViewModel
class PledgeViewModel @Inject constructor(
    private val pledgeRepository: PledgeRepository,
    private val needsRepository: NeedsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = SingleLiveEvent<PledgeState>()
    val state: LiveData<PledgeState> = _state

    private var isSubmitting = false

    fun submitPledge(
        needId: String,
        pledgeType: String,
        amount: Double,
        itemDescription: String,
        message: String
    ) {
        if (isSubmitting) return
        isSubmitting = true

        val user = authRepository.currentUser
        if (user == null) {
            _state.value = PledgeState.Error("Not logged in")
            isSubmitting = false
            return
        }

        _state.value = PledgeState.Loading

        viewModelScope.launch {
            try {
                val userData = authRepository.getCurrentUserData()
                val pledge = Pledge(
                    needId          = needId,
                    alumniId        = user.uid,
                    alumniName      = userData?.displayName
                        ?: user.email ?: "Anonymous",
                    pledgeType      = pledgeType,
                    amount          = amount,
                    itemDescription = itemDescription,
                    message         = message,
                    timestamp       = Timestamp.now()
                )

                val result = pledgeRepository.makePledge(pledge)

                if (result.isSuccess) {

                    val updateResult =
                        needsRepository.updatePledgedAmount(needId, amount)

                    if (updateResult.isSuccess) {
                        _state.postValue(PledgeState.Success)
                    } else {
                        _state.postValue(
                            PledgeState.Error(
                                updateResult.exceptionOrNull()?.message
                                    ?: "Failed to update need totals"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _state.postValue(
                    PledgeState.Error(
                        e.message ?: "Pledge failed"
                    )
                )
            } finally {
                isSubmitting = false
            }
        }
    }

    fun resetState() {
        isSubmitting = false
    }
}