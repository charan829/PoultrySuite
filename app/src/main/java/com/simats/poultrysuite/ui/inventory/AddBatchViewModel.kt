package com.simats.poultrysuite.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.poultrysuite.data.remote.PoultryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddBatchViewModel @Inject constructor(
    private val api: PoultryApi
) : ViewModel() {

    private val _state = MutableStateFlow<AddBatchState>(AddBatchState.Idle)
    val state = _state.asStateFlow()

    fun saveBatch(type: String, count: String, ageDays: String) {
        if (count.isBlank() || count.toIntOrNull() == null) {
            _state.value = AddBatchState.Error("Please enter a valid quantity")
            return
        }
        viewModelScope.launch {
            try {
                _state.value = AddBatchState.Saving
                val body = mapOf(
                    "type" to type,
                    "count" to count,
                    "ageDays" to (ageDays.ifBlank { "0" })
                )
                api.addBatch(body)
                _state.value = AddBatchState.Success
            } catch (e: Exception) {
                _state.value = AddBatchState.Error(e.message ?: "Failed to save batch")
            }
        }
    }

    fun resetState() { _state.value = AddBatchState.Idle }
}

sealed class AddBatchState {
    object Idle : AddBatchState()
    object Saving : AddBatchState()
    object Success : AddBatchState()
    data class Error(val message: String) : AddBatchState()
}
