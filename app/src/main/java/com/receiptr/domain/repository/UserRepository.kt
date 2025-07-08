package com.receiptr.domain.repository

import com.receiptr.domain.model.User

interface UserRepository {
    suspend fun saveUser(user: User): Result<User>
    suspend fun updateUser(user: User): Result<User>
    suspend fun getUserById(userId: String): Result<User?>
    suspend fun getUserByEmail(email: String): Result<User?>
    suspend fun userExists(userId: String): Result<Boolean>
    suspend fun deleteUser(userId: String): Result<Unit>
    suspend fun updateUserPreferences(userId: String, preferences: Map<String, Any>): Result<Unit>
}
