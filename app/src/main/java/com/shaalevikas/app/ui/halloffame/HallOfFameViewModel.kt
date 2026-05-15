package com.shaalevikas.app.ui.halloffame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaalevikas.app.data.model.User
import com.shaalevikas.app.data.repository.PledgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HallOfFameViewModel @Inject constructor(
    private val pledgeRepository: PledgeRepository
) : ViewModel() {

    private val _topPledgers = MutableStateFlow<List<User>>(emptyList())
    val topPledgers: StateFlow<List<User>> = _topPledgers

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            pledgeRepository.getTopPledgers().collect { users ->
                _topPledgers.value = users
                    .sortedByDescending { it.pledgePoints }
            }
        }
    }
}