package com.simats.poultrysuite.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.poultrysuite.data.model.BatchDetail
import com.simats.poultrysuite.data.remote.PoultryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BatchDetailViewModel @Inject constructor(
    private val api: PoultryApi
) : ViewModel() {

    private val _state = MutableStateFlow<BatchDetailState>(BatchDetailState.Loading)
    val state = _state.asStateFlow()

    fun loadBatch(batchId: String) {
        viewModelScope.launch {
            try {
                _state.value = BatchDetailState.Loading
                val detail = api.getBatchDetail(batchId)
                _state.value = BatchDetailState.Success(detail)
            } catch (e: Exception) {
                _state.value = BatchDetailState.Error(e.message ?: "Failed to load batch")
            }
        }
    }

    fun logMortality(batchId: String, count: String, cause: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val req = mapOf("count" to count, "cause" to cause)
                val response = api.logMortality(batchId, req)
                if (response.isSuccessful) {
                    onSuccess()
                    loadBatch(batchId)
                } else {
                    onError("Failed to log mortality")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            }
        }
    }

    fun logVaccination(batchId: String, name: String, date: String, status: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val req = mapOf("name" to name, "scheduledDate" to date, "status" to status)
                val response = api.logVaccination(batchId, req)
                if (response.isSuccessful) {
                    onSuccess()
                    loadBatch(batchId)
                } else {
                    onError("Failed to log vaccination")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            }
        }
    }

    fun logFeed(batchId: String, amountKg: String, notes: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val req = mapOf("amountKg" to amountKg, "notes" to notes)
                val response = api.logFeed(batchId, req)
                if (response.isSuccessful) {
                    onSuccess()
                    loadBatch(batchId)
                } else {
                    onError("Failed to log feed")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class BatchDetailState {
    object Loading : BatchDetailState()
    data class Success(val batch: BatchDetail) : BatchDetailState()
    data class Error(val message: String) : BatchDetailState()
}
