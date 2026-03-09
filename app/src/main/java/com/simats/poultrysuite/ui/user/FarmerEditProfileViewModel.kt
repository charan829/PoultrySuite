package com.simats.poultrysuite.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.poultrysuite.data.model.FarmerProfileUpdateRequest
import com.simats.poultrysuite.data.remote.PoultryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FarmerEditProfileViewModel @Inject constructor(
    private val api: PoultryApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState = _updateState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                _uiState.value = EditProfileUiState.Loading
                val profile = api.getFarmerProfile()
                _uiState.value = EditProfileUiState.Success(profile)
            } catch (e: Exception) {
                _uiState.value = EditProfileUiState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

    fun updateProfile(fullName: String, phone: String, farmName: String, location: String) {
        viewModelScope.launch {
            try {
                _updateState.value = UpdateState.Saving
                val request = FarmerProfileUpdateRequest(fullName, phone, farmName, location)
                val response = api.updateFarmerProfile(request)
                if (response.isSuccessful) {
                    _updateState.value = UpdateState.Success
                } else {
                    _updateState.value = UpdateState.Error("Failed to update profile")
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.message ?: "An error occurred")
            }
        }
    }
}

sealed class EditProfileUiState {
    object Loading : EditProfileUiState()
    data class Success(val profile: com.simats.poultrysuite.data.model.FarmerProfile) : EditProfileUiState()
    data class Error(val message: String) : EditProfileUiState()
}

sealed class UpdateState {
    object Idle : UpdateState()
    object Saving : UpdateState()
    object Success : UpdateState()
    data class Error(val message: String) : UpdateState()
}
