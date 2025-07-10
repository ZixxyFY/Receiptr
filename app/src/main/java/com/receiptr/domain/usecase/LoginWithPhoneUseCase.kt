package com.receiptr.domain.usecase

import android.app.Activity
import com.receiptr.domain.model.AuthResult
import com.receiptr.domain.repository.AuthRepository
import javax.inject.Inject

class LoginWithPhoneUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phoneNumber: String, activity: Activity): AuthResult {
        if (phoneNumber.isBlank()) {
            return AuthResult.Error("Phone number cannot be empty")
        }
        
        return authRepository.signInWithPhoneNumber(phoneNumber, activity)
    }
    
    suspend fun verifyOtp(verificationId: String, code: String): AuthResult {
        if (verificationId.isBlank() || code.isBlank()) {
            return AuthResult.Error("Verification ID and code cannot be empty")
        }
        
        return authRepository.verifyPhoneNumber(verificationId, code)
    }
}
