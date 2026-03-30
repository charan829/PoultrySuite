package com.simats.poultrysuite.ui.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.poultrysuite.data.local.SessionManager
import com.simats.poultrysuite.data.model.CanReviewResponse
import com.simats.poultrysuite.data.model.Order
import com.simats.poultrysuite.data.model.ProductRequest
import com.simats.poultrysuite.data.model.Review
import com.simats.poultrysuite.data.model.ReviewRequest
import com.simats.poultrysuite.data.remote.PoultryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
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

    private val _reviewsState = MutableStateFlow<ReviewsState>(ReviewsState.Idle)
    val reviewsState = _reviewsState.asStateFlow()

    private val _canReview = MutableStateFlow<CanReviewResponse?>(null)
    val canReview = _canReview.asStateFlow()

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
                loadListings()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to create listing")
            }
        }
    }

    fun placeOrder(
        productId: String,
        purchaseType: String = "ONLINE",
        quantity: Int? = null,
        deliveryAddress: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val request = buildMap {
                    put("productId", productId)
                    put("purchaseType", purchaseType)
                    if (quantity != null) {
                        put("quantity", quantity.toString())
                    }
                    if (!deliveryAddress.isNullOrBlank()) {
                        put("deliveryAddress", deliveryAddress)
                    }
                }
                api.placeOrder(request)
                loadListings()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to place order")
            }
        }
    }

    fun loadFarmReviews(farmId: String) {
        viewModelScope.launch {
            _reviewsState.value = ReviewsState.Loading
            try {
                val reviews = api.getFarmReviews(farmId)
                _reviewsState.value = ReviewsState.Success(reviews)
            } catch (e: Exception) {
                _reviewsState.value = ReviewsState.Error(e.message ?: "Failed to load reviews")
            }
        }
    }

    fun checkCanReview(farmId: String) {
        viewModelScope.launch {
            try {
                _canReview.value = api.canReviewFarm(farmId)
            } catch (_: Exception) {
                _canReview.value = null
            }
        }
    }

    fun submitReview(orderId: String, rating: Int, comment: String?, images: List<String>, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val request = ReviewRequest(orderId, rating, comment, images)
                val response = api.submitReview(request)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = parseErrorMessage(errorBody) ?: "Failed to submit review"
                    onError(message)
                }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to submit review")
            }
        }
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            JSONObject(errorBody).optString("error").takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
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
    data class Success(val orders: List<Order>) : OrdersState()
    data class Error(val message: String) : OrdersState()
}

sealed class ReviewsState {
    object Idle : ReviewsState()
    object Loading : ReviewsState()
    data class Success(val reviews: List<Review>) : ReviewsState()
    data class Error(val message: String) : ReviewsState()
}
