package com.simats.poultrysuite.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.poultrysuite.data.model.Farm
import com.simats.poultrysuite.data.remote.PoultryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val api: PoultryApi
) : ViewModel() {

    private val _farmState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val farmState = _farmState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            try {
                _farmState.value = DashboardState.Loading
                val farm = api.getDashboard()
                _farmState.value = DashboardState.Success(farm)
            } catch (e: Exception) {
                _farmState.value = DashboardState.Error(e.message ?: "Failed to load dashboard")
            }
        }
    }

    fun addBatch(type: String, count: String, age: String) {
        viewModelScope.launch {
            try {
                // Reload after adding batch
                val request = mapOf(
                    "type" to type,
                    "count" to count,
                    "ageDays" to age
                )
                api.addBatch(request) 
                loadDashboard()
            } catch (e: Exception) {
                _farmState.value = DashboardState.Error("Failed to add batch: ${e.message}")
            }
        }
    }
}

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(val farm: Farm) : DashboardState()
    data class Error(val message: String) : DashboardState()
}
