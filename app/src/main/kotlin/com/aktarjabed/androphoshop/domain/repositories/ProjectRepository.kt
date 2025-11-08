package com.aktarjabed.androphoshop.domain.repository

import com.aktarjabed.androphoshop.data.database.entities.ProjectEntity
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun getProjects(): Flow<List<ProjectEntity>>
    suspend fun createProject(entity: ProjectEntity)
    suspend fun updateProject(entity: ProjectEntity)
    suspend fun deleteProject(id: String)
}