package com.receiptr.data.remote

import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.receiptr.domain.model.AuthResult
import com.receiptr.domain.model.User
import com.receiptr.domain.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository
) {
    
    fun getCurrentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }
    
    suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user
            
            if (firebaseUser != null) {
                val user = firebaseUser.toUser()
                
                // Check if user exists in Firestore
                val existsResult = userRepository.userExists(user.id)
                
                if (existsResult.isSuccess && existsResult.getOrNull() == false) {
                    // New user - save to Firestore
                    val firestoreResult = userRepository.saveUser(user)
                    
                    if (firestoreResult.isSuccess) {
                        AuthResult.Success(firestoreResult.getOrNull() ?: user)
                    } else {
                        // If Firestore save fails, still return success
                        AuthResult.Success(user)
                    }
                } else {
                    // Existing user - just return the user data
                    AuthResult.Success(user)
                }
            } else {
                AuthResult.Error("Google authentication failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Google sign-in failed")
        }
    }
    
    suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success(result.user?.toUser() ?: User())
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign-in failed")
        }
    }
    
    suspend fun signUpWithEmailAndPassword(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            
            if (firebaseUser != null) {
                val user = firebaseUser.toUser()
                
                // Save user to Firestore
                val firestoreResult = userRepository.saveUser(user)
                
                if (firestoreResult.isSuccess) {
                    AuthResult.Success(firestoreResult.getOrNull() ?: user)
                } else {
                    // If Firestore save fails, still return success but log the error
                    // User is authenticated but not saved to Firestore
                    AuthResult.Success(user)
                }
            } else {
                AuthResult.Error("Authentication failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign-up failed")
        }
    }
    
    suspend fun signInWithPhoneNumber(phoneNumber: String): AuthResult {
        return try {
            // This is a simplified implementation
            // In a real app, you would use PhoneAuthProvider.verifyPhoneNumber
            // with proper activity and callbacks
            AuthResult.Error("Phone authentication requires activity context")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Phone sign-in failed")
        }
    }
    
    suspend fun verifyPhoneNumber(verificationId: String, code: String): AuthResult {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user
            
            if (firebaseUser != null) {
                val user = firebaseUser.toUser()
                
                // Check if user exists in Firestore
                val existsResult = userRepository.userExists(user.id)
                
                if (existsResult.isSuccess && existsResult.getOrNull() == false) {
                    // New user - save to Firestore
                    val firestoreResult = userRepository.saveUser(user)
                    
                    if (firestoreResult.isSuccess) {
                        AuthResult.Success(firestoreResult.getOrNull() ?: user)
                    } else {
                        // If Firestore save fails, still return success
                        AuthResult.Success(user)
                    }
                } else {
                    // Existing user - just return the user data
                    AuthResult.Success(user)
                }
            } else {
                AuthResult.Error("Phone verification failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Phone verification failed")
        }
    }
    
    suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            AuthResult.Success(User())
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Password reset failed")
        }
    }
    
    suspend fun signOut(): AuthResult {
        return try {
            firebaseAuth.signOut()
            AuthResult.Success(User())
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign-out failed")
        }
    }
    
    fun isUserAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }
    
    private fun FirebaseUser.toUser(): User {
        return User(
            id = uid,
            email = email,
            phoneNumber = phoneNumber,
            displayName = displayName,
            photoUrl = photoUrl?.toString(),
            isEmailVerified = isEmailVerified
        )
    }
}
