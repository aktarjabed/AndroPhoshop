package com.aktarjabed.androphoshop.presentation.screens.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aktarjabed.androphoshop.features.ai.AIEnhancementEngine
import com.aktarjabed.androphoshop.features.ai.BlendModeCompositor
import com.aktarjabed.androphoshop.features.ai.BackgroundReplaceEngine
import com.aktarjabed.androphoshop.presentation.viewmodels.EditorViewModel
import java.lang.Float.max
import java.lang.Float.min

@Composable
fun EditorScreen(
    imageUri: String,
    onNavigateBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val pickBg = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.loadBackgroundFromUri(uri)
    }

    val pickMultiple = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { list ->
        val uris = list.mapNotNull { it }
        if (uris.isNotEmpty()) viewModel.batchCutoutAndExport(uris)
    }

    LaunchedEffect(imageUri) {
        viewModel.loadImage(Uri.parse(imageUri))
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            EditorTopBar(
                onBack = onNavigateBack,
                onSave = { viewModel.exportCutoutPng() }
            )
        },
        bottomBar = {
            EditorBottomBar(
                viewModel = viewModel,
                onPickBg = { pickBg.launch(ActivityResultContracts.PickVisualMedia.ImageOnly) },
                onBatchCutout = { pickMultiple.launch(ActivityResultContracts.PickVisualMedia.ImageOnly) }
            )
        }
    ) { paddingValues ->
        var transformMode by remember { mutableStateOf(false) }

        Column(modifier = Modifier.padding(paddingValues)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(selected = transformMode, onClick = { transformMode = !transformMode }, label = { Text("Transform Mode") })
                OutlinedButton(onClick = { viewModel.resetTransform() }) { Text("Reset Transform") }
            }

            val tState = rememberTransformableState { zoomChange, panChange, rotationChange ->
                if (transformMode) {
                    viewModel.subjectScale = (viewModel.subjectScale * zoomChange).coerceIn(0.2f, 5f)
                    viewModel.subjectRotation += Math.toDegrees(rotationChange.toDouble()).toFloat()
                    viewModel.subjectOffsetX += panChange.x
                    viewModel.subjectOffsetY += panChange.y
                }
            }

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .transformable(tState)
            ) {
                viewModel.editedBitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                if (transformMode) {
                                    translationX = viewModel.subjectOffsetX
                                    translationY = viewModel.subjectOffsetY
                                    scaleX = viewModel.subjectScale
                                    scaleY = viewModel.subjectScale
                                    rotationZ = viewModel.subjectRotation
                                }
                            },
                        contentScale = ContentScale.Fit
                    )
                } ?: if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }

        viewModel.errorMessage?.let { error ->
            ErrorDialog(
                message = error,
                onDismiss = { viewModel.errorMessage = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorTopBar(
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    TopAppBar(
        title = { Text("Editor") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onSave) {
                Icon(Icons.Default.Save, contentDescription = "Save")
            }
        }
    )
}

@Composable
fun EditorBottomBar(
    viewModel: EditorViewModel,
    onPickBg: () -> Unit,
    onBatchCutout: () -> Unit
) {
    var selectedTool by remember { mutableStateOf(EditorTool.NONE) }
    val isLoading = viewModel.isLoading

    Column {
        NavigationBar {
            NavigationBarItem(
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI") },
                label = { Text("AI") },
                selected = selectedTool == EditorTool.AI_ENHANCE,
                onClick = { selectedTool = EditorTool.AI_ENHANCE }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Layers, contentDescription = "Replace BG") },
                label = { Text("Replace BG") },
                selected = selectedTool == EditorTool.REPLACE_BG,
                onClick = { selectedTool = EditorTool.REPLACE_BG }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Brush, contentDescription = "Relight & Blend") },
                label = { Text("Effects") },
                selected = selectedTool == EditorTool.EFFECTS,
                onClick = { selectedTool = EditorTool.EFFECTS }
            )
             NavigationBarItem(
                icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = "Batch") },
                label = { Text("Batch") },
                selected = selectedTool == EditorTool.BATCH,
                onClick = { selectedTool = EditorTool.BATCH }
            )
        }

        when (selectedTool) {
            EditorTool.AI_ENHANCE -> AIEnhancementPanel(
                onEnhance = { type -> viewModel.enhanceAI(type) },
                onRemoveSimple = { viewModel.removeBackgroundAI() },
                onRemoveMl = { viewModel.createCutoutWithML() },
                isLoading = isLoading
            )
            EditorTool.REPLACE_BG -> BackgroundReplacePanel(
                viewModel = viewModel,
                onPickBg = onPickBg,
                isLoading = isLoading
            )
            EditorTool.EFFECTS -> RelightAndBlendPanel(viewModel = viewModel)
            EditorTool.BATCH -> BatchPanel(
                viewModel = viewModel,
                onBatchCutout = onBatchCutout
            )
            else -> {}
        }
    }
}

