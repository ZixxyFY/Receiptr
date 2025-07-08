# Firebase Firestore Integration for User Management

This document explains how Firebase Firestore has been integrated into the Receiptr app to store and manage user data when they register.

## Overview

The integration automatically saves user information to Firestore whenever a new user registers through any authentication method (Email/Password, Google Sign-In, or Phone Authentication).

## Architecture

### 1. Data Layer
- **FirebaseFirestoreService**: Handles all Firestore operations for user data
- **UserRepository**: Interface for user data operations
- **UserRepositoryImpl**: Implementation of UserRepository using Firestore

### 2. Domain Layer
- **User Model**: Enhanced with Firestore-specific fields and serialization methods
- **Use Cases**: SaveUserUseCase, UpdateUserUseCase, GetUserByIdUseCase

### 3. Updated Authentication Flow
- **FirebaseAuthService**: Now automatically saves new users to Firestore
- **AuthViewModel**: Unchanged - existing functionality works seamlessly

## User Data Structure

```kotlin
data class User(
    val id: String = "",                     // Firebase Auth UID
    val email: String? = null,               // User's email
    val phoneNumber: String? = null,         // User's phone number
    val displayName: String? = null,         // Display name from auth provider
    val photoUrl: String? = null,            // Profile photo URL
    val isEmailVerified: Boolean = false,    // Email verification status
    val createdAt: Long = System.currentTimeMillis(),  // Registration timestamp
    val updatedAt: Long = System.currentTimeMillis(),  // Last update timestamp
    val firstName: String? = null,           // First name (optional)
    val lastName: String? = null,            // Last name (optional)
    val isActive: Boolean = true,            // Account status
    val preferences: Map<String, Any> = emptyMap()     // User preferences
)
```

## Firestore Collection Structure

```
/users/{userId}
  ├── id: string
  ├── email: string?
  ├── phoneNumber: string?
  ├── displayName: string?
  ├── photoUrl: string?
  ├── isEmailVerified: boolean
  ├── createdAt: timestamp
  ├── updatedAt: timestamp
  ├── firstName: string?
  ├── lastName: string?
  ├── isActive: boolean
  └── preferences: map
```

## Automatic User Saving

### Email/Password Registration
When a user registers with email and password:
1. Firebase Auth creates the user account
2. User data is automatically saved to Firestore
3. If Firestore save fails, authentication still succeeds (user can try again later)

### Google Sign-In
When a user signs in with Google:
1. Firebase Auth authenticates the user
2. System checks if user exists in Firestore
3. If new user: saves user data to Firestore
4. If existing user: proceeds without saving

### Phone Authentication
When a user verifies their phone number:
1. Firebase Auth authenticates the user
2. System checks if user exists in Firestore
3. If new user: saves user data to Firestore
4. If existing user: proceeds without saving

## Available Operations

### 1. Save User
```kotlin
class SaveUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Result<User>
}
```

### 2. Update User
```kotlin
class UpdateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Result<User>
}
```

### 3. Get User by ID
```kotlin
class GetUserByIdUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<User?>
}
```

### 4. Update User Preferences
```kotlin
// Through UserRepository
suspend fun updateUserPreferences(
    userId: String, 
    preferences: Map<String, Any>
): Result<Unit>
```

## Example Usage

### Get Current User Data from Firestore
```kotlin
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    fun loadUserProfile() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                if (user != null) {
                    val result = getUserByIdUseCase(user.id)
                    result.onSuccess { firestoreUser ->
                        // Use complete user data from Firestore
                        _userProfile.value = firestoreUser
                    }
                }
            }
        }
    }
}
```

### Update User Profile
```kotlin
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val updateUserUseCase: UpdateUserUseCase
) : ViewModel() {
    
    fun updateProfile(firstName: String, lastName: String) {
        viewModelScope.launch {
            val currentUser = _userProfile.value
            if (currentUser != null) {
                val updatedUser = currentUser.copy(
                    firstName = firstName,
                    lastName = lastName,
                    updatedAt = System.currentTimeMillis()
                )
                
                val result = updateUserUseCase(updatedUser)
                result.onSuccess { 
                    _userProfile.value = it 
                }
            }
        }
    }
}
```

### Update User Preferences
```kotlin
fun updateNotificationPreferences(enablePushNotifications: Boolean) {
    viewModelScope.launch {
        val userId = getCurrentUserId() // Get from auth
        val preferences = mapOf(
            "pushNotifications" to enablePushNotifications,
            "emailNotifications" to true
        )
        
        userRepository.updateUserPreferences(userId, preferences)
    }
}
```

## Error Handling

All Firestore operations return `Result<T>` objects:

```kotlin
val result = userRepository.saveUser(user)
result.onSuccess { savedUser ->
    // Handle success
}.onFailure { exception ->
    // Handle error
    Log.e("Firestore", "Failed to save user", exception)
}
```

## Dependencies Added

The following dependency was added to `build.gradle.kts`:
```kotlin
implementation("com.google.firebase:firebase-firestore-ktx")
```

## Security Rules

Make sure your Firestore security rules allow authenticated users to read/write their own data:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

## Benefits

1. **Automatic**: Users are saved to Firestore without any additional code in your UI
2. **Reliable**: Graceful handling of Firestore failures doesn't break authentication
3. **Extensible**: Easy to add more user fields and preferences
4. **Consistent**: Same user data structure across all authentication methods
5. **Clean Architecture**: Proper separation of concerns with repository pattern

## Next Steps

- Add user profile editing screens
- Implement user preferences management
- Add user analytics and usage tracking
- Create admin panel for user management
