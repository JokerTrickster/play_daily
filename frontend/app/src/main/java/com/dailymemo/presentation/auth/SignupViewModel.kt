package com.dailymemo.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailymemo.domain.models.AuthResult
import com.dailymemo.domain.usecases.SignupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val signupUseCase: SignupUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Initial)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _authCode = MutableStateFlow("")
    val authCode: StateFlow<String> = _authCode.asStateFlow()

    fun onUsernameChange(newUsername: String) {
        _username.value = newUsername
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onAuthCodeChange(newAuthCode: String) {
        _authCode.value = newAuthCode
    }

    fun signup() {
        viewModelScope.launch {
            _state.value = AuthState.Loading

            val result = signupUseCase(
                username = _username.value.trim(),
                password = _password.value,
                authCode = _authCode.value.trim()
            )

            _state.value = when (result) {
                is AuthResult.Success -> AuthState.Success(result.user)
                is AuthResult.Error -> AuthState.Error(result.message)
            }
        }
    }

    fun resetState() {
        _state.value = AuthState.Initial
    }
}
