package com.closingassistant.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closingassistant.data.repository.AuthRepository
import com.closingassistant.data.repository.AuthResult
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: FirebaseUser) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _uiState = MutableLiveData<LoginUiState>(LoginUiState.Idle)
    val uiState: LiveData<LoginUiState> = _uiState

    fun login(email: String, password: String) {
        if (!validateInputs(email, password)) return

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> _uiState.value = LoginUiState.Success(result.user)
                is AuthResult.Error -> _uiState.value = LoginUiState.Error(result.message)
            }
        }
    }

    fun register(email: String, password: String) {
        if (!validateInputs(email, password)) return

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            when (val result = authRepository.register(email, password)) {
                is AuthResult.Success -> _uiState.value = LoginUiState.Success(result.user)
                is AuthResult.Error -> _uiState.value = LoginUiState.Error(result.message)
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isBlank()) {
            _uiState.value = LoginUiState.Error("Please enter your email address.")
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = LoginUiState.Error("Please enter a valid email address.")
            return false
        }
        if (password.isBlank()) {
            _uiState.value = LoginUiState.Error("Please enter your password.")
            return false
        }
        if (password.length < 6) {
            _uiState.value = LoginUiState.Error("Password must be at least 6 characters.")
            return false
        }
        return true
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}
