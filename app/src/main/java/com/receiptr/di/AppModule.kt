package com.receiptr.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.receiptr.data.remote.FirebaseAuthService
import com.receiptr.data.remote.FirebaseFirestoreService
import com.receiptr.data.repository.AuthRepositoryImpl
import com.receiptr.data.repository.UserRepositoryImpl
import com.receiptr.domain.repository.AuthRepository
import com.receiptr.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseFirestoreService(firestore: FirebaseFirestore): FirebaseFirestoreService {
        return FirebaseFirestoreService(firestore)
    }
    
    @Provides
    @Singleton
    fun provideUserRepository(firestoreService: FirebaseFirestoreService): UserRepository {
        return UserRepositoryImpl(firestoreService)
    }
    
    @Provides
    @Singleton
    fun provideFirebaseAuthService(
        firebaseAuth: FirebaseAuth,
        userRepository: UserRepository
    ): FirebaseAuthService {
        return FirebaseAuthService(firebaseAuth, userRepository)
    }
    
    @Provides
    @Singleton
    fun provideAuthRepository(firebaseAuthService: FirebaseAuthService): AuthRepository {
        return AuthRepositoryImpl(firebaseAuthService)
    }
}
