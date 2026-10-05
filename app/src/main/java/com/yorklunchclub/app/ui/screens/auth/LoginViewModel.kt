package com.yorklunchclub.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yorklunchclub.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val signedInUid: String? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmailChange(email: String) = _state.update { it.copy(email = email, error = null) }
    fun onPasswordChange(password: String) = _state.update { it.copy(password = password, error = null) }

    fun login() {
        val current = _state.value
        if (current.email.isBlank() || current.password.isBlank()) {
            _state.update { it.copy(error = "Email and password are required.") }
            return
        }
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val result = authRepository.login(current.email.trim(), current.password)
            result.fold(
                onSuccess = { uid ->
                    _state.update { it.copy(loading = false, signedInUid = uid) }
                },
                onFailure = { e ->
                    val detail = e.message ?: e::class.java.simpleName
                    _state.update { it.copy(loading = false, error = "Login failed: $detail") }
                }
            )
        }
    }

    fun onSignedInHandled() = _state.update { it.copy(signedInUid = null) }

    fun loginWithStoredCredentials(email: String, password: String) {
        _state.update { it.copy(loading = true, error = null, email = email, password = password) }
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = { uid -> _state.update { it.copy(loading = false, signedInUid = uid) } },
                onFailure = { e -> _state.update { it.copy(loading = false, error = e.message ?: "Login failed.") } }
            )
        }
    }
}
