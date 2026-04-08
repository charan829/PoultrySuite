package com.simats.poultrysuite.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.poultrysuite.data.model.InventoryResponse
import com.simats.poultrysuite.data.remote.PoultryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val api: PoultryApi
) : ViewModel() {

    private val _state = MutableStateFlow<InventoryState>(InventoryState.Loading)
    val state = _state.asStateFlow()

    init { loadInventory() }

    fun loadInventory() {
        viewModelScope.launch {
            try {
                _state.value = InventoryState.Loading
                val response = api.getInventory()
                _state.value = InventoryState.Success(response)
            } catch (e: Exception) {
                _state.value = InventoryState.Error(e.message ?: "Failed to load inventory")
            }
        }
    }

    fun addFeed(amountKg: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.addFeedStock(mapOf("amountKg" to amountKg))
                if (response.isSuccessful) {
                    onSuccess()
                    loadInventory()
                } else {
                    onError("Failed to add feed")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to add feed")
            }
        }
    }

    fun addMedicine(count: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.addMedicineStock(mapOf("count" to count))
                if (response.isSuccessful) {
                    onSuccess()
                    loadInventory()
                } else {
                    onError("Failed to add medicine")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to add medicine")
            }
        }
    }
}

sealed class InventoryState {
    object Loading : InventoryState()
    data class Success(val data: InventoryResponse) : InventoryState()
    data class Error(val message: String) : InventoryState()
}
