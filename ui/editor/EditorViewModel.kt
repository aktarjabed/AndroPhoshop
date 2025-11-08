package com.aktarjabed.androphoshop.ui.editor

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aktarjabed.androphoshop.data.repository.ImageProcessor
import com.aktarjabed.androphoshop.ui.editor.state.EditorTool
import com.aktarjabed.androphoshop.ui.editor.state.RotationState
import com.aktarjabed.androphoshop.ui.editor.state.ZoomState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val imageProcessor: ImageProcessor
) : ViewModel() {

    private val _currentTool = MutableStateFlow<EditorTool>(EditorTool.Select)
    val currentTool = _currentTool.asStateFlow()

    private val _currentImage = MutableStateFlow<ImageBitmap?>(null)
    val currentImage = _currentImage.asStateFlow()

    private val _zoomState = MutableStateFlow(ZoomState(1f, Offset.Zero))
    val zoomState = _zoomState.asStateFlow()

    private val _rotationState = MutableStateFlow(RotationState(0f))
    val rotationState = _rotationState.asStateFlow()

    private val _showBeforeAfter = MutableStateFlow(false)
    val showBeforeAfter = _showBeforeAfter.asStateFlow()

    private val _isAIProcessing = MutableStateFlow(false)
    val isAIProcessing = _isAIProcessing.asStateFlow()

    private val _aiProgress = MutableStateFlow(0f)
    val aiProgress = _aiProgress.asStateFlow()

    private val _canUndo = MutableStateFlow(false)
    val canUndo = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo = _canRedo.asStateFlow()

    // Adjustments
    private val _brightness = MutableStateFlow(0f)
    val brightness = _brightness.asStateFlow()
    private val _contrast = MutableStateFlow(1f)
    val contrast = _contrast.asStateFlow()
    private val _saturation = MutableStateFlow(1f)
    val saturation = _saturation.asStateFlow()

    // Brush & Text
    private val _brushSize = MutableStateFlow(24f)
    val brushSize = _brushSize.asStateFlow()
    private val _textContent = MutableStateFlow("")
    val textContent = _textContent.asStateFlow()
    private val _textSize = MutableStateFlow(48f)
    val textSize = _textSize.asStateFlow()

    // Filters
    data class UiFilter(val id: String, val displayName: String)
    val availableFilters = listOf(
        UiFilter("none","None"),
        UiFilter("gray","Grayscale"),
        UiFilter("sepia","Sepia"),
        UiFilter("invert","Invert"),
        UiFilter("vignette","Vignette"),
        UiFilter("blur","Blur")
    )

    fun setCurrentTool(t: EditorTool) { _currentTool.value = t }
    fun updateZoom(z: ZoomState) { _zoomState.value = z }
    fun updateRotation(r: RotationState) { _rotationState.value = r }
    fun toggleBeforeAfter() { _showBeforeAfter.value = !_showBeforeAfter.value }
    fun undo() { /* hook to your history */ }
    fun redo() { /* hook to your history */ }
    fun cancelAI() { /* set _isAIProcessing=false if cancellable */ }
    fun onCanvasTapped(p: Offset) { /* tap-to-focus for ML cutout, etc. */ }

    // Adjust panel
    fun setBrightness(v: Float) { _brightness.value = v }
    fun setContrast(v: Float) { _contrast.value = v }
    fun setSaturation(v: Float) { _saturation.value = v }
    fun resetAdjustments() {
        _brightness.value = 0f; _contrast.value = 1f; _saturation.value = 1f
    }
    fun applyAdjustmentsNow() {
        // call your ImageRepository.applyAdjustments(...) then update _currentImage
    }

    // Brush panel
    fun setBrushSize(px: Float) { _brushSize.value = px }
    fun setBrushType(type: String) { /* NORMAL / SOFT / ERASER -> your processor */ }

    // Text panel
    fun setText(t: String) { _textContent.value = t }
    fun setTextSize(sz: Float) { _textSize.value = sz }
    fun addTextLayer() { /* add to your LayerManager; re-render to _currentImage if needed */ }

    // Shapes
    fun addShape(kind: String) { /* add layer */ }

    // Crop
    fun cropAspect(w: Float, h: Float) { /* set crop box */ }
    fun applyCrop() { /* apply crop & update _currentImage */ }

    // Filters
    fun applyFilter(f: UiFilter) {
        // map id → your FilterProcessor.FilterType and update _currentImage
    }

    // AI tools
    fun aiEnhance() = launchAI("Enhancing image…") {
        // imageProcessor.autoEnhance(_currentImage.value)?.let {
        //     _currentImage.value = it
        // }
    }

    fun aiRemoveBackground() = launchAI("Removing background…") {
        // imageProcessor.removeBackground(_currentImage.value)?.let {
        //     _currentImage.value = it
        // }
    }

    fun aiReplaceBackground() = launchAI("Replacing background…") {
        // imageProcessor.replaceBackground(_currentImage.value)?.let {
        //     _currentImage.value = it
        // }
    }

    fun aiRelight() = launchAI("Relighting scene…") {
        // imageProcessor.relight(_currentImage.value)?.let {
        //     _currentImage.value = it
        // }
    }

    fun aiUpscale() = launchAI("Upscaling image…") {
        // imageProcessor.upscale(_currentImage.value)?.let {
        //     _currentImage.value = it
        // }
    }

    fun aiColorize() = launchAI("Colorizing…") {
        // imageProcessor.colorize(_currentImage.value)?.let {
        //     _currentImage.value = it
        // }
    }

    // helper
    private fun launchAI(task: String, action: suspend () -> Unit) {
        viewModelScope.launch {
            _isAIProcessing.value = true
            _aiProgress.value = 0f
            try {
                repeat(10) { i ->
                    delay(120)
                    _aiProgress.value = (i + 1) / 10f
                }
                action()
            } catch (e: Exception) {
                Log.e("AndroPhoshop", "AI error: ${e.message}")
            } finally {
                _isAIProcessing.value = false
                _aiProgress.value = 0f
            }
        }
    }

    fun exportImage(settings: com.aktarjabed.androphoshop.ui.components.ExportSettings) {
        viewModelScope.launch {
            try {
                // val bitmap = _currentImage.value ?: return@launch
                // val output = imageProcessor.exportBitmap(
                //     bitmap,
                //     format = settings.format,
                //     quality = settings.quality,
                //     scale = settings.scale,
                //     useAIUpscale = settings.useUpscale
                // )
                // save file using repository, update state
            } catch (e: Exception) {
                Log.e("AndroPhoshop", "Export failed: ${e.message}")
            }
        }
    }
}