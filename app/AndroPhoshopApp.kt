package com.aktarjabed.androphoshop.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.aktarjabed.androphoshop.theme.AndroPhoshopTheme
import com.aktarjabed.androphoshop.ui.components.AIActionButton
import com.aktarjabed.androphoshop.ui.editor.EditorScreen
import com.aktarjabed.androphoshop.ui.home.HomeScreen
import com.aktarjabed.androphoshop.ui.projects.ProjectGalleryScreen
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun AndroPhoshopApp() {
    AndroPhoshopTheme {
        val navController = rememberNavController()
        val systemUiController = rememberSystemUiController()
        val isDarkTheme = isSystemInDarkTheme()

        // Set immersive system UI
        SideEffect {
            systemUiController.setSystemBarsColor(
                color = Color.Transparent,
                darkIcons = !isDarkTheme
            )
        }

        Scaffold(
            bottomBar = {
                BottomNavigationBar(navController)
            },
            floatingActionButton = {
                AIActionButton(navController)
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("home") { HomeScreen(navController) }
                composable("editor") { EditorScreen(navController) }
                composable("projects") { ProjectGalleryScreen(navController) }
                // composable("export") { ExportSheetScreen(navController) }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentRoute = currentRoute(navController)

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
    ) {
        val tabs = listOf(
            TabItem("home", Icons.Default.Home, "Home"),
            TabItem("editor", Icons.Default.Edit, "Edit"),
            TabItem("projects", Icons.Default.Folder, "Projects")
        )

        tabs.forEach { tab ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label
                    )
                },
                label = { Text(tab.label) },
                selected = currentRoute == tab.route,
                onClick = {
                    if (currentRoute != tab.route) {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

data class TabItem(val route: String, val icon: ImageVector, val label: String)