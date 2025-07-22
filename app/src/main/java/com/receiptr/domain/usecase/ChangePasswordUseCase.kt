package com.receiptr.domain.usecase

import com.receiptr.domain.model.AuthResult
import com.receiptr.domain.repository.AuthRepository
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(currentPassword: String, newPassword: String): AuthResult {
        return try {
            // Validate input
            if (currentPassword.isBlank()) {
                return AuthResult.Error("Current password cannot be empty")
            }
            
            if (newPassword.isBlank()) {
                return AuthResult.Error("New password cannot be empty")
            }
            
            if (newPassword.length < 6) {
                return AuthResult.Error("New password must be at least 6 characters long")
            }
            
            if (currentPassword == newPassword) {
                return AuthResult.Error("New password must be different from current password")
            }
            
            // Call repository to change password
            authRepository.changePassword(currentPassword, newPassword)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to change password")
        }
    }
}
