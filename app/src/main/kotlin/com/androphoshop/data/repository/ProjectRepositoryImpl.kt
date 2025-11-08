package com.androphoshop.data.repository

import com.androphoshop.data.database.dao.ProjectDao
import com.androphoshop.data.database.entities.ProjectEntity
import com.androphoshop.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepositoryImpl @Inject constructor(
    private val dao: ProjectDao
) : ProjectRepository {

    override fun getProjects(): Flow<List<ProjectEntity>> = dao.getProjects()

    override suspend fun createProject(entity: ProjectEntity) {
        dao.insertProject(entity)
    }

    override suspend fun updateProject(entity: ProjectEntity) {
        dao.updateProject(entity)
    }

    override suspend fun deleteProject(id: String) {
        dao.deleteProjectById(id)
    }
}