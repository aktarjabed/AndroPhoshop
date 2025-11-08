package com.androphoshop.domain.use_cases

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import com.androphoshop.core.utils.Result
import com.androphoshop.domain.repository.ImageRepository
import com.androphoshop.domain.repository.ProjectRepository
import com.androphoshop.features.ai.AIEnhancementEngine
import com.androphoshop.features.ai.BackgroundRemover
import com.androphoshop.features.editor.processors.CropProcessor
import com.androphoshop.features.editor.processors.FilterProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class EditorUseCases @Inject constructor(
    val loadImage: LoadImageUseCase,
    val saveImage: SaveImageUseCase,
    val createProject: CreateProjectUseCase,
    val updateProject: UpdateProjectUseCase,
    val getProjects: GetProjectsUseCase,
    val enhanceImage: EnhanceImageUseCase,
    val removeBackground: RemoveBackgroundUseCase,
    val applyFilter: ApplyFilterUseCase,
    val cropImage: CropImageUseCase
)

class LoadImageUseCase @Inject constructor(
    private val repository: ImageRepository
) {
    suspend operator fun invoke(uri: String): Result<Bitmap> {
        return repository.loadBitmapFromUri(Uri.parse(uri))
    }
}

class SaveImageUseCase @Inject constructor(
    private val repository: ImageRepository
) {
    suspend operator fun invoke(bitmap: Bitmap): Result<Uri> {
        val filename = "edited_${System.currentTimeMillis()}"
        return repository.saveBitmapToGallery(bitmap, filename)
    }
}

class EnhanceImageUseCase @Inject constructor(
    private val aiEngine: AIEnhancementEngine
) {
    suspend operator fun invoke(
        bitmap: Bitmap,
        type: AIEnhancementEngine.EnhancementType
    ): Result<AIEnhancementEngine.EnhancementResult> = withContext(Dispatchers.IO) {
        aiEngine.enhanceImage(bitmap, type)
    }
}

class RemoveBackgroundUseCase @Inject constructor(
    private val backgroundRemover: BackgroundRemover
) {
    suspend operator fun invoke(bitmap: Bitmap): Result<BackgroundRemover.RemovalResult> = withContext(Dispatchers.IO) {
        backgroundRemover.removeBackground(bitmap)
    }
}

class ApplyFilterUseCase @Inject constructor(
    private val filterProcessor: FilterProcessor
) {
    suspend operator fun invoke(bitmap: Bitmap, filterType: String): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            val result = filterProcessor.applyFilter(bitmap, filterType)
            Result.Success(result ?: bitmap)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class CropImageUseCase @Inject constructor(
    private val cropProcessor: CropProcessor
) {
    suspend operator fun invoke(
        bitmap: Bitmap,
        rect: Rect
    ): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            val result = cropProcessor.crop(bitmap, rect)
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class CreateProjectUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(name: String, imageUri: String): Result<Unit> {
        return try {
            // Implementation would create a project entity and save it
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class UpdateProjectUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(projectId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            // Implementation would update project
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetProjectsUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    operator fun invoke() = repository.getProjects()
}