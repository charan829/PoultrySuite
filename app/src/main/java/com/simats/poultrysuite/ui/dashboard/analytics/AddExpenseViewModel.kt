package com.simats.poultrysuite.ui.dashboard.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.poultrysuite.data.remote.PoultryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val api: PoultryApi
) : ViewModel() {

    private val _state = MutableStateFlow<AddExpenseState>(AddExpenseState.Idle)
    val state = _state.asStateFlow()

    fun addExpense(category: String, amount: String, date: String, description: String) {
        viewModelScope.launch {
            try {
                if (amount.isBlank() || category.isBlank()) {
                    _state.value = AddExpenseState.Error("Category and Amount are required")
                    return@launch
                }
                _state.value = AddExpenseState.Loading
                val req = mapOf(
                    "category" to category,
                    "amount" to amount,
                    "date" to date,
                    "description" to description
                )
                val response = api.addExpense(req)
                if (response.isSuccessful) {
                    _state.value = AddExpenseState.Success
                } else {
                    _state.value = AddExpenseState.Error("Failed to add expense")
                }
            } catch (e: Exception) {
                _state.value = AddExpenseState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun resetState() {
        _state.value = AddExpenseState.Idle
    }
}

sealed class AddExpenseState {
    object Idle : AddExpenseState()
    object Loading : AddExpenseState()
    object Success : AddExpenseState()
    data class Error(val message: String) : AddExpenseState()
}
