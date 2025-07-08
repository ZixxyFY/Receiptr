package com.receiptr.domain.usecase

import com.receiptr.domain.model.AuthResult
import com.receiptr.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthResult {
        if (email.isBlank() || password.isBlank()) {
            return AuthResult.Error("Email and password cannot be empty")
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return AuthResult.Error("Please enter a valid email address")
        }
        
        if (password.length < 6) {
            return AuthResult.Error("Password must be at least 6 characters long")
        }
        
        if (!password.any { it.isDigit() }) {
            return AuthResult.Error("Password must contain at least one number")
        }
        
        return authRepository.signUpWithEmailAndPassword(email, password)
    }
}
