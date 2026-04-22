package com.closingassistant.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closingassistant.data.model.ClientProfile
import com.closingassistant.data.repository.ClientRepository
import com.closingassistant.data.repository.ClientResult
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val clientId: String, val profile: ClientProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ClientProfileViewModel : ViewModel() {

    private val clientRepository = ClientRepository()

    private val _uiState = MutableLiveData<ProfileUiState>(ProfileUiState.Idle)
    val uiState: LiveData<ProfileUiState> = _uiState

    // Holds the validated profile temporarily before navigation
    private val _currentProfile = MutableLiveData<ClientProfile>()
    val currentProfile: LiveData<ClientProfile> = _currentProfile

    fun validateAndProceed(
        ageStr: String,
        incomeStr: String,
        dependentsStr: String,
        financialGoals: String,
        concerns: String
    ) {
        // --- Validation ---
        if (ageStr.isBlank()) {
            _uiState.value = ProfileUiState.Error("Age is required.")
            return
        }
        val age = ageStr.toIntOrNull()
        if (age == null || age < 18 || age > 75) {
            _uiState.value = ProfileUiState.Error("Please enter a valid age between 18 and 75.")
            return
        }

        if (incomeStr.isBlank()) {
            _uiState.value = ProfileUiState.Error("Monthly income is required.")
            return
        }
        val income = incomeStr.replace(",", "").toDoubleOrNull()
        if (income == null || income <= 0) {
            _uiState.value = ProfileUiState.Error("Please enter a valid monthly income.")
            return
        }

        if (dependentsStr.isBlank()) {
            _uiState.value = ProfileUiState.Error("Number of dependents is required.")
            return
        }
        val dependents = dependentsStr.toIntOrNull()
        if (dependents == null || dependents < 0 || dependents > 20) {
            _uiState.value = ProfileUiState.Error("Please enter a valid number of dependents (0–20).")
            return
        }

        if (financialGoals.isBlank()) {
            _uiState.value = ProfileUiState.Error("Please describe your financial goals.")
            return
        }

        if (concerns.isBlank()) {
            _uiState.value = ProfileUiState.Error("Please describe any concerns or objections.")
            return
        }

        val profile = ClientProfile(
            age = age,
            monthlyIncome = income,
            numberOfDependents = dependents,
            financialGoals = financialGoals.trim(),
            concerns = concerns.trim()
        )

        _currentProfile.value = profile
        _uiState.value = ProfileUiState.Success("draft", profile)
    }

    fun saveClientToFirestore(profile: ClientProfile) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            when (val result = clientRepository.saveClient(profile)) {
                is ClientResult.Success -> {
                    _uiState.value = ProfileUiState.Success(result.clientId, profile)
                }
                is ClientResult.Error -> {
                    _uiState.value = ProfileUiState.Error(result.message)
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = ProfileUiState.Idle
    }
}
