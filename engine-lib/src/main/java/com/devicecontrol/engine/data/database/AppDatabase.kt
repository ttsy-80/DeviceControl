package com.devicecontrol.engine.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.devicecontrol.engine.data.database.dao.EngineDao
import com.devicecontrol.engine.data.database.dao.TaskDao
import com.devicecontrol.engine.data.model.ConfigItem
import com.devicecontrol.engine.data.model.EngineModel
import com.devicecontrol.engine.data.model.Task
import com.devicecontrol.engine.data.model.TaskExecution

@Database(
    entities = [EngineModel::class, ConfigItem::class, Task::class, TaskExecution::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun engineDao(): EngineDao
    abstract fun taskDao(): TaskDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "engine_control_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
