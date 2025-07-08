package com.receiptr.data.repository

import com.receiptr.data.remote.FirebaseFirestoreService
import com.receiptr.domain.model.User
import com.receiptr.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestoreService: FirebaseFirestoreService
) : UserRepository {
    
    override suspend fun saveUser(user: User): Result<User> {
        return firestoreService.saveUser(user)
    }
    
    override suspend fun updateUser(user: User): Result<User> {
        return firestoreService.updateUser(user)
    }
    
    override suspend fun getUserById(userId: String): Result<User?> {
        return firestoreService.getUserById(userId)
    }
    
    override suspend fun getUserByEmail(email: String): Result<User?> {
        return firestoreService.getUserByEmail(email)
    }
    
    override suspend fun userExists(userId: String): Result<Boolean> {
        return firestoreService.userExists(userId)
    }
    
    override suspend fun deleteUser(userId: String): Result<Unit> {
        return firestoreService.deleteUser(userId)
    }
    
    override suspend fun updateUserPreferences(userId: String, preferences: Map<String, Any>): Result<Unit> {
        return firestoreService.updateUserPreferences(userId, preferences)
    }
}
