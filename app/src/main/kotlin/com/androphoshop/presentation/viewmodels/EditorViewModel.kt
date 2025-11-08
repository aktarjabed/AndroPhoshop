package com.androphoshop.presentation.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androphoshop.core.constants.Constants
import com.androphoshop.core.utils.Result
import com.androphoshop.domain.use_cases.EditorUseCases
import com.androphoshop.features.ai.AIEnhancementEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val editorUseCases: EditorUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private var currentImageJob: Job? = null

    fun loadImage(imageUri: String) {
        currentImageJob?.cancel()
        currentImageJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = editorUseCases.loadImage(imageUri)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        originalBitmap = result.data,
                        currentBitmap = result.data,
                        isLoading = false
                    )
                    addToHistory(result.data)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.exception.message ?: "Failed to load image",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun enhanceImage(type: AIEnhancementEngine.EnhancementType) {
        val currentBitmap = _uiState.value.currentBitmap ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = editorUseCases.enhanceImage(currentBitmap, type)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        currentBitmap = result.data.enhancedBitmap,
                        isLoading = false,
                        lastOperation = "AI Enhancement applied"
                    )
                    addToHistory(result.data.enhancedBitmap)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.exception.message ?: "Enhancement failed",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun removeBackground() {
        val currentBitmap = _uiState.value.currentBitmap ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = editorUseCases.removeBackground(currentBitmap)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        currentBitmap = result.data.resultBitmap,
                        isLoading = false,
                        lastOperation = "Background removed"
                    )
                    addToHistory(result.data.resultBitmap)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.exception.message ?: "Background removal failed",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun applyFilter(filterType: String) {
        val currentBitmap = _uiState.value.currentBitmap ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = editorUseCases.applyFilter(currentBitmap, filterType)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        currentBitmap = result.data,
                        isLoading = false,
                        lastOperation = "Filter applied"
                    )
                    addToHistory(result.data)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.exception.message ?: "Filter application failed",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun saveImage() {
        val currentBitmap = _uiState.value.currentBitmap ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            when (val result = editorUseCases.saveImage(currentBitmap)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        lastOperation = "Image saved successfully to gallery"
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.exception.message ?: "Save failed",
                        isSaving = false
                    )
                }
            }
        }
    }

    fun undo() {
        if (_uiState.value.historyIndex > 0) {
            val newIndex = _uiState.value.historyIndex - 1
            _uiState.value = _uiState.value.copy(
                currentBitmap = _uiState.value.history[newIndex],
                historyIndex = newIndex,
                lastOperation = "Undo"
            )
        }
    }

    fun redo() {
        if (_uiState.value.historyIndex < _uiState.value.history.size - 1) {
            val newIndex = _uiState.value.historyIndex + 1
            _uiState.value = _uiState.value.copy(
                currentBitmap = _uiState.value.history[newIndex],
                historyIndex = newIndex,
                lastOperation = "Redo"
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearLastOperation() {
        _uiState.value = _uiState.value.copy(lastOperation = null)
    }

    private fun addToHistory(bitmap: Bitmap) {
        val currentHistory = _uiState.value.history.toMutableList()
        val currentIndex = _uiState.value.historyIndex

        if (currentIndex < currentHistory.size - 1) {
            currentHistory.subList(currentIndex + 1, currentHistory.size).clear()
        }

        currentHistory.add(bitmap)

        if (currentHistory.size > Constants.MAX_HISTORY_SIZE) {
            currentHistory.removeAt(0)
        }

        _uiState.value = _uiState.value.copy(
            history = currentHistory,
            historyIndex = currentHistory.size - 1
        )
    }

    override fun onCleared() {
        _uiState.value.history.forEach { if (!it.isRecycled) it.recycle() }
        super.onCleared()
    }
}

data class EditorUiState(
    val originalBitmap: Bitmap? = null,
    val currentBitmap: Bitmap? = null,
    val history: List<Bitmap> = emptyList(),
    val historyIndex: Int = -1,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val lastOperation: String? = null
)