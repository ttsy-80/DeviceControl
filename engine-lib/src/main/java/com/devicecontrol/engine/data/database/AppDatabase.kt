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
    version = 8,  // 版本升级：engine_models 增加 safeTorque、imagePath（2.0 型号页）
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
        
        // 数据库迁移：从版本5升级到版本6，添加TaskExecution回溯速度字段
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加回溯速度字段，使用默认值1.0秒/圈
                database.execSQL("ALTER TABLE task_executions ADD COLUMN playbackSpeed REAL NOT NULL DEFAULT 1.0")
            }
        }

        // 数据库迁移：从版本6升级到版本7，task_records 增加角度（度）
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE task_records ADD COLUMN angleDegrees REAL NOT NULL DEFAULT 0"
                )
                // 与既有 position（0.1° 整数）对齐：度 = position / 10
                database.execSQL(
                    "UPDATE task_records SET angleDegrees = CAST(position AS REAL) / 10.0"
                )
            }
        }
        
        // 版本 7→8：engine_models 增加 2.0 型号页字段
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE engine_models ADD COLUMN safeTorque TEXT NOT NULL DEFAULT ''",
                )
                database.execSQL(
                    "ALTER TABLE engine_models ADD COLUMN imagePath TEXT",
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "engine_control_database"
                )
                    .addMigrations(
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                    )
                    .fallbackToDestructiveMigration() // 仅在开发阶段使用，生产环境应移除
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
