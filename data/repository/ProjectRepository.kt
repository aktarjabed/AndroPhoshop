package com.aktarjabed.androphoshop.data.repository

import com.aktarjabed.androphoshop.data.model.Project
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun getRecentProjects(): Flow<List<Project>>
}