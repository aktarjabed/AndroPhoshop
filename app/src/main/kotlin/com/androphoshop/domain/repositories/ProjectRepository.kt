package com.androphoshop.domain.repository

import com.androphoshop.data.database.entities.ProjectEntity
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun getProjects(): Flow<List<ProjectEntity>>
    suspend fun createProject(entity: ProjectEntity)
    suspend fun updateProject(entity: ProjectEntity)
    suspend fun deleteProject(id: String)
}