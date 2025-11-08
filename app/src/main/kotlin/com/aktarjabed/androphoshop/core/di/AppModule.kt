package com.aktarjabed.androphoshop.core.di

import android.content.Context
import androidx.room.Room
import com.aktarjabed.androphoshop.data.database.AppDatabase
import com.aktarjabed.androphoshop.data.database.dao.ProjectDao
import com.aktarjabed.androphoshop.data.repository.ImageRepositoryImpl
import com.aktarjabed.androphoshop.data.repository.ProjectRepositoryImpl
import com.aktarjabed.androphoshop.domain.repository.ImageRepository
import com.aktarjabed.androphoshop.domain.repository.ProjectRepository
import com.aktarjabed.androphoshop.domain.use_cases.*
import com.aktarjabed.androphoshop.features.ai.AIEnhancementEngine
import com.aktarjabed.androphoshop.features.ai.BackgroundRemover
import com.aktarjabed.androphoshop.features.editor.processors.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "androphoshop.db"
        ).fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideProjectDao(database: AppDatabase): ProjectDao {
        return database.projectDao()
    }

    @Provides
    @Singleton
    fun provideProjectRepository(projectDao: ProjectDao): ProjectRepository {
        return ProjectRepositoryImpl(projectDao)
    }

    @Provides
    @Singleton
    fun provideImageRepository(@ApplicationContext context: Context): ImageRepository {
        return ImageRepositoryImpl(context, context.contentResolver)
    }

    @Provides
    @Singleton
    fun provideAIEnhancementEngine() = AIEnhancementEngine()

    @Provides
    @Singleton
    fun provideBackgroundRemover() = BackgroundRemover()

    @Provides
    @Singleton
    fun provideBackgroundRemoverMlKit() = BackgroundRemoverMlKit()

    @Provides
    @Singleton
    fun provideFilterProcessor(): FilterProcessor {
        return FilterProcessor()
    }

    @Provides
    @Singleton
    fun provideCropProcessor(): CropProcessor {
        return CropProcessor()
    }

    @Provides
    @Singleton
    fun provideBackgroundReplaceEngine() = BackgroundReplaceEngine()

    @Provides
    @Singleton
    fun provideRelightEngine() = RelightEngine()

    @Provides
    @Singleton
    fun provideBlendModeCompositor() = BlendModeCompositor

    @Provides
    @Singleton
    fun provideEditorUseCases(
        imageRepository: ImageRepository,
        projectRepository: ProjectRepository,
        aiEnhancementEngine: AIEnhancementEngine,
        backgroundRemover: BackgroundRemover,
        backgroundRemoverMlKit: BackgroundRemoverMlKit,
        filterProcessor: FilterProcessor,
        cropProcessor: CropProcessor
    ): EditorUseCases {
        return EditorUseCases(
            loadImage = LoadImageUseCase(imageRepository),
            saveImage = SaveImageUseCase(imageRepository),
            createProject = CreateProjectUseCase(projectRepository),
            updateProject = UpdateProjectUseCase(projectRepository),
            getProjects = GetProjectsUseCase(projectRepository),
            enhanceImage = EnhanceImageUseCase(aiEnhancementEngine),
            removeBackground = RemoveBackgroundUseCase(backgroundRemover),
            removeBackgroundMlKit = RemoveBackgroundMlKitUseCase(backgroundRemoverMlKit),
            applyFilter = ApplyFilterUseCase(filterProcessor),
            cropImage = CropImageUseCase(cropProcessor)
        )
    }

    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}