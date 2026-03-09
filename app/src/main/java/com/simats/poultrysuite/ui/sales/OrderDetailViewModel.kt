package com.simats.poultrysuite.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.poultrysuite.data.model.OrderDetail
import com.simats.poultrysuite.data.remote.PoultryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OrderDetailState {
    object Loading : OrderDetailState()
    data class Success(val order: OrderDetail) : OrderDetailState()
    data class Error(val message: String) : OrderDetailState()
}

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val api: PoultryApi
) : ViewModel() {

    private val _state = MutableStateFlow<OrderDetailState>(OrderDetailState.Loading)
    val state = _state.asStateFlow()

    fun loadOrder(id: String) {
        viewModelScope.launch {
            try {
                _state.value = OrderDetailState.Loading
                val detail = api.getOrderDetail(id)
                _state.value = OrderDetailState.Success(detail)
            } catch (e: Exception) {
                _state.value = OrderDetailState.Error(e.message ?: "Failed to load order")
            }
        }
    }

    fun markAsPaid(id: String) {
        viewModelScope.launch {
            try {
                api.markSaleAsPaid(id)
                // Reload to reflect updated status
                loadOrder(id)
            } catch (e: Exception) {
                // best-effort — optimistically update UI anyway
                val current = _state.value
                if (current is OrderDetailState.Success) {
                    _state.value = OrderDetailState.Success(current.order.copy(paymentStatus = "Paid"))
                }
            }
        }
    }
}
