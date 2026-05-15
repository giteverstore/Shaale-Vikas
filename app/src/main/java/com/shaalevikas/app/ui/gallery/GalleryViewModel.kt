package com.shaalevikas.app.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaalevikas.app.data.model.Need
import com.shaalevikas.app.data.repository.NeedsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val needsRepository: NeedsRepository
) : ViewModel() {

    private val _fulfilledNeeds = MutableStateFlow<List<Need>>(emptyList())
    val fulfilledNeeds: StateFlow<List<Need>> = _fulfilledNeeds

    init {
        viewModelScope.launch {
            needsRepository.getFulfilledNeeds().collect { _fulfilledNeeds.value = it }
        }
    }
}