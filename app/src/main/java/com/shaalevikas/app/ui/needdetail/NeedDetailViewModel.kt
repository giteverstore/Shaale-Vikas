package com.shaalevikas.app.ui.needdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaalevikas.app.data.model.Need
import com.shaalevikas.app.data.model.Pledge
import com.shaalevikas.app.data.repository.AuthRepository
import com.shaalevikas.app.data.repository.NeedsRepository
import com.shaalevikas.app.data.repository.PledgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NeedDetailViewModel @Inject constructor(
    private val needsRepository: NeedsRepository,
    private val pledgeRepository: PledgeRepository,
    val authRepository: AuthRepository
) : ViewModel() {

    private val _need = MutableStateFlow<Need?>(null)
    val need: StateFlow<Need?> = _need

    private val _pledges = MutableStateFlow<List<Pledge>>(emptyList())
    val pledges: StateFlow<List<Pledge>> = _pledges

    fun loadNeed(needId: String) {
        viewModelScope.launch {
            needsRepository.getNeedById(needId).collect { _need.value = it }
        }
        viewModelScope.launch {
            pledgeRepository.getPledgesForNeed(needId).collect { _pledges.value = it }
        }
    }

    suspend fun getUserRole() = authRepository.getUserRole()
    val currentUserId: String? get() = authRepository.currentUser?.uid
}