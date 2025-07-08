package com.receiptr.domain.usecase

import com.receiptr.domain.model.User
import com.receiptr.domain.repository.UserRepository
import javax.inject.Inject

class SaveUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Result<User> {
        if (user.id.isBlank()) {
            return Result.failure(IllegalArgumentException("User ID cannot be empty"))
        }
        
        return userRepository.saveUser(user)
    }
}

class UpdateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Result<User> {
        if (user.id.isBlank()) {
            return Result.failure(IllegalArgumentException("User ID cannot be empty"))
        }
        
        return userRepository.updateUser(user)
    }
}

class GetUserByIdUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<User?> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("User ID cannot be empty"))
        }
        
        return userRepository.getUserById(userId)
    }
}
