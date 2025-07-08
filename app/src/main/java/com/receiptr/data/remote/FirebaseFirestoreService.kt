package com.receiptr.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.receiptr.domain.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    companion object {
        private const val USERS_COLLECTION = "users"
    }
    
    /**
     * Save a new user to Firestore
     */
    suspend fun saveUser(user: User): Result<User> {
        return try {
            val userWithTimestamp = user.copy(
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            firestore.collection(USERS_COLLECTION)
                .document(user.id)
                .set(userWithTimestamp.toMap())
                .await()
            
            Result.success(userWithTimestamp)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update an existing user in Firestore
     */
    suspend fun updateUser(user: User): Result<User> {
        return try {
            val userWithTimestamp = user.copy(updatedAt = System.currentTimeMillis())
            
            firestore.collection(USERS_COLLECTION)
                .document(user.id)
                .update(userWithTimestamp.toMap())
                .await()
            
            Result.success(userWithTimestamp)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get a user by ID from Firestore
     */
    suspend fun getUserById(userId: String): Result<User?> {
        return try {
            val document = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            if (document.exists()) {
                val userData = document.data ?: emptyMap()
                val user = User.fromMap(userData)
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get a user by email from Firestore
     */
    suspend fun getUserByEmail(email: String): Result<User?> {
        return try {
            val querySnapshot = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()
            
            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents[0]
                val userData = document.data ?: emptyMap()
                val user = User.fromMap(userData)
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if a user exists in Firestore
     */
    suspend fun userExists(userId: String): Result<Boolean> {
        return try {
            val document = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            Result.success(document.exists())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete a user from Firestore
     */
    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .delete()
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update user preferences
     */
    suspend fun updateUserPreferences(userId: String, preferences: Map<String, Any>): Result<Unit> {
        return try {
            val updates = mapOf(
                "preferences" to preferences,
                "updatedAt" to System.currentTimeMillis()
            )
            
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .update(updates)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
