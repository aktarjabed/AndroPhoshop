package com.aktarjabed.androphoshop.ui.editor.components.ToolPanels

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aktarjabed.androphoshop.viewmodel.EditorViewModel

@Composable
fun AIToolPanel(viewModel: EditorViewModel) {
    val isProcessing by viewModel.isAIProcessing.collectAsState()
    val aiProgress by viewModel.aiProgress.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "AI Tools",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (isProcessing) {
            LinearProgressIndicator(
                progress = { aiProgress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AIToolButton(
                icon = Icons.Default.AutoAwesome,
                label = "Enhance",
                onClick = { viewModel.aiEnhance() }
            )
            AIToolButton(
                icon = Icons.Default.BackgroundRemove,
                label = "Remove BG",
                onClick = { viewModel.aiRemoveBackground() }
            )
            AIToolButton(
                icon = Icons.Default.WbSunny,
                label = "Relight",
                onClick = { viewModel.aiRelight() }
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AIToolButton(
                icon = Icons.Default.Landscape,
                label = "Replace BG",
                onClick = { viewModel.aiReplaceBackground() }
            )
            AIToolButton(
                icon = Icons.Default.ZoomIn,
                label = "Upscale",
                onClick = { viewModel.aiUpscale() }
            )
            AIToolButton(
                icon = Icons.Default.Palette,
                label = "Colorize",
                onClick = { viewModel.aiColorize() }
            )
        }
    }
}

@Composable
private fun AIToolButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .height(56.dp)
    ) {
        Icon(icon, contentDescription = label)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label)
    }
}