package com.simats.poultrysuite.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.poultrysuite.data.model.FarmerProfile
import com.simats.poultrysuite.data.remote.PoultryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FarmerAccountViewModel @Inject constructor(
    private val api: PoultryApi
) : ViewModel() {

    private val _profileState = MutableStateFlow<FarmerProfileState>(FarmerProfileState.Loading)
    val profileState = _profileState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            try {
                _profileState.value = FarmerProfileState.Loading
                val profile = api.getFarmerProfile()
                _profileState.value = FarmerProfileState.Success(profile)
            } catch (e: Exception) {
                _profileState.value = FarmerProfileState.Error(e.message ?: "Failed to load profile")
            }
        }
    }
}

sealed class FarmerProfileState {
    object Loading : FarmerProfileState()
    data class Success(val profile: FarmerProfile) : FarmerProfileState()
    data class Error(val message: String) : FarmerProfileState()
}
