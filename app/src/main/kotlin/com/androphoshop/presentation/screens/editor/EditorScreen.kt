package com.androphoshop.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.androphoshop.features.ai.AIEnhancementEngine
import com.androphoshop.presentation.viewmodels.EditorViewModel

@Composable
fun EditorScreen(
    imageUri: String,
    onNavigateBack: () -> Unit,
    onSaveComplete: (String) -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(imageUri) {
        viewModel.loadImage(imageUri)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            EditorTopBar(
                onBack = onNavigateBack,
                onSave = { viewModel.saveImage() },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                canUndo = uiState.historyIndex > 0,
                canRedo = uiState.historyIndex < uiState.history.size - 1,
                isSaving = uiState.isSaving
            )
        },
        bottomBar = {
            EditorBottomBar(
                onEnhance = { type -> viewModel.enhanceImage(type) },
                onRemoveBackground = { viewModel.removeBackground() },
                onApplyFilter = { filter -> viewModel.applyFilter(filter) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            uiState.currentBitmap?.let { bitmap ->
                AsyncImage(
                    model = bitmap,
                    contentDescription = "Edited image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            if (uiState.isLoading) {
                CircularProgressIndicator()
            }

            uiState.error?.let { error ->
                ErrorDialog(
                    message = error,
                    onDismiss = { viewModel.clearError() }
                )
            }

            uiState.lastOperation?.let { operation ->
                LaunchedEffect(operation) {
                    snackbarHostState.showSnackbar(operation)
                    viewModel.clearLastOperation()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorTopBar(
    onBack: () -> Unit,
    onSave: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    isSaving: Boolean
) {
    TopAppBar(
        title = { Text("Editor") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onUndo, enabled = canUndo) {
                Icon(Icons.Default.Undo, contentDescription = "Undo")
            }
            IconButton(onClick = onRedo, enabled = canRedo) {
                Icon(Icons.Default.Redo, contentDescription = "Redo")
            }
            IconButton(onClick = onSave, enabled = !isSaving) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Save, contentDescription = "Save")
                }
            }
        }
    )
}

@Composable
fun EditorBottomBar(
    onEnhance: (AIEnhancementEngine.EnhancementType) -> Unit,
    onRemoveBackground: () -> Unit,
    onApplyFilter: (String) -> Unit
) {
    var selectedTool by remember { mutableStateOf(EditorTool.NONE) }

    Column {
        NavigationBar {
            NavigationBarItem(
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI") },
                label = { Text("AI") },
                selected = selectedTool == EditorTool.AI_ENHANCE,
                onClick = { selectedTool = EditorTool.AI_ENHANCE }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Filter, contentDescription = "Filters") },
                label = { Text("Filters") },
                selected = selectedTool == EditorTool.FILTERS,
                onClick = { selectedTool = EditorTool.FILTERS }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Crop, contentDescription = "Crop") },
                label = { Text("Crop") },
                selected = selectedTool == EditorTool.CROP,
                onClick = { selectedTool = EditorTool.CROP }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Build, contentDescription = "Tools") },
                label = { Text("Tools") },
                selected = selectedTool == EditorTool.TOOLS,
                onClick = { selectedTool = EditorTool.TOOLS }
            )
        }

        when (selectedTool) {
            EditorTool.AI_ENHANCE -> AIEnhancementPanel(onEnhance = onEnhance)
            EditorTool.FILTERS -> FilterPanel(onApplyFilter = onApplyFilter)
            EditorTool.CROP -> CropPanel()
            EditorTool.TOOLS -> ToolsPanel(onRemoveBackground = onRemoveBackground)
            else -> {}
        }
    }
}

@Composable
fun AIEnhancementPanel(onEnhance: (AIEnhancementEngine.EnhancementType) -> Unit) {
    Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("AI Enhancements", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = false, onClick = { onEnhance(AIEnhancementEngine.EnhancementType.AUTO_ENHANCE) }, label = { Text("Auto") })
                FilterChip(selected = false, onClick = { onEnhance(AIEnhancementEngine.EnhancementType.SUPER_RESOLUTION) }, label = { Text("Super Res") })
                FilterChip(selected = false, onClick = { onEnhance(AIEnhancementEngine.EnhancementType.FACE_ENHANCE) }, label = { Text("Face") })
            }
        }
    }
}

@Composable
fun FilterPanel(onApplyFilter: (String) -> Unit) {
    Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Filters", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = false, onClick = { onApplyFilter("grayscale") }, label = { Text("Grayscale") })
                FilterChip(selected = false, onClick = { onApplyFilter("sepia") }, label = { Text("Sepia") })
            }
        }
    }
}

@Composable
fun CropPanel() {
    Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
            Text("Crop & Rotate (Not Implemented)", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun ToolsPanel(onRemoveBackground: () -> Unit) {
    Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tools", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRemoveBackground) {
                Text("Remove Background")
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
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}

enum class EditorTool {
    NONE, AI_ENHANCE, FILTERS, CROP, TOOLS
}