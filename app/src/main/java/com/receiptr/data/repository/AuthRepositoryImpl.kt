package com.receiptr.data.repository

import android.app.Activity
import com.receiptr.data.remote.FirebaseAuthService
import com.receiptr.domain.model.AuthResult
import com.receiptr.domain.model.User
import com.receiptr.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthService: FirebaseAuthService
) : AuthRepository {
    
    override fun getCurrentUser(): Flow<User?> {
        return firebaseAuthService.getCurrentUser()
    }
    
    override suspend fun signInWithGoogle(idToken: String): AuthResult {
        return firebaseAuthService.signInWithGoogle(idToken)
    }
    
    override suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult {
        return firebaseAuthService.signInWithEmailAndPassword(email, password)
    }
    
    override suspend fun signUpWithEmailAndPassword(email: String, password: String): AuthResult {
        return firebaseAuthService.signUpWithEmailAndPassword(email, password)
    }
    
    override suspend fun signInWithPhoneNumber(phoneNumber: String, activity: Activity): AuthResult {
        return firebaseAuthService.signInWithPhoneNumber(phoneNumber, activity)
    }
    
    override suspend fun verifyPhoneNumber(verificationId: String, code: String): AuthResult {
        return firebaseAuthService.verifyPhoneNumber(verificationId, code)
    }
    
    override suspend fun resendVerificationCode(phoneNumber: String, activity: Activity): AuthResult {
        return firebaseAuthService.resendVerificationCode(phoneNumber, activity)
    }
    
    override suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return firebaseAuthService.sendPasswordResetEmail(email)
    }
    
    override suspend fun changePassword(currentPassword: String, newPassword: String): AuthResult {
        return firebaseAuthService.changePassword(currentPassword, newPassword)
    }
    
    override suspend fun signOut(): AuthResult {
        return firebaseAuthService.signOut()
    }
    
    override fun isUserAuthenticated(): Boolean {
        return firebaseAuthService.isUserAuthenticated()
    }
}
