package com.androphoshop.presentation.screens.camera

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun CameraScreen(
    onPhotoCaptured: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                // In a real app, this would be the URI from CameraX
                onPhotoCaptured("content://media/external/images/media/123")
            },
        contentAlignment = Alignment.Center
    ) {
        Text("Camera Placeholder - Tap to Capture")
        // Integrate CameraX view here
    }
}