package com.receiptr.domain.usecase

import com.receiptr.domain.model.AuthResult
import com.receiptr.domain.model.User
import com.receiptr.domain.repository.AuthRepository
import com.receiptr.domain.repository.UserRepository
import javax.inject.Inject

class RegisterUserWithProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): AuthResult {
        // Validate input
        if (email.isBlank() || password.isBlank()) {
            return AuthResult.Error("Email and password cannot be empty")
        }
        
        if (firstName.isBlank() || lastName.isBlank()) {
            return AuthResult.Error("First name and last name cannot be empty")
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
        
        // Register user with Firebase Auth
        val authResult = authRepository.signUpWithEmailAndPassword(email, password)
        
        if (authResult is AuthResult.Success) {
            // Update user with additional profile information
            val user = authResult.user
            val updatedUser = user.copy(
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                displayName = "${firstName.trim()} ${lastName.trim()}",
                updatedAt = System.currentTimeMillis()
            )
            
            // Save updated user to Firestore
            val saveResult = userRepository.updateUser(updatedUser)
            
            if (saveResult.isSuccess) {
                return AuthResult.Success(saveResult.getOrNull() ?: updatedUser)
            } else {
                // If profile update fails, still return success but with original user
                return AuthResult.Success(user)
            }
        }
        
        return authResult
    }
}
