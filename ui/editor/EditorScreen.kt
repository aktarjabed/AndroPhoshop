package com.aktarjabed.androphoshop.ui.editor

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aktarjabed.androphoshop.ui.components.ExportSheet
import com.aktarjabed.androphoshop.ui.editor.components.AnimatedToolDrawer
import com.aktarjabed.androphoshop.ui.editor.components.MiniMapOverlay
import com.aktarjabed.androphoshop.ui.editor.components.TransformableCanvas
import com.aktarjabed.androphoshop.ui.editor.state.EditorTool
import com.aktarjabed.androphoshop.viewmodel.EditorViewModel

@Composable
fun EditorScreen(
    navController: NavController,
    viewModel: EditorViewModel = hiltViewModel()
) {
    EditorScaffold(viewModel) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main editing workspace
            EditingWorkspace(viewModel)

            // UI overlays
            TopActionBar(viewModel, onBack = { navController.popBackStack() })
            MiniMapOverlay(viewModel)
            BeforeAfterOverlay(viewModel)
        }
    }
}

@Composable
private fun EditorScaffold(
    viewModel: EditorViewModel,
    content: @Composable (PaddingValues) -> Unit
) {
    val currentTool by viewModel.currentTool.collectAsState()
    val isProcessing by viewModel.isAIProcessing.collectAsState()

    Scaffold(
        topBar = { /* custom top bar in overlay */ },
        bottomBar = {
            AnimatedToolDrawer(
                tool = currentTool,
                onToolChange = { viewModel.setCurrentTool(it) },
                viewModel = viewModel
            )
        },
        floatingActionButton = {
            if (isProcessing) AIProcessingFab(viewModel)
        }
    ) { paddingValues -> content(paddingValues) }
}

@Composable
private fun EditingWorkspace(viewModel: EditorViewModel) {
    val img by viewModel.currentImage.collectAsState()
    val zoom by viewModel.zoomState.collectAsState()
    val rot by viewModel.rotationState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        TransformableCanvas(
            bitmap = img,
            zoomState = zoom,
            rotationState = rot,
            onZoomChange = viewModel::updateZoom,
            onRotationChange = viewModel::updateRotation,
            onTap = { viewModel.onCanvasTapped(it) }
        )
    }
}

@Composable
private fun TopActionBar(
    viewModel: EditorViewModel,
    onBack: () -> Unit
) {
    val currentTool by viewModel.currentTool.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    var showExport by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .align(Alignment.TopStart)
    ) {
        // Frosted background
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = .92f),
                            MaterialTheme.colorScheme.surface.copy(alpha = .75f)
                        )
                    )
                )
                .blur(16.dp),
            color = Color.Transparent,
            tonalElevation = 3.dp
        ) { }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = currentTool.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Row {
                IconButton(onClick = viewModel::undo, enabled = canUndo) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo")
                }
                IconButton(onClick = viewModel::redo, enabled = canRedo) {
                    Icon(Icons.Default.Redo, contentDescription = "Redo")
                }
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Export") },
                            onClick = {
                                showExport = true
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    }

    if (showExport) {
        ExportSheet(
            viewModel = viewModel,
            onDismiss = { showExport = false },
            onExport = { viewModel.exportImage(it) }
        )
    }
}

@Composable
private fun BeforeAfterOverlay(viewModel: EditorViewModel) {
    val show by viewModel.showBeforeAfter.collectAsState()
    AnimatedVisibility(
        visible = show,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { viewModel.toggleBeforeAfter() }
                }
        ) {
            // Simple overlay explaining the state (replace with original bitmap if you wish)
            Surface(
                color = Color.Black.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "Before — tap to toggle",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun AIProcessingFab(viewModel: EditorViewModel) {
    val progress by viewModel.aiProgress.collectAsState()
    ExtendedFloatingActionButton(
        onClick = { viewModel.cancelAI() },
        modifier = Modifier.padding(16.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        if (progress > 0f) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(8.dp))
        }
        Text("AI Processing…")
    }
}