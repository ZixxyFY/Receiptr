# Receiptr - AI-Powered Expense Tracking App

A modular, scalable Android authentication flow built with **Jetpack Compose**, **Firebase Authentication**, and **Clean Architecture**.

## 🏗️ Architecture

This project follows **Clean Architecture** principles with clear separation of concerns:

### 📂 Project Structure

```
app/src/main/java/com/receiptr/
├── data/                           # Data Layer
│   ├── remote/
│   │   └── FirebaseAuthService.kt  # Firebase authentication implementation
│   └── repository/
│       └── AuthRepositoryImpl.kt   # Repository implementation
├── domain/                         # Domain Layer
│   ├── model/
│   │   ├── User.kt                 # User data model
│   │   └── AuthResult.kt          # Authentication result models
│   ├── repository/
│   │   └── AuthRepository.kt       # Repository interface
│   └── usecase/
│       ├── LoginWithGoogleUseCase.kt
│       ├── LoginWithEmailUseCase.kt
│       ├── LoginWithPhoneUseCase.kt
│       └── RegisterUserUseCase.kt
├── presentation/                   # Presentation Layer
│   ├── auth/
│   │   ├── SplashScreen.kt
│   │   ├── LoginScreen.kt
│   │   ├── SignupScreen.kt
│   │   ├── EmailAuthScreen.kt
│   │   └── PhoneAuthScreen.kt
│   ├── home/
│   │   └── HomeScreen.kt
│   ├── viewmodel/
│   │   └── AuthViewModel.kt
│   └── navigation/
│       └── NavGraph.kt
├── di/                            # Dependency Injection
│   └── AppModule.kt
└── ui/theme/                      # UI Theme
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

## ✅ Features

### Authentication Methods
- **Google Sign-In** - One-tap authentication with Google
- **Email/Password** - Traditional email registration and login
- **Phone Number** - SMS OTP verification
- **Input Validation** - Real-time validation with user feedback
- **Error Handling** - Comprehensive error messages

### UI/UX
- **Material 3 Design** - Modern, accessible interface
- **Dark/Light Mode** - Automatic theme switching
- **Responsive Layout** - Optimized for different screen sizes
- **Loading States** - Clear feedback during authentication
- **Navigation** - Smooth transitions between screens

### Architecture Benefits
- **Clean Architecture** - Testable, maintainable code
- **Dependency Injection** - Modular, loosely coupled components
- **State Management** - Reactive UI with StateFlow
- **Repository Pattern** - Abstracted data access

## 🚀 Setup Instructions

### 1. Firebase Configuration

1. **Create a Firebase Project**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a new project named "Receiptr"

2. **Add Android App**
   - Package name: `com.receiptr`
   - Download `google-services.json`
   - Place it in `app/` directory

3. **Enable Authentication Methods**
   - Go to Authentication > Sign-in method
   - Enable: Email/Password, Google, Phone

4. **Configure Google Sign-In**
   - Add SHA-1 certificate fingerprint
   - Note the Web client ID for Google Sign-In

### 2. Google Sign-In Setup

1. **Get SHA-1 Fingerprint**
   ```bash
   # Debug keystore
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   
   # Release keystore (if you have one)
   keytool -list -v -keystore path/to/your/keystore -alias your_alias_name
   ```

2. **Update Firebase Console**
   - Add SHA-1 fingerprint in Project Settings > Your apps > SHA certificate fingerprints

3. **Update LoginScreen.kt**
   - Replace `"your_web_client_id_here"` with your actual Web client ID
   - Line 109 in `LoginScreen.kt`

### 3. Dependencies

All required dependencies are already included in `build.gradle.kts`:

- **Jetpack Compose** - Modern UI toolkit
- **Firebase Auth** - Authentication backend
- **Hilt** - Dependency injection
- **Navigation Compose** - Type-safe navigation
- **Coroutines** - Asynchronous programming

### 4. Build and Run

1. **Sync Project**
   ```bash
   ./gradlew build
   ```

2. **Run on Device/Emulator**
   ```bash
   ./gradlew installDebug
   ```

## 📱 Screens Overview

### 1. SplashScreen
- App logo and branding
- 2-second delay before navigation
- Checks authentication state

### 2. LoginScreen
- Three authentication options
- Google Sign-In button
- Email/Password option
- Phone number option

### 3. EmailAuthScreen
- Toggle between Sign In/Sign Up
- Email and password validation
- Forgot password option
- Real-time error handling

### 4. SignupScreen
- Email registration
- Password strength validation
- Confirm password matching
- Terms and privacy notice

### 5. PhoneAuthScreen
- Phone number input
- OTP verification
- Resend code functionality
- Number change option

### 6. HomeScreen
- Welcome message with user info
- Sign out functionality
- Placeholder for app features

## 🔧 Customization

### Theming
- Update colors in `ui/theme/Color.kt`
- Modify typography in `ui/theme/Type.kt`
- Adjust theme in `ui/theme/Theme.kt`

### Navigation
- Add new screens in `presentation/navigation/NavGraph.kt`
- Define new routes and composables

### Authentication Logic
- Extend use cases in `domain/usecase/`
- Add new authentication methods in `FirebaseAuthService.kt`

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### UI Tests
```bash
./gradlew connectedAndroidTest
```

## 🔒 Security Features

- **Input Validation** - Prevents malicious input
- **Secure Storage** - Firebase handles token storage
- **Network Security** - HTTPS communication
- **Authentication State** - Proper session management

## 📋 TODO

- [ ] Implement forgot password functionality
- [ ] Add biometric authentication
- [ ] Implement proper phone auth with activity context
- [ ] Add email verification flow
- [ ] Implement social login (Facebook, Twitter)
- [ ] Add privacy policy and terms screens
- [ ] Implement deep linking
- [ ] Add analytics tracking

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**Built with ❤️ using Modern Android Development practices**
