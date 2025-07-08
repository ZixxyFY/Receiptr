package com.receiptr.domain.repository

import com.receiptr.domain.model.AuthResult
import com.receiptr.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun signInWithGoogle(idToken: String): AuthResult
    suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult
    suspend fun signUpWithEmailAndPassword(email: String, password: String): AuthResult
    suspend fun signInWithPhoneNumber(phoneNumber: String): AuthResult
    suspend fun verifyPhoneNumber(verificationId: String, code: String): AuthResult
    suspend fun sendPasswordResetEmail(email: String): AuthResult
    suspend fun signOut(): AuthResult
    fun isUserAuthenticated(): Boolean
}
