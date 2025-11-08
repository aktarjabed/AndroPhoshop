package com.aktarjabed.androphoshop.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aktarjabed.androphoshop.presentation.AppState
import com.aktarjabed.androphoshop.presentation.screens.camera.CameraScreen
import com.aktarjabed.androphoshop.presentation.screens.editor.EditorScreen
import com.aktarjabed.androphoshop.presentation.screens.gallery.GalleryScreen
import com.aktarjabed.androphoshop.presentation.screens.home.HomeScreen
// import com.aktarjabed.androphoshop.presentation.screens.splash.SplashScreen // No splash screen needed here

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    appState: AppState
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route, // Go directly to Home
        modifier = modifier
    ) {
        // Splash screen is handled by the system, so no composable route for it.

        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController
            )
        }

        composable(
            route = Screen.Editor.route,
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            EditorScreen(
                imageUri = imageUri,
                onNavigateBack = { navController.popBackStack() },
                onSaveComplete = { savedUri ->
                    // Handle save completion
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onPhotoCaptured = { imageUri ->
                    navController.navigate(Screen.Editor.createRoute(imageUri)) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Gallery.route) {
            GalleryScreen(
                onImageSelected = { imageUri ->
                    navController.navigate(Screen.Editor.createRoute(imageUri))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Splash : Screen("splash") // Kept for reference but not used in NavHost
    object Home : Screen("home")
    object Editor : Screen("editor/{imageUri}") {
        fun createRoute(imageUri: String) = "editor/$imageUri"
    }
    object Camera : Screen("camera")
    object Gallery : Screen("gallery")
}