package com.receiptr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.receiptr.presentation.navigation.NavGraph
import com.receiptr.ui.theme.ReceiptrTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReceiptrTheme {
                ReceiptrApp()
            }
        }
    }
}

@Composable
fun ReceiptrApp() {
    val navController = rememberNavController()
    
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        NavGraph(navController = navController)
    }
}
