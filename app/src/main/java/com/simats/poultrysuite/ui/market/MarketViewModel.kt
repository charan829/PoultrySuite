package com.simats.poultrysuite.ui.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.poultrysuite.data.local.SessionManager
import com.simats.poultrysuite.data.model.ProductRequest
import com.simats.poultrysuite.data.remote.PoultryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val api: PoultryApi,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _marketState = MutableStateFlow<MarketState>(MarketState.Loading)
    val marketState = _marketState.asStateFlow()
    
    private val _ordersState = MutableStateFlow<OrdersState>(OrdersState.Loading)
    val ordersState = _ordersState.asStateFlow()
    
    // Simple way to expose role
    private val _userRole = MutableStateFlow<String?>(null)
    val userRole = _userRole.asStateFlow()

    init {
        loadListings()
        viewModelScope.launch {
            sessionManager.userRole.collect { role ->
                _userRole.value = role
            }
        }
    }

    fun loadListings() {
        viewModelScope.launch {
            try {
                _marketState.value = MarketState.Loading
                val listings = api.getListings()
                _marketState.value = MarketState.Success(listings)
            } catch (e: Exception) {
                _marketState.value = MarketState.Error(e.message ?: "Failed to load listings")
            }
        }
    }

    fun loadMyOrders() {
        viewModelScope.launch {
            try {
                _ordersState.value = OrdersState.Loading
                val orders = api.getMyOrders()
                _ordersState.value = OrdersState.Success(orders)
            } catch (e: Exception) {
                _ordersState.value = OrdersState.Error(e.message ?: "Failed to load orders")
            }
        }
    }

    fun createListing(type: String, quantity: String, price: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val request = mapOf(
                    "type" to type,
                    "quantity" to quantity,
                    "pricePerUnit" to price
                )
                api.createListing(request)
                loadListings() // Refresh
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to create listing")
            }
        }
    }
    
    fun placeOrder(productId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val request = mapOf("productId" to productId)
                api.placeOrder(request)
                loadListings() // Refresh to remove sold item
                onSuccess()
            } catch (e: Exception) {
                // Navigate or notify failure
            }
        }
    }
}

sealed class MarketState {
    object Loading : MarketState()
    data class Success(val listings: List<ProductRequest>) : MarketState()
    data class Error(val message: String) : MarketState()
}

sealed class OrdersState {
    object Loading : OrdersState()
    data class Success(val orders: List<com.simats.poultrysuite.data.model.Order>) : OrdersState()
    data class Error(val message: String) : OrdersState()
}
