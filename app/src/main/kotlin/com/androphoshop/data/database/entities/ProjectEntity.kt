package com.androphoshop.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imageUri: String,
    val lastModified: Long = System.currentTimeMillis()
)