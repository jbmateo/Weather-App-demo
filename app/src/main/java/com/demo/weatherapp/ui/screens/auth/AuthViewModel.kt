package com.demo.weatherapp.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.weatherapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val currentUserName: String? = null,
    val isSignup: Boolean = false,
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AuthUiState(
            isLoggedIn = authRepository.isLoggedIn(),
            currentUserName = authRepository.currentUserName()
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState

    fun onNameChange(value: String) = _uiState.update { it.copy(displayName = value, errorMessage = null) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }

    // toggle for Login or Signup
    fun setSignupMode(isSignup: Boolean) = _uiState.update {
        it.copy(isSignup = isSignup, errorMessage = null)
    }

    fun submit() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = if (state.isSignup) {
                authRepository.signUp(state.displayName, state.email, state.password)
            } else {
                authRepository.login(state.email, state.password)
            }
            _uiState.update {
                it.copy(
                    isLoggedIn = result.isSuccess,
                    currentUserName = if (result.isSuccess) authRepository.currentUserName() else it.currentUserName,
                    isLoading = false,
                    displayName = if (result.isSuccess) "" else it.displayName,
                    email = if (result.isSuccess) "" else it.email,
                    password = if (result.isSuccess) "" else it.password,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState()
    }
}
