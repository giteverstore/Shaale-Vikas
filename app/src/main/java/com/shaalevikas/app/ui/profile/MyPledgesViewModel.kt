package com.shaalevikas.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaalevikas.app.data.model.Pledge
import com.shaalevikas.app.data.repository.AuthRepository
import com.shaalevikas.app.data.repository.PledgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPledgesViewModel @Inject constructor(
    private val pledgeRepository: PledgeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _myPledges = MutableStateFlow<List<Pledge>>(emptyList())
    val myPledges: StateFlow<List<Pledge>> = _myPledges

    init {
        loadMyPledges()
    }

    private fun loadMyPledges() {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                pledgeRepository.getMyPledges(uid).collect {
                    _myPledges.value = it
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}