package com.simats.poultrysuite.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.poultrysuite.data.model.AdminStats
import com.simats.poultrysuite.data.remote.PoultryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val api: PoultryApi
) : ViewModel() {

    private val _statsState = MutableStateFlow<AdminState>(AdminState.Loading)
    val statsState = _statsState.asStateFlow()

    private val _farmsState = MutableStateFlow<FarmsState>(FarmsState.Loading)
    val farmsState = _farmsState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            try {
                _statsState.value = AdminState.Loading
                val stats = api.getAdminStats()
                _statsState.value = AdminState.Success(stats)
            } catch (e: Exception) {
                _statsState.value = AdminState.Error(e.message ?: "Failed to load stats")
            }
        }
    }

    fun loadFarms() {
        viewModelScope.launch {
            try {
                _farmsState.value = FarmsState.Loading
                val farms = api.getFarms()
                _farmsState.value = FarmsState.Success(farms)
            } catch (e: Exception) {
                _farmsState.value = FarmsState.Error(e.message ?: "Failed to load farms")
            }
        }
    }

    private val _farmDetailsState = MutableStateFlow<FarmDetailsState>(FarmDetailsState.Loading)
    val farmDetailsState = _farmDetailsState.asStateFlow()

    fun loadFarmDetails(id: String) {
        viewModelScope.launch {
            try {
                _farmDetailsState.value = FarmDetailsState.Loading
                val details = api.getFarmDetails(id)
                _farmDetailsState.value = FarmDetailsState.Success(details)
            } catch (e: Exception) {
                _farmDetailsState.value = FarmDetailsState.Error(e.message ?: "Failed to load farm details")
            }
        }
    }
}

sealed class AdminState {
    object Loading : AdminState()
    data class Success(val stats: AdminStats) : AdminState()
    data class Error(val message: String) : AdminState()
}

sealed class FarmsState {
    object Loading : FarmsState()
    data class Success(val farms: List<com.simats.poultrysuite.data.model.AdminFarmItem>) : FarmsState()
    data class Error(val message: String) : FarmsState()
}

sealed class FarmDetailsState {
    object Loading : FarmDetailsState()
    data class Success(val details: com.simats.poultrysuite.data.model.FarmDetails) : FarmDetailsState()
    data class Error(val message: String) : FarmDetailsState()
}
