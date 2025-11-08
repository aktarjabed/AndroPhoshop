package com.aktarjabed.androphoshop.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aktarjabed.androphoshop.R
import com.aktarjabed.androphoshop.presentation.components.FeatureCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToEditor: (String) -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    // This would be populated by a ViewModel in a full implementation
    val recentProjects by remember { mutableStateOf(emptyList<ProjectItem>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                actions = {
                    IconButton(onClick = { /* Open settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Quick Actions Grid
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val quickActions = getQuickActions(
                onNavigateToCamera = onNavigateToCamera,
                onNavigateToGallery = onNavigateToGallery
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(quickActions) { action ->
                    FeatureCard(
                        title = action.title,
                        icon = action.icon,
                        description = action.description,
                        onClick = action.onClick,
                        modifier = Modifier.aspectRatio(1f)
                    )
                }
            }

            // Recent Projects
            if (recentProjects.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Recent Projects",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(120.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentProjects) { project ->
                        RecentProjectCard(
                            project = project,
                            onClick = { /* Open project */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentProjectCard(
    project: ProjectItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Photo, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = project.name,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
            Text(
                text = project.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getQuickActions(
    onNavigateToCamera: () -> Unit,
    onNavigateToGallery: () -> Unit
): List<QuickAction> {
    return listOf(
        QuickAction(
            title = "Camera",
            icon = Icons.Default.CameraAlt,
            description = "Take a new photo",
            onClick = onNavigateToCamera
        ),
        QuickAction(
            title = "Gallery",
            icon = Icons.Default.PhotoLibrary,
            description = "Choose from gallery",
            onClick = onNavigateToGallery
        ),
        QuickAction(
            title = "AI Enhance",
            icon = Icons.Default.AutoAwesome,
            description = "Smart photo enhancement",
            onClick = { /* TODO */ }
        ),
        QuickAction(
            title = "Remove BG",
            icon = Icons.Default.PersonRemove,
            description = "Erase the background",
            onClick = { /* TODO */ }
        )
    )
}

data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val description: String,
    val onClick: () -> Unit
)

data class ProjectItem(
    val id: String,
    val name: String,
    val date: String,
    val thumbnailUri: String
)