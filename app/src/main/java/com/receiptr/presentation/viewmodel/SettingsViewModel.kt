package com.receiptr.presentation.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    companion object {
        private const val PREFERENCES_NAME = "receiptr_settings"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_EMAIL_NOTIFICATIONS = "email_notifications"
        private const val KEY_PUSH_NOTIFICATIONS = "push_notifications"
    }
    
    private val preferences: SharedPreferences = 
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    
    // Dark Mode
    private val _isDarkModeEnabled = MutableStateFlow(
        preferences.getBoolean(KEY_DARK_MODE, false)
    )
    val isDarkModeEnabled: StateFlow<Boolean> = _isDarkModeEnabled.asStateFlow()
    
    // Language
    private val _selectedLanguage = MutableStateFlow(
        preferences.getString(KEY_LANGUAGE, "English") ?: "English"
    )
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()
    
    // Notifications
    private val _notificationsEnabled = MutableStateFlow(
        preferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    )
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()
    
    private val _emailNotificationsEnabled = MutableStateFlow(
        preferences.getBoolean(KEY_EMAIL_NOTIFICATIONS, true)
    )
    val emailNotificationsEnabled: StateFlow<Boolean> = _emailNotificationsEnabled.asStateFlow()
    
    private val _pushNotificationsEnabled = MutableStateFlow(
        preferences.getBoolean(KEY_PUSH_NOTIFICATIONS, true)
    )
    val pushNotificationsEnabled: StateFlow<Boolean> = _pushNotificationsEnabled.asStateFlow()
    
    fun toggleDarkMode() {
        viewModelScope.launch {
            val newValue = !_isDarkModeEnabled.value
            _isDarkModeEnabled.value = newValue
            preferences.edit().putBoolean(KEY_DARK_MODE, newValue).apply()
        }
    }
    
    fun setLanguage(language: String) {
        viewModelScope.launch {
            _selectedLanguage.value = language
            preferences.edit().putString(KEY_LANGUAGE, language).apply()
        }
    }
    
    fun toggleNotifications() {
        viewModelScope.launch {
            val newValue = !_notificationsEnabled.value
            _notificationsEnabled.value = newValue
            preferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, newValue).apply()
        }
    }
    
    fun toggleEmailNotifications() {
        viewModelScope.launch {
            val newValue = !_emailNotificationsEnabled.value
            _emailNotificationsEnabled.value = newValue
            preferences.edit().putBoolean(KEY_EMAIL_NOTIFICATIONS, newValue).apply()
        }
    }
    
    fun togglePushNotifications() {
        viewModelScope.launch {
            val newValue = !_pushNotificationsEnabled.value
            _pushNotificationsEnabled.value = newValue
            preferences.edit().putBoolean(KEY_PUSH_NOTIFICATIONS, newValue).apply()
        }
    }
}
