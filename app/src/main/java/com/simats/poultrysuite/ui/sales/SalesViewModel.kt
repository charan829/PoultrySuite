package com.simats.poultrysuite.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.poultrysuite.data.model.AdminSalesStats
import com.simats.poultrysuite.data.model.CustomerInfo
import com.simats.poultrysuite.data.model.ProductInfo
import com.simats.poultrysuite.data.model.SaleRecord
import com.simats.poultrysuite.data.model.TransactionDetails
import com.simats.poultrysuite.data.model.TransactionItem
import com.simats.poultrysuite.data.model.WeeklyRevenue
import com.simats.poultrysuite.data.remote.PoultryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Farmer Sales States ─────────────────────────────────────────────
sealed class SalesState {
    object Loading : SalesState()
    data class Success(val stats: AdminSalesStats) : SalesState()
    data class Error(val message: String) : SalesState()
}

// ── Farmer Sales (new simple list) ─────────────────────────────────
sealed class FarmerSalesState {
    object Loading : FarmerSalesState()
    data class Success(val sales: List<SaleRecord>) : FarmerSalesState()
    data class Error(val message: String) : FarmerSalesState()
}

// ── Transaction Detail State ─────────────────────────────────────────
sealed class TransactionState {
    object Loading : TransactionState()
    data class Success(val details: TransactionDetails) : TransactionState()
    data class Error(val message: String) : TransactionState()
}

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val api: PoultryApi
) : ViewModel() {

    // Admin / TotalSalesScreen state
    private val _salesState = MutableStateFlow<SalesState>(SalesState.Loading)
    val salesState = _salesState.asStateFlow()

    // Farmer sales list state (used by new FarmerSalesScreen)
    private val _state = MutableStateFlow<FarmerSalesState>(FarmerSalesState.Loading)
    val state = _state.asStateFlow()

    // Transaction detail state
    private val _transactionState = MutableStateFlow<TransactionState>(TransactionState.Loading)
    val transactionState = _transactionState.asStateFlow()

    init { loadSales() }

    fun loadSales() {
        viewModelScope.launch {
            try {
                _state.value = FarmerSalesState.Loading
                val sales = api.getSales()
                _state.value = FarmerSalesState.Success(sales)
            } catch (e: Exception) {
                _state.value = FarmerSalesState.Error(e.message ?: "Failed to load sales")
            }
        }
    }

    // Used by TotalSalesScreen (admin view)
    fun loadSalesData() {
        viewModelScope.launch {
            try {
                _salesState.value = SalesState.Loading
                val sales = api.getSales()
                val totalToday = sales.filter {
                    it.createdAt.startsWith(java.time.LocalDate.now().toString())
                }.sumOf { it.totalPrice }

                val recentTransactions = sales.take(10).map { s ->
                    TransactionItem(
                        id = s.id,
                        customerName = s.buyerName.ifBlank { "Walk-in Customer" },
                        farmName = "",
                        items = "${s.quantity} ${s.productType.lowercase()}",
                        amount = s.totalPrice,
                        status = s.paymentStatus,
                        date = s.createdAt
                    )
                }

                // Build last 7 days weekly revenue
                val weekDays = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")
                val weekly = weekDays.map { day ->
                    WeeklyRevenue(day = day, revenue = sales.filter {
                        it.createdAt.contains(day, ignoreCase = true)
                    }.sumOf { it.totalPrice })
                }

                _salesState.value = SalesState.Success(
                    AdminSalesStats(
                        todayRevenue = totalToday,
                        todayOrders = sales.count {
                            it.createdAt.startsWith(java.time.LocalDate.now().toString())
                        },
                        weeklyRevenue = weekly,
                        recentTransactions = recentTransactions
                    )
                )
            } catch (e: Exception) {
                _salesState.value = SalesState.Error(e.message ?: "Failed to load sales data")
            }
        }
    }

    // Used by TransactionDetailsScreen
    fun loadTransactionDetails(transactionId: String) {
        viewModelScope.launch {
            try {
                _transactionState.value = TransactionState.Loading
                val sales = api.getSales()
                val sale = sales.firstOrNull { it.id == transactionId }
                if (sale != null) {
                    _transactionState.value = TransactionState.Success(
                        TransactionDetails(
                            id = sale.id,
                            amount = sale.totalPrice,
                            status = sale.paymentStatus,
                            date = sale.createdAt,
                            customer = CustomerInfo(
                                name = sale.buyerName.ifBlank { "Walk-in Customer" },
                                email = "—",
                                phone = null
                            ),
                            product = ProductInfo(
                                name = "${sale.productType.lowercase()}s",
                                quantity = sale.quantity,
                                pricePerUnit = sale.pricePerUnit,
                                farm = "—",
                                location = null
                            )
                        )
                    )
                } else {
                    _transactionState.value = TransactionState.Error("Transaction not found")
                }
            } catch (e: Exception) {
                _transactionState.value = TransactionState.Error(e.message ?: "Failed to load details")
            }
        }
    }
}
