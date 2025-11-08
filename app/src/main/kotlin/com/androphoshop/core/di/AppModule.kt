package com.androphoshop.core.di

import android.content.Context
import androidx.room.Room
import com.androphoshop.data.database.AppDatabase
import com.androphoshop.data.database.dao.ProjectDao
import com.androphoshop.data.repository.ImageRepositoryImpl
import com.androphoshop.data.repository.ProjectRepositoryImpl
import com.androphoshop.domain.repository.ImageRepository
import com.androphoshop.domain.repository.ProjectRepository
import com.androphoshop.domain.use_cases.*
import com.androphoshop.features.ai.AIEnhancementEngine
import com.androphoshop.features.ai.BackgroundRemover
import com.androphoshop.features.editor.processors.*
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
    fun provideAIEnhancementEngine(@ApplicationContext context: Context): AIEnhancementEngine {
        return AIEnhancementEngine(context)
    }

    @Provides
    @Singleton
    fun provideBackgroundRemover(@ApplicationContext context: Context): BackgroundRemover {
        return BackgroundRemover(context)
    }

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
    fun provideEditorUseCases(
        imageRepository: ImageRepository,
        projectRepository: ProjectRepository,
        aiEnhancementEngine: AIEnhancementEngine,
        backgroundRemover: BackgroundRemover,
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
            applyFilter = ApplyFilterUseCase(filterProcessor),
            cropImage = CropImageUseCase(cropProcessor)
        )
    }

    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}