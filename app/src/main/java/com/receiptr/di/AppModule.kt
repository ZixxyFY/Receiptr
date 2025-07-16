package com.receiptr.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.receiptr.data.remote.FirebaseAuthService
import com.receiptr.data.remote.FirebaseFirestoreService
import com.receiptr.data.repository.AuthRepositoryImpl
import com.receiptr.data.repository.UserRepositoryImpl
import com.receiptr.data.repository.ReceiptRepositoryImpl
import com.receiptr.domain.repository.AuthRepository
import com.receiptr.domain.repository.UserRepository
import com.receiptr.domain.repository.ReceiptRepository
import com.receiptr.data.ml.TextRecognitionService
import com.receiptr.data.ml.ReceiptParserService
import com.receiptr.data.ml.enhanced.ReceiptCategorizationService
import com.receiptr.data.analytics.ReceiptAnalyticsService
import com.receiptr.data.sync.ReceiptSyncService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import android.content.Context
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
    
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideReceiptRepository(
        @ApplicationContext context: Context,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): ReceiptRepository {
        return ReceiptRepositoryImpl(context, firestore, storage)
    }
    
    @Provides
    @Singleton
    fun provideTextRecognitionService(@ApplicationContext context: Context): TextRecognitionService {
        return TextRecognitionService(context)
    }
    
    @Provides
    @Singleton
    fun provideReceiptParserService(): ReceiptParserService {
        return ReceiptParserService()
    }
    
    @Provides
    @Singleton
    fun provideReceiptAnalyticsService(
        receiptRepository: ReceiptRepository
    ): ReceiptAnalyticsService {
        return ReceiptAnalyticsService(receiptRepository)
    }
    
    @Provides
    @Singleton
    fun provideReceiptSyncService(
        receiptRepository: ReceiptRepository,
        analyticsService: ReceiptAnalyticsService
    ): ReceiptSyncService {
        return ReceiptSyncService(receiptRepository, analyticsService)
    }
}