@Composable
fun AIEnhancementPanel(
    onEnhance: (AIEnhancementEngine.EnhancementType) -> Unit,
    onRemoveSimple: () -> Unit,
    onRemoveMl: () -> Unit,
    isLoading: Boolean
) {
    Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("AI Tools", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(onClick = { onEnhance(AIEnhancementEngine.EnhancementType.AUTO_ENHANCE) }, enabled = !isLoading) { Text("AI Enhance") }
                OutlinedButton(onClick = onRemoveSimple, enabled = !isLoading) { Text("Remove BG (Simple)") }
                FilledTonalButton(onClick = onRemoveMl, enabled = !isLoading) { Text("Remove BG (ML)") }
            }
        }
    }
}

@Composable
fun BackgroundReplacePanel(
    viewModel: EditorViewModel,
    onPickBg: () -> Unit,
    isLoading: Boolean
) {
    val params = viewModel.replaceParams

    Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Background Replace", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = params.mode == BackgroundReplaceEngine.Mode.AUTO_BLUR, onClick = { viewModel.replaceParams = params.copy(mode = BackgroundReplaceEngine.Mode.AUTO_BLUR) }, label = { Text("Auto Blur") })
                FilterChip(selected = params.mode == BackgroundReplaceEngine.Mode.COLOR, onClick = { viewModel.replaceParams = params.copy(mode = BackgroundReplaceEngine.Mode.COLOR) }, label = { Text("Color") })
                FilterChip(selected = params.mode == BackgroundReplaceEngine.Mode.IMAGE_FIT, onClick = { viewModel.replaceParams = params.copy(mode = BackgroundReplaceEngine.Mode.IMAGE_FIT) }, label = { Text("Image Fit") })
                FilterChip(selected = params.mode == BackgroundReplaceEngine.Mode.IMAGE_FILL, onClick = { viewModel.replaceParams = params.copy(mode = BackgroundReplaceEngine.Mode.IMAGE_FILL) }, label = { Text("Image Fill") })
            }

            if (params.mode == BackgroundReplaceEngine.Mode.IMAGE_FIT || params.mode == BackgroundReplaceEngine.Mode.IMAGE_FILL) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onPickBg) { Text(if (viewModel.backgroundBitmap == null) "Pick Background Image" else "Change Background Image") }
            }

            if (params.mode == BackgroundReplaceEngine.Mode.COLOR) {
                // Color selector (simple RGB sliders for now)
            }

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = params.addShadow, onCheckedChange = { viewModel.replaceParams = params.copy(addShadow = it) })
                Text("Add Subject Shadow")
            }

            Spacer(Modifier.height(8.dp))
            FilledTonalButton(onClick = { viewModel.replaceBackground() }, enabled = !isLoading) { Text("Apply Replace") }
        }
    }
}

@Composable
fun RelightAndBlendPanel(viewModel: EditorViewModel) {
    Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Relight & Blend", style = MaterialTheme.typography.titleMedium)

            Text("Rim Intensity: ${"%.2f".format(viewModel.relightParams.intensity)}")
            Slider(value = viewModel.relightParams.intensity, onValueChange = { viewModel.relightParams = viewModel.relightParams.copy(intensity = it.coerceIn(0f,1f)) }, valueRange = 0f..1f)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Normal" to BlendModeCompositor.Mode.NORMAL, "Multiply" to BlendModeCompositor.Mode.MULTIPLY, "Screen" to BlendModeCompositor.Mode.SCREEN, "Overlay" to BlendModeCompositor.Mode.OVERLAY).forEach { (label, mode) ->
                    FilterChip(selected = viewModel.selectedBlendMode == mode, onClick = { viewModel.selectedBlendMode = mode }, label = { Text(label) })
                }
            }
        }
    }
}

@Composable
fun BatchPanel(viewModel: EditorViewModel, onBatchCutout: () -> Unit) {
    val batchRunning by viewModel.batchRunning.collectAsState()
    val batchProgress by viewModel.batchProgress.collectAsState()

    Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Batch Processing", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onBatchCutout, enabled = !batchRunning) { Text("Batch Cutout") }
            }

            if (batchRunning) {
                LinearProgressIndicator(progress = batchProgress, modifier = Modifier.fillMaxWidth().height(4.dp))
            }
        }
    }
}

@Composable
fun ErrorDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Error") },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
    )
}

enum class EditorTool {
    NONE, AI_ENHANCE, REPLACE_BG, EFFECTS, BATCH
}