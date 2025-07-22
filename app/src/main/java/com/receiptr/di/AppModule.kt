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
import com.receiptr.data.ml.preprocessing.ImagePreprocessingService
import com.receiptr.data.ml.annotation.AnnotationService
import com.receiptr.data.ml.annotation.AnnotationRepository
import com.receiptr.data.ml.annotation.AnnotationRepositoryImpl
import com.receiptr.data.ml.annotation.AnnotationDao
import com.receiptr.data.ml.annotation.AnnotationDatabase
import com.receiptr.data.local.ReceiptDatabase
import com.receiptr.data.local.ReceiptDao
import com.receiptr.data.ml.cloud.CloudVisionService
import com.receiptr.data.analytics.ReceiptAnalyticsService
import com.receiptr.data.sync.ReceiptSyncService
import com.receiptr.domain.usecase.GenerateReceiptPdfUseCase
import com.receiptr.data.notification.NotificationService
import com.receiptr.data.notification.NotificationManager
import com.receiptr.data.email.EmailService
import com.receiptr.data.email.EmailAuthService
import com.receiptr.data.email.EmailReceiptParser
import com.receiptr.data.email.EmailReceiptRepository
import com.receiptr.data.cache.CacheDao
import androidx.room.Room
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
    fun provideReceiptDatabase(@ApplicationContext context: Context): ReceiptDatabase {
        return Room.databaseBuilder(
            context,
            ReceiptDatabase::class.java,
            "receipt_database"
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideReceiptDao(database: ReceiptDatabase): ReceiptDao {
        return database.receiptDao()
    }
    
    @Provides
    @Singleton
    fun provideReceiptRepository(
        @ApplicationContext context: Context,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
        receiptDao: ReceiptDao
    ): ReceiptRepository {
        return ReceiptRepositoryImpl(context, firestore, storage, receiptDao)
    }
    
    @Provides
    @Singleton
    fun provideTextRecognitionService(
        @ApplicationContext context: Context,
        preprocessService: ImagePreprocessingService
    ): TextRecognitionService {
        return TextRecognitionService(context, preprocessService)
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
    
    @Provides
    @Singleton
    fun provideAnnotationDatabase(@ApplicationContext context: Context): AnnotationDatabase {
        return Room.databaseBuilder(
            context,
            AnnotationDatabase::class.java,
            "annotation_database"
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideAnnotationDao(database: AnnotationDatabase): AnnotationDao {
        return database.annotationDao()
    }
    
    @Provides
    @Singleton
    fun provideAnnotationRepository(annotationDao: AnnotationDao): AnnotationRepository {
        return AnnotationRepositoryImpl(annotationDao)
    }
    
    @Provides
    @Singleton
    fun provideAnnotationService(
        @ApplicationContext context: Context,
        annotationRepository: AnnotationRepository
    ): AnnotationService {
        return AnnotationService(context, annotationRepository)
    }
    
    @Provides
    @Singleton
    fun provideCloudVisionService(@ApplicationContext context: Context): CloudVisionService {
        return CloudVisionService(context)
    }
    
    @Provides
    @Singleton
    fun provideGenerateReceiptPdfUseCase(
        @ApplicationContext context: Context,
        notificationManager: NotificationManager
    ): GenerateReceiptPdfUseCase {
        return GenerateReceiptPdfUseCase(context, notificationManager)
    }
    
    @Provides
    @Singleton
    fun provideNotificationService(
        @ApplicationContext context: Context
    ): NotificationService {
        return NotificationService(context)
    }
    
    @Provides
    @Singleton
    fun provideNotificationManager(
        notificationService: NotificationService
    ): NotificationManager {
        return NotificationManager(notificationService)
    }
    
    @Provides
    @Singleton
    fun provideEmailService(
        @ApplicationContext context: Context
    ): EmailService {
        return EmailService(context)
    }
    
    @Provides
    @Singleton
    fun provideEmailAuthService(
        @ApplicationContext context: Context
    ): EmailAuthService {
        return EmailAuthService(context)
    }
    
    @Provides
    @Singleton
    fun provideEmailReceiptParser(): EmailReceiptParser {
        return EmailReceiptParser()
    }
    
    @Provides
    @Singleton
    fun provideEmailReceiptRepository(
        emailService: EmailService,
        emailReceiptParser: EmailReceiptParser,
        receiptRepository: ReceiptRepository,
        notificationManager: NotificationManager
    ): EmailReceiptRepository {
        return EmailReceiptRepository(emailService, emailReceiptParser, receiptRepository, notificationManager)
    }
    
    @Provides
    @Singleton
    fun provideCacheDao(database: ReceiptDatabase): CacheDao {
        return database.cacheDao()
    }
}
