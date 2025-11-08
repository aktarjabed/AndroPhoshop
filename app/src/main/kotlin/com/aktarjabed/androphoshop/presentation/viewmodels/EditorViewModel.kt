package com.aktarjabed.androphoshop.presentation.viewmodels

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PointF
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aktarjabed.androphoshop.domain.use_cases.EditorUseCases
import com.aktarjabed.androphoshop.features.ai.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val editorUseCases: EditorUseCases,
    private val bgRemoverMlKit: BackgroundRemoverMlKit,
    private val bgReplaceEngine: BackgroundReplaceEngine,
    private val relightEngine: RelightEngine,
    private val blendCompositor: BlendModeCompositor
) : ViewModel() {

    var originalBitmap by mutableStateOf<Bitmap?>(null)
    var editedBitmap by mutableStateOf<Bitmap?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // Background replacement state
    var replaceParams by mutableStateOf(BackgroundReplaceEngine.Params())
    var backgroundBitmap by mutableStateOf<Bitmap?>(null)
    val lastTouch = MutableStateFlow<PointF?>(null)

    // Transform state
    var subjectScale by mutableStateOf(1f)
    var subjectRotation by mutableStateOf(0f)
    var subjectOffsetX by mutableStateOf(0f)
    var subjectOffsetY by mutableStateOf(0f)

    // Relight and Blend state
    var relightParams by mutableStateOf(RelightEngine.Params())
    var selectedBlendMode by mutableStateOf(BlendModeCompositor.Mode.NORMAL)

    // Batch processing state
    val batchProgress = MutableStateFlow(0f)
    val batchRunning = MutableStateFlow(false)
    val batchResults = MutableStateFlow<List<Uri>>(emptyList())

    fun loadImage(uri: Uri) = viewModelScope.launch {
        isLoading = true
        when (val result = editorUseCases.loadImage(uri.toString())) {
            is com.aktarjabed.androphoshop.core.utils.Result.Success -> {
                originalBitmap = result.data
                editedBitmap = result.data
            }
            is com.aktarjabed.androphoshop.core.utils.Result.Error -> {
                errorMessage = "Failed to load image"
            }
        }
        isLoading = false
    }

    fun loadBackgroundFromUri(uri: Uri) = viewModelScope.launch {
        isLoading = true
        when (val result = editorUseCases.loadImage(uri.toString())) {
            is com.aktarjabed.androphoshop.core.utils.Result.Success -> {
                backgroundBitmap = result.data
            }
            is com.aktarjabed.androphoshop.core.utils.Result.Error -> {
                errorMessage = "BG load failed"
            }
        }
        isLoading = false
    }

    fun enhanceAI(type: AIEnhancementEngine.EnhancementType) = viewModelScope.launch {
        editedBitmap?.let {
            isLoading = true
            when (val result = editorUseCases.enhanceImage(it, type)) {
                is com.aktarjabed.androphoshop.core.utils.Result.Success -> {
                    editedBitmap = result.data.enhancedBitmap
                }
                is com.aktarjabed.androphoshop.core.utils.Result.Error -> {
                    errorMessage = "AI Enhance failed"
                }
            }
            isLoading = false
        }
    }

    fun removeBackgroundAI() = viewModelScope.launch {
        editedBitmap?.let {
            isLoading = true
            when (val result = editorUseCases.removeBackground(it)) {
                is com.aktarjabed.androphoshop.core.utils.Result.Success -> {
                    editedBitmap = result.data.resultBitmap
                }
                is com.aktarjabed.androphoshop.core.utils.Result.Error -> {
                    errorMessage = "Background removal failed"
                }
            }
            isLoading = false
        }
    }

    fun createCutoutWithML() = viewModelScope.launch {
        val src = editedBitmap ?: originalBitmap ?: run { errorMessage = "No image loaded"; return@launch }
        isLoading = true
        errorMessage = null
        try {
            val cut = bgRemoverMlKit.removeBackground(src) // transparent BG
            editedBitmap = cut
        } catch (e: Exception) {
            errorMessage = "Cutout failed: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    fun replaceBackground() = viewModelScope.launch {
        val src = originalBitmap ?: editedBitmap ?: run { errorMessage = "No image loaded"; return@launch }
        val cut = editedBitmap ?: run {
            isLoading = true
            val tmp = bgRemoverMlKit.removeBackground(src)
            isLoading = false
            tmp
        }

        isLoading = true
        errorMessage = null
        try {
            val transformed = applySubjectTransform(cut, src.width, src.height)
            val bg = when (replaceParams.mode) {
                BackgroundReplaceEngine.Mode.IMAGE_FIT,
                BackgroundReplaceEngine.Mode.IMAGE_FILL -> backgroundBitmap
                else -> null
            }

            val base = withContext(Dispatchers.Default) {
                bgReplaceEngine.compose(original = src, cutout = transformed, background = bg, params = replaceParams)
            }

            val rimmed = relightEngine.addRimLight(transformed, relightParams)
            val final = BlendModeCompositor.composite(base, rimmed, selectedBlendMode)

            editedBitmap = final
        } catch (e: Exception) {
            errorMessage = "Replace failed: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    fun exportCutoutPng() = viewModelScope.launch {
        val bmp = editedBitmap ?: run { errorMessage = "Nothing to export"; return@launch }
        isLoading = true
        try {
            editorUseCases.saveImage(bmp)
        } catch (e: Exception) {
            errorMessage = "Export failed: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    fun batchCutoutAndExport(uris: List<Uri>) = viewModelScope.launch {
        if (uris.isEmpty()) return@launch
        batchRunning.value = true
        batchProgress.value = 0f
        val outUris = mutableListOf<Uri>()

        try {
            uris.forEachIndexed { idx, u ->
                when (val result = editorUseCases.loadImage(u.toString())) {
                    is com.aktarjabed.androphoshop.core.utils.Result.Success -> {
                        val src = result.data
                        val cut = bgRemoverMlKit.removeBackground(src)
                        when (val saveResult = editorUseCases.saveImage(cut)) {
                            is com.aktarjabed.androphoshop.core.utils.Result.Success -> {
                                outUris.add(saveResult.data)
                            }
                            is com.aktarjabed.androphoshop.core.utils.Result.Error -> {
                                // Handle save error if needed
                            }
                        }
                    }
                    is com.aktarjabed.androphoshop.core.utils.Result.Error -> {
                        // Handle load error if needed
                    }
                }
                batchProgress.value = (idx + 1).toFloat() / uris.size
            }
            batchResults.value = outUris
        } catch (e: Exception) {
            errorMessage = "Batch failed: ${e.localizedMessage}"
        } finally {
            batchRunning.value = false
        }
    }

    fun resetTransform() {
        subjectScale = 1f
        subjectRotation = 0f
        subjectOffsetX = 0f
        subjectOffsetY = 0f
    }

    private fun applySubjectTransform(subject: Bitmap, canvasW: Int, canvasH: Int): Bitmap {
        val out = Bitmap.createBitmap(canvasW, canvasH, Bitmap.Config.ARGB_8888)
        val c = android.graphics.Canvas(out)
        val cx = canvasW / 2f
        val cy = canvasH / 2f
        val m = android.graphics.Matrix().apply {
            postTranslate(-subject.width / 2f, -subject.height / 2f)
            postScale(subjectScale, subjectScale)
            postRotate(subjectRotation)
            postTranslate(cx + subjectOffsetX, cy + subjectOffsetY)
        }
        c.drawBitmap(subject, m, android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG))
        return out
    }
}