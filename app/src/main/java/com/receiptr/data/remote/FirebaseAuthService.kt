package com.receiptr.data.remote

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.receiptr.domain.model.AuthResult
import com.receiptr.domain.model.User
import com.receiptr.domain.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class FirebaseAuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository
) {
    
    // Store verification ID for phone authentication
    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    
    // Expose storedVerificationId for use in other layers
    val currentVerificationId: String? get() = storedVerificationId
    
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
    
    suspend fun signInWithPhoneNumber(phoneNumber: String, activity: Activity): AuthResult {
        return suspendCancellableCoroutine { continuation ->
            var isCompleted = false
            
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // This callback will be invoked in two situations:
                    // 1 - Instant verification. In some cases the phone number can be instantly
                    //     verified without needing to send or enter a verification code.
                    // 2 - Auto-retrieval. On some devices Google Play services can automatically
                    //     detect the incoming verification SMS and perform verification without
                    //     user action.
                    if (!isCompleted) {
                        isCompleted = true
                        // Attempt to sign in with the credential immediately
                        firebaseAuth.signInWithCredential(credential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val user = task.result?.user?.toUser() ?: User()
                                    continuation.resume(AuthResult.Success(user))
                                } else {
                                    continuation.resume(AuthResult.Error(task.exception?.message ?: "Auto-verification failed"))
                                }
                            }
                    }
                }
                
                override fun onVerificationFailed(e: FirebaseException) {
                    if (!isCompleted) {
                        isCompleted = true
                        val errorMessage = when (e) {
                            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                                "Invalid phone number format. Please check and try again."
                            }
                            is com.google.firebase.FirebaseTooManyRequestsException -> {
                                "Too many requests. Please try again later."
                            }
                            is com.google.firebase.FirebaseNetworkException -> {
                                "Network error. Please check your connection and try again."
                            }
                            else -> e.message ?: "Phone verification failed. Please try again."
                        }
                        continuation.resume(AuthResult.Error(errorMessage))
                    }
                }
                
                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    // The SMS verification code has been sent to the provided phone number
                    if (!isCompleted) {
                        isCompleted = true
                        storedVerificationId = verificationId
                        resendToken = token
                        // Return a special success result to indicate OTP was sent
                        continuation.resume(AuthResult.Success(User(id = "otp_sent")))
                    }
                }
            }
            
            // Validate phone number format before making the request
            if (!isValidPhoneNumber(phoneNumber)) {
                continuation.resume(AuthResult.Error("Please enter a valid phone number with country code (e.g., +1234567890)"))
                return@suspendCancellableCoroutine
            }
            
            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(120L, TimeUnit.SECONDS) // Increased timeout to 2 minutes
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
            
            PhoneAuthProvider.verifyPhoneNumber(options)
            
            // Set up cancellation handling
            continuation.invokeOnCancellation {
                // Clean up if needed
            }
        }
    }
    
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Basic validation - should start with + and contain only digits and +
        return phoneNumber.matches(Regex("^\\+[1-9]\\d{1,14}$"))
    }
    
    suspend fun verifyPhoneNumber(verificationId: String, code: String): AuthResult {
        return try {
            // Use stored verification ID if provided verificationId is empty or null
            val actualVerificationId = if (verificationId.isNotBlank()) verificationId else storedVerificationId
            
            if (actualVerificationId.isNullOrBlank()) {
                return AuthResult.Error("Verification session expired. Please request a new code.")
            }
            
            // Validate the code format
            if (code.isBlank()) {
                return AuthResult.Error("Please enter the verification code")
            }
            
            if (code.length != 6) {
                return AuthResult.Error("Verification code must be 6 digits")
            }
            
            if (!code.all { it.isDigit() }) {
                return AuthResult.Error("Verification code must contain only numbers")
            }
            
            val credential = PhoneAuthProvider.getCredential(actualVerificationId, code)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user
            
            if (firebaseUser != null) {
                val user = firebaseUser.toUser()
                
                // Clear stored verification ID after successful verification
                storedVerificationId = null
                resendToken = null
                
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
                AuthResult.Error("Authentication failed. Please try again.")
            }
        } catch (e: Exception) {
            // Handle specific Firebase Auth exceptions
            val errorMessage = when (e) {
                is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                    if (e.message?.contains("invalid verification code", ignoreCase = true) == true) {
                        "Invalid verification code. Please check and try again."
                    } else {
                        "Invalid credentials. Please try again."
                    }
                }
                is com.google.firebase.auth.FirebaseAuthException -> {
                    when (e.errorCode) {
                        "ERROR_SESSION_EXPIRED" -> "Verification session expired. Please request a new code."
                        "ERROR_INVALID_VERIFICATION_CODE" -> "Invalid verification code. Please try again."
                        else -> "Verification failed: ${e.message}"
                    }
                }
                is com.google.firebase.FirebaseNetworkException -> {
                    "Network error. Please check your connection and try again."
                }
                else -> e.message ?: "Phone verification failed. Please try again."
            }
            AuthResult.Error(errorMessage)
        }
    }
    
    // Add method to resend OTP
    suspend fun resendVerificationCode(phoneNumber: String, activity: Activity): AuthResult {
        return if (resendToken != null) {
            suspendCancellableCoroutine { continuation ->
                var isCompleted = false
                
                val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        if (!isCompleted) {
                            isCompleted = true
                            continuation.resume(AuthResult.Success(User(id = "auto_verified")))
                        }
                    }
                    
                    override fun onVerificationFailed(e: FirebaseException) {
                        if (!isCompleted) {
                            isCompleted = true
                            continuation.resume(AuthResult.Error(e.message ?: "Resend failed"))
                        }
                    }
                    
                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        if (!isCompleted) {
                            isCompleted = true
                            storedVerificationId = verificationId
                            resendToken = token
                            continuation.resume(AuthResult.Success(User(id = "otp_resent")))
                        }
                    }
                }
                
                val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(120L, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(callbacks)
                    .setForceResendingToken(resendToken!!)
                    .build()
                
                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        } else {
            AuthResult.Error("Cannot resend at this time. Please try requesting a new code.")
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
