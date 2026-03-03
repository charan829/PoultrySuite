package com.simats.poultrysuite.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.poultrysuite.data.model.AdminSalesStats
import com.simats.poultrysuite.data.model.TransactionDetails
import com.simats.poultrysuite.data.remote.PoultryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val api: PoultryApi
) : ViewModel() {

    private val _salesState = MutableStateFlow<SalesState>(SalesState.Loading)
    val salesState = _salesState.asStateFlow()

    private val _transactionState = MutableStateFlow<TransactionState>(TransactionState.Loading)
    val transactionState = _transactionState.asStateFlow()

    fun loadSalesData() {
        viewModelScope.launch {
            try {
                _salesState.value = SalesState.Loading
                val stats = api.getAdminSales()
                _salesState.value = SalesState.Success(stats)
            } catch (e: Exception) {
                _salesState.value = SalesState.Error(e.message ?: "Failed to load sales data")
            }
        }
    }

    fun loadTransactionDetails(id: String) {
        viewModelScope.launch {
            try {
                _transactionState.value = TransactionState.Loading
                val details = api.getTransactionDetails(id)
                _transactionState.value = TransactionState.Success(details)
            } catch (e: Exception) {
                _transactionState.value = TransactionState.Error(e.message ?: "Failed to load transaction details")
            }
        }
    }
}

sealed class SalesState {
    object Loading : SalesState()
    data class Success(val stats: AdminSalesStats) : SalesState()
    data class Error(val message: String) : SalesState()
}

sealed class TransactionState {
    object Loading : TransactionState()
    data class Success(val details: TransactionDetails) : TransactionState()
    data class Error(val message: String) : TransactionState()
}
