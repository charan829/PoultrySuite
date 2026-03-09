package com.simats.poultrysuite.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.poultrysuite.data.model.SaleRequest
import com.simats.poultrysuite.data.remote.PoultryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddSaleViewModel @Inject constructor(
    private val api: PoultryApi
) : ViewModel() {

    private val _saleState = MutableStateFlow<SaleState>(SaleState.Idle)
    val saleState = _saleState.asStateFlow()

    fun submitSale(
        productType: String,
        quantity: Int,
        pricePerUnit: Double,
        buyerName: String,
        notes: String
    ) {
        viewModelScope.launch {
            try {
                _saleState.value = SaleState.Saving
                val request = SaleRequest(
                    productType = productType,
                    quantity = quantity,
                    pricePerUnit = pricePerUnit,
                    buyerName = buyerName.ifBlank { "Walk-in Customer" },
                    notes = notes
                )
                val response = api.addSale(request)
                if (response.isSuccessful) {
                    _saleState.value = SaleState.Success
                } else {
                    _saleState.value = SaleState.Error("Failed to record sale (${response.code()})")
                }
            } catch (e: Exception) {
                _saleState.value = SaleState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun resetState() {
        _saleState.value = SaleState.Idle
    }
}

sealed class SaleState {
    object Idle : SaleState()
    object Saving : SaleState()
    object Success : SaleState()
    data class Error(val message: String) : SaleState()
}
