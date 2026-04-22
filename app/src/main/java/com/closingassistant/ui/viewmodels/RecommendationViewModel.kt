package com.closingassistant.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closingassistant.data.model.ClientProfile
import com.closingassistant.data.model.Recommendation
import com.closingassistant.data.repository.ClientRepository
import com.closingassistant.data.repository.ClientResult
import com.closingassistant.data.repository.ScriptRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class RecommendationUiState {
    object Loading : RecommendationUiState()
    data class Success(val recommendation: Recommendation) : RecommendationUiState()
    data class Saved(val clientId: String) : RecommendationUiState()
    data class Error(val message: String) : RecommendationUiState()
}

class RecommendationViewModel : ViewModel() {

    private val scriptRepository = ScriptRepository()
    private val clientRepository = ClientRepository()

    private val _uiState = MutableLiveData<RecommendationUiState>()
    val uiState: LiveData<RecommendationUiState> = _uiState

    fun generateRecommendation(profile: ClientProfile) {
        viewModelScope.launch {
            _uiState.value = RecommendationUiState.Loading
            // Small artificial delay for polish (feels intentional, not instant)
            delay(800)
            val recommendation = scriptRepository.generateRecommendation(profile)
            _uiState.value = RecommendationUiState.Success(recommendation)
        }
    }

    fun saveClientProfile(profile: ClientProfile) {
        viewModelScope.launch {
            when (val result = clientRepository.saveClient(profile)) {
                is ClientResult.Success -> _uiState.value = RecommendationUiState.Saved(result.clientId)
                is ClientResult.Error -> _uiState.value = RecommendationUiState.Error(result.message)
            }
        }
    }
}
