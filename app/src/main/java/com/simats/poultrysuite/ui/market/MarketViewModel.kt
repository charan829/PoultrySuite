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

    fun createListing(type: String, quantity: String, price: String) {
        viewModelScope.launch {
            try {
                // Optimistic update or just reload
                val request = mapOf(
                    "type" to type,
                    "quantity" to quantity,
                    "pricePerUnit" to price
                )
                api.createListing(request)
                loadListings() // Refresh
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun placeOrder(productId: String) {
        viewModelScope.launch {
            try {
                val request = mapOf("productId" to productId)
                api.placeOrder(request)
                loadListings() // Refresh to remove sold item
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

sealed class MarketState {
    object Loading : MarketState()
    data class Success(val listings: List<ProductRequest>) : MarketState()
    data class Error(val message: String) : MarketState()
}
