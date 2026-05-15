package com.shaalevikas.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaalevikas.app.data.model.Need
import com.shaalevikas.app.data.repository.AuthRepository
import com.shaalevikas.app.data.repository.NeedsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val needsRepository: NeedsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _filteredNeeds = MutableStateFlow<List<Need>>(emptyList())
    val filteredNeeds: StateFlow<List<Need>> = _filteredNeeds

    private val _userRoleFlow = MutableStateFlow("alumni")
    val userRoleFlow: StateFlow<String> = _userRoleFlow

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("All")

    init {
        loadUserRole()
        observeNeeds()
    }

    private fun loadUserRole() {
        viewModelScope.launch {
            try {
                val role = authRepository.getUserRole()
                _userRoleFlow.value = role
                android.util.Log.d("DashboardViewModel", "Role loaded: $role")
            } catch (e: Exception) {
                _userRoleFlow.value = "alumni"
            }
        }
    }

    private fun observeNeeds() {
        viewModelScope.launch {
            try {
                needsRepository.getActiveNeeds().collect { needs ->
                    applyFilters(needs)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun applyFilters(needs: List<Need>) {
        val query = _searchQuery.value
        val category = _selectedCategory.value

        _filteredNeeds.value = needs
            .filter { need ->
                if (query.isBlank()) true
                else need.title.contains(query, ignoreCase = true) ||
                        need.description.contains(query, ignoreCase = true)
            }
            .filter { need ->
                if (category == "All") true
                else need.category == category
            }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            needsRepository.getActiveNeeds().collect { needs ->
                applyFilters(needs)
            }
        }
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
        viewModelScope.launch {
            needsRepository.getActiveNeeds().collect { needs ->
                applyFilters(needs)
            }
        }
    }

    fun deleteNeed(needId: String) {
        viewModelScope.launch {
            try {
                needsRepository.deleteNeed(needId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}