package com.receiptr.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptr.domain.model.AuthResult
import com.receiptr.domain.model.AuthState
import com.receiptr.domain.model.User
import com.receiptr.domain.repository.AuthRepository
import com.receiptr.domain.usecase.LoginWithEmailUseCase
import com.receiptr.domain.usecase.LoginWithGoogleUseCase
import com.receiptr.domain.usecase.LoginWithPhoneUseCase
import com.receiptr.domain.usecase.RegisterUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val loginWithEmailUseCase: LoginWithEmailUseCase,
    private val loginWithPhoneUseCase: LoginWithPhoneUseCase,
    private val registerUserUseCase: RegisterUserUseCase
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _authResult = MutableStateFlow<AuthResult?>(null)
    val authResult: StateFlow<AuthResult?> = _authResult.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId: StateFlow<String?> = _verificationId.asStateFlow()
    
    init {
        checkAuthState()
        observeCurrentUser()
    }
    
    private fun checkAuthState() {
        _authState.value = if (authRepository.isUserAuthenticated()) {
            AuthState.Authenticated
        } else {
            AuthState.Unauthenticated
        }
    }
    
    private fun observeCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                _currentUser.value = user
                _authState.value = if (user != null) {
                    AuthState.Authenticated
                } else {
                    AuthState.Unauthenticated
                }
            }
        }
    }
    
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authResult.value = AuthResult.Loading
            _authResult.value = loginWithGoogleUseCase(idToken)
        }
    }
    
    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authResult.value = AuthResult.Loading
            _authResult.value = loginWithEmailUseCase(email, password)
        }
    }
    
    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authResult.value = AuthResult.Loading
            _authResult.value = registerUserUseCase(email, password)
        }
    }
    
    fun signInWithPhone(phoneNumber: String) {
        viewModelScope.launch {
            _authResult.value = AuthResult.Loading
            val result = loginWithPhoneUseCase(phoneNumber)
            _authResult.value = result
            
            // In a real implementation, you would get the verification ID from the result
            // For now, we'll simulate it
            if (result is AuthResult.Success) {
                _verificationId.value = "dummy_verification_id"
            }
        }
    }
    
    fun verifyOtp(code: String) {
        val verificationId = _verificationId.value
        if (verificationId != null) {
            viewModelScope.launch {
                _authResult.value = AuthResult.Loading
                _authResult.value = loginWithPhoneUseCase.verifyOtp(verificationId, code)
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _authState.value = AuthState.Unauthenticated
        }
    }
    
    fun clearAuthResult() {
        _authResult.value = null
    }
}
