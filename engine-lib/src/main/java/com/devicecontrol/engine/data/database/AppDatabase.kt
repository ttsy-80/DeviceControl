package com.devicecontrol.engine.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.devicecontrol.engine.data.database.dao.EngineDao
import com.devicecontrol.engine.data.database.dao.TaskDao
import com.devicecontrol.engine.data.model.ConfigItem
import com.devicecontrol.engine.data.model.EngineModel
import com.devicecontrol.engine.data.model.Task
import com.devicecontrol.engine.data.model.TaskExecution
import com.devicecontrol.engine.data.model.TaskRecord

@Database(
    entities = [EngineModel::class, ConfigItem::class, Task::class, TaskExecution::class, TaskRecord::class],
    version = 5,  // 版本升级：添加TaskExecution配置字段
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun engineDao(): EngineDao
    abstract fun taskDao(): TaskDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        // 数据库迁移：从版本3升级到版本4，添加task_records表
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建task_records表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS task_records (
                        recordId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        taskId INTEGER NOT NULL,
                        gearRatioIndex INTEGER NOT NULL,
                        recordNumber INTEGER NOT NULL,
                        position INTEGER NOT NULL,
                        bladeNumber INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // 创建索引
                database.execSQL("CREATE INDEX IF NOT EXISTS index_task_records_taskId_gearRatioIndex ON task_records(taskId, gearRatioIndex)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_task_records_recordId ON task_records(recordId)")
            }
        }
        
        // 数据库迁移：从版本4升级到版本5，添加TaskExecution配置字段
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加配置字段，使用默认值
                database.execSQL("ALTER TABLE task_executions ADD COLUMN speedStep REAL NOT NULL DEFAULT 1.0")
                database.execSQL("ALTER TABLE task_executions ADD COLUMN continuousCycles INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE task_executions ADD COLUMN jogInterval INTEGER NOT NULL DEFAULT 1")
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "engine_control_database"
                )
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration() // 仅在开发阶段使用，生产环境应移除
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
