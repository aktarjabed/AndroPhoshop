package com.aktarjabed.androphoshop.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aktarjabed.androphoshop.data.database.dao.ProjectDao
import com.aktarjabed.androphoshop.data.database.entities.ProjectEntity

@Database(
    entities = [ProjectEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
}