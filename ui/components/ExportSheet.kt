package com.aktarjabed.androphoshop.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aktarjabed.androphoshop.viewmodel.EditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportSheet(
    viewModel: EditorViewModel,
    onDismiss: () -> Unit,
    onExport: (ExportSettings) -> Unit
) {
    var format by remember { mutableStateOf("JPEG") }
    var quality by remember { mutableStateOf(90f) }
    var resolution by remember { mutableStateOf(1f) }
    var applyUpscale by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Export Settings", style = MaterialTheme.typography.titleMedium)
            Divider()

            // Format
            Text("File Format")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ExportOption("JPEG", format == "JPEG") { format = "JPEG" }
                ExportOption("PNG", format == "PNG") { format = "PNG" }
                ExportOption("WEBP", format == "WEBP") { format = "WEBP" }
            }

            // Quality
            Text("Quality: ${quality.toInt()}%")
            Slider(
                value = quality,
                onValueChange = { quality = it },
                valueRange = 50f..100f
            )

            // Resolution
            Text("Resolution: ${(resolution * 100).toInt()}%")
            Slider(
                value = resolution,
                onValueChange = { resolution = it },
                valueRange = 0.5f..2f
            )

            // Upscaling
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = applyUpscale, onCheckedChange = { applyUpscale = it })
                Text("Apply AI Upscaling")
            }

            // Buttons
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                Button(
                    onClick = {
                        onExport(ExportSettings(format, quality.toInt(), resolution, applyUpscale))
                        onDismiss()
                    }
                ) {
                    Icon(Icons.Default.Download, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Export")
                }
            }
        }
    }
}

@Composable
private fun ExportOption(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}

data class ExportSettings(
    val format: String,
    val quality: Int,
    val scale: Float,
    val useUpscale: Boolean
)