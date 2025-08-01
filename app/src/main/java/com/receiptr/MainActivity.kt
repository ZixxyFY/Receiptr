package com.receiptr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.receiptr.presentation.navigation.NavGraph
import com.receiptr.presentation.viewmodel.SettingsViewModel
import com.receiptr.ui.theme.ReceiptrTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReceiptrApp()
        }
    }
}

@Composable
fun ReceiptrApp(
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val themeMode by settingsViewModel.themeMode.collectAsState()
    val systemInDarkTheme = isSystemInDarkTheme()
    
    // Determine dark theme based on user preference
    val darkTheme = when (themeMode) {
        SettingsViewModel.THEME_DARK -> true
        SettingsViewModel.THEME_LIGHT -> false
        else -> systemInDarkTheme // Follow system theme
    }
    
    ReceiptrTheme(
        darkTheme = darkTheme
    ) {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            NavGraph(navController = navController)
        }
    }
}
