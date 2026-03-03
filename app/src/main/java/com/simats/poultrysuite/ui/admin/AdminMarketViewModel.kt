package com.simats.poultrysuite.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.poultrysuite.data.model.AdminSalesStats
import com.simats.poultrysuite.data.model.ProductRequest
import com.simats.poultrysuite.data.remote.PoultryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MarketState {
    object Loading : MarketState()
    data class Success(
        val salesStats: AdminSalesStats,
        val recentListings: List<ProductRequest>,
        val flaggedItems: List<com.simats.poultrysuite.data.model.FlaggedItem> = emptyList()
    ) : MarketState()
    data class Error(val message: String) : MarketState()
}

@HiltViewModel
class AdminMarketViewModel @Inject constructor(
    private val api: PoultryApi
) : ViewModel() {

    private val _marketState = MutableStateFlow<MarketState>(MarketState.Loading)
    val marketState: StateFlow<MarketState> = _marketState.asStateFlow()

    init {
        loadMarketData()
    }

    private fun loadMarketData() {
        viewModelScope.launch {
            _marketState.value = MarketState.Loading
            try {
                // Fetch data sequentially to avoid complex error handling for now
                val salesStats = api.getAdminSales()
                val listings = api.getListings()
                
                // Sort by ID descending as a proxy for recency since createdAt might not be available
                val recentListings = listings.sortedByDescending { it.id }.take(5)

                // Mock Flagged Items Logic (Client-side rule for demo)
                // In real app, this would come from backend analysis
                val flaggedItems = listings.filter { it.pricePerUnit > 5000 || it.quantity > 10000 }
                    .map { 
                        com.simats.poultrysuite.data.model.FlaggedItem(
                            id = it.id,
                            itemName = "${it.quantity} ${it.type}",
                            farmName = it.farm?.name ?: "Unknown Farm",
                            price = it.pricePerUnit,
                            reason = if (it.pricePerUnit > 5000) "Price deviation detected" else "Unusual volume",
                            severity = "Medium"
                        )
                    }
                    // If empty, add a dummy one for UI verification if needed, but let's stick to logic.
                    // To ensure UI shows up during verification, I'll add a hardcoded one if empty for now?
                    // No, let's respect the data. I'll rely on user testing or seed data.
                    // Actually, for "First Glance Wow", let's ensure at least one if strict logic fails, 
                    // or just leave it. I'll leave it dynamic.

                _marketState.value = MarketState.Success(
                    salesStats = salesStats,
                    recentListings = recentListings,
                    flaggedItems = flaggedItems
                )
            } catch (e: Exception) {
                _marketState.value = MarketState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
