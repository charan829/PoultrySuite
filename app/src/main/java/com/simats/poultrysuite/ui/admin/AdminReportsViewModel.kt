package com.simats.poultrysuite.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.poultrysuite.data.model.AdminReportsResponse
import com.simats.poultrysuite.data.remote.PoultryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportsUiState(
    val isLoading: Boolean = false,
    val data: AdminReportsResponse? = null,
    val error: String? = null
)

@HiltViewModel
class AdminReportsViewModel @Inject constructor(
    private val api: PoultryApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState(isLoading = true))
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadReports()
    }

    private fun loadReports() {
        viewModelScope.launch {
            _uiState.value = ReportsUiState(isLoading = true)
            try {
                val reports = api.getReports()
                _uiState.value = ReportsUiState(data = reports)
            } catch (e: Exception) {
                _uiState.value = ReportsUiState(error = e.message ?: "Unknown error")
            }
        }
    }
}
