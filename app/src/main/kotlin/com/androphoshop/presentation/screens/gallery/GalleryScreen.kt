package com.androphoshop.presentation.screens.gallery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun GalleryScreen(
    onImageSelected: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                // In a real app, this would be the URI from the ImagePicker
                onImageSelected("content://media/external/images/media/456")
            },
        contentAlignment = Alignment.Center
    ) {
        Text("Gallery Placeholder - Tap to Select Image")
        // Use ImagePicker library here
    }
}