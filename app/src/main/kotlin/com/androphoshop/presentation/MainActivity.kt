package com.androphoshop.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.androphoshop.presentation.navigation.AppNavigation
import com.androphoshop.presentation.theme.AndroPhoshopTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var keepSplashScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { keepSplashScreen }
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // In a real app, you might have background tasks to wait for.
        // For this setup, we'll just remove the splash immediately.
        keepSplashScreen = false

        setContent {
            AndroPhoshopApp()
        }
    }
}

@Composable
fun AndroPhoshopApp() {
    val darkTheme = isSystemInDarkTheme()
    var appState by remember { mutableStateOf(AppState.IDLE) }

    // Handle app state changes
    LaunchedEffect(Unit) {
        // Simulate loading and then set to ready
        appState = AppState.LOADING
        delay(100) // Small delay to ensure splash is gone
        appState = AppState.READY
    }

    AndroPhoshopTheme(darkTheme = darkTheme) {
        AppNavigation(appState = appState)
    }
}

enum class AppState {
    IDLE, LOADING, READY, ERROR
}