package com.simats.poultrysuite.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.poultrysuite.data.model.Review
import com.simats.poultrysuite.data.remote.PoultryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FarmerReviewsViewModel @Inject constructor(
    private val api: PoultryApi
) : ViewModel() {

    private val _state = MutableStateFlow<FarmerReviewsState>(FarmerReviewsState.Loading)
    val state = _state.asStateFlow()

    init {
        loadReviews()
    }

    fun loadReviews() {
        viewModelScope.launch {
            try {
                _state.value = FarmerReviewsState.Loading
                val reviews = api.getFarmerReviews()
                _state.value = FarmerReviewsState.Success(reviews)
            } catch (e: Exception) {
                _state.value = FarmerReviewsState.Error(e.message ?: "Failed to load reviews")
            }
        }
    }
}

sealed class FarmerReviewsState {
    object Loading : FarmerReviewsState()
    data class Success(val reviews: List<Review>) : FarmerReviewsState()
    data class Error(val message: String) : FarmerReviewsState()
}
