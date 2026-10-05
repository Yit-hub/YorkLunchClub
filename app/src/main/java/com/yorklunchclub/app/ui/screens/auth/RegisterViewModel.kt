package com.yorklunchclub.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yorklunchclub.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val registeredUid: String? = null
)

class RegisterViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun onFullNameChange(v: String) = _state.update { it.copy(fullName = v, error = null) }
    fun onUsernameChange(v: String) = _state.update { it.copy(username = v, error = null) }
    fun onEmailChange(v: String) = _state.update { it.copy(email = v, error = null) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, error = null) }
    fun onConfirmPasswordChange(v: String) = _state.update { it.copy(confirmPassword = v, error = null) }

    fun register() {
        val s = _state.value
        when {
            s.fullName.isBlank() -> { _state.update { it.copy(error = "Full name is required.") }; return }
            s.username.isBlank() -> { _state.update { it.copy(error = "Username is required.") }; return }
            s.email.isBlank() -> { _state.update { it.copy(error = "Email is required.") }; return }
            s.password.length < 6 -> { _state.update { it.copy(error = "Password must be 6+ characters.") }; return }
            s.password != s.confirmPassword -> { _state.update { it.copy(error = "Passwords do not match.") }; return }
        }
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val result = authRepository.register(s.email.trim(), s.password, s.fullName.trim(), s.username.trim())
            result.fold(
                onSuccess = { uid -> _state.update { it.copy(loading = false, registeredUid = uid) } },
                onFailure = { e ->
                    val detail = e.message ?: e::class.java.simpleName
                    _state.update { it.copy(loading = false, error = "Registration failed: $detail") }
                }
            )
        }
    }

    fun onRegisteredHandled() = _state.update { it.copy(registeredUid = null) }
}
