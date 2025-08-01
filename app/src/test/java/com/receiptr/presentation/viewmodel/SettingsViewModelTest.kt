package com.receiptr.presentation.viewmodel

import android.content.Context
import android.content.SharedPreferences
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var mockContext: Context
    private lateinit var mockPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var settingsViewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        mockContext = mockk()
        mockPreferences = mockk()
        mockEditor = mockk()

        every { mockContext.getSharedPreferences(any(), any()) } returns mockPreferences
        every { mockPreferences.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.putBoolean(any(), any()) } returns mockEditor
        every { mockEditor.apply() } just Runs

        // Default theme mode to system
        every { mockPreferences.getString("theme_mode", SettingsViewModel.THEME_SYSTEM) } returns SettingsViewModel.THEME_SYSTEM
        every { mockPreferences.getBoolean(any(), any()) } returns true

        settingsViewModel = SettingsViewModel(mockContext)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial theme mode should be system`() = runTest {
        // Given - setup complete
        
        // When
        val themeMode = settingsViewModel.themeMode.first()
        
        // Then
        assertEquals(SettingsViewModel.THEME_SYSTEM, themeMode)
    }

    @Test
    fun `setThemeMode should update theme mode and save to preferences`() = runTest {
        // Given
        val newThemeMode = SettingsViewModel.THEME_DARK
        
        // When
        settingsViewModel.setThemeMode(newThemeMode)
        
        // Then
        val themeMode = settingsViewModel.themeMode.first()
        assertEquals(newThemeMode, themeMode)
        
        verify { mockEditor.putString("theme_mode", newThemeMode) }
        verify { mockEditor.apply() }
    }

    @Test
    fun `setThemeMode to dark should update isDarkModeEnabled to true`() = runTest {
        // Given
        val darkTheme = SettingsViewModel.THEME_DARK
        
        // When
        settingsViewModel.setThemeMode(darkTheme)
        
        // Then
        val isDarkMode = settingsViewModel.isDarkModeEnabled.first()
        assertTrue(isDarkMode)
    }

    @Test
    fun `setThemeMode to light should update isDarkModeEnabled to false`() = runTest {
        // Given
        val lightTheme = SettingsViewModel.THEME_LIGHT
        
        // When
        settingsViewModel.setThemeMode(lightTheme)
        
        // Then
        val isDarkMode = settingsViewModel.isDarkModeEnabled.first()
        assertEquals(false, isDarkMode)
    }

    @Test
    fun `toggleDarkMode should cycle through themes correctly`() = runTest {
        // Given - start with system theme
        assertEquals(SettingsViewModel.THEME_SYSTEM, settingsViewModel.themeMode.first())
        
        // When - toggle from system
        settingsViewModel.toggleDarkMode()
        
        // Then - should go to dark
        assertEquals(SettingsViewModel.THEME_DARK, settingsViewModel.themeMode.first())
        
        // When - toggle from dark
        settingsViewModel.toggleDarkMode()
        
        // Then - should go to light
        assertEquals(SettingsViewModel.THEME_LIGHT, settingsViewModel.themeMode.first())
        
        // When - toggle from light
        settingsViewModel.toggleDarkMode()
        
        // Then - should go to dark
        assertEquals(SettingsViewModel.THEME_DARK, settingsViewModel.themeMode.first())
    }

    @Test
    fun `all theme constants should be properly defined`() {
        // Given - constants defined in companion object
        
        // Then - verify they have expected values
        assertEquals("system", SettingsViewModel.THEME_SYSTEM)
        assertEquals("light", SettingsViewModel.THEME_LIGHT)
        assertEquals("dark", SettingsViewModel.THEME_DARK)
    }
}
