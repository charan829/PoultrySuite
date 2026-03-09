package com.simats.poultrysuite.ui.dashboard.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.poultrysuite.data.model.AnalyticsResponse
import com.simats.poultrysuite.data.remote.PoultryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val api: PoultryApi
) : ViewModel() {

    private val _state = MutableStateFlow<AnalyticsState>(AnalyticsState.Loading)
    val state = _state.asStateFlow()

    fun loadAnalytics() {
        viewModelScope.launch {
            try {
                _state.value = AnalyticsState.Loading
                val data = api.getAnalytics()
                _state.value = AnalyticsState.Success(data)
            } catch (e: Exception) {
                _state.value = AnalyticsState.Error(e.message ?: "Failed to load analytics")
            }
        }
    }
}

sealed class AnalyticsState {
    object Loading : AnalyticsState()
    data class Success(val data: AnalyticsResponse) : AnalyticsState()
    data class Error(val message: String) : AnalyticsState()
}
