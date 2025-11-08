package com.aktarjabed.androphoshop.data.model

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController

data class Project(
    val id: String,
    val name: String,
    val lastModified: String,
    val thumbnailUrl: String? = null
)

data class QuickAction(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val onClick: (NavController) -> Unit
)