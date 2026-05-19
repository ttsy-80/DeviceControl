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
import com.devicecontrol.engine.data.database.dao.V2InspectionConfigDao
import com.devicecontrol.engine.data.model.ConfigItem
import com.devicecontrol.engine.data.model.EngineModel
import com.devicecontrol.engine.data.model.Task
import com.devicecontrol.engine.data.model.TaskExecution
import com.devicecontrol.engine.data.model.TaskRecord
import com.devicecontrol.engine.data.model.V2AutoInspectionConfig
import com.devicecontrol.engine.data.model.V2InspectionPrefs
import com.devicecontrol.engine.data.model.V2ManualInspectionConfig

@Database(
    entities = [
        EngineModel::class,
        ConfigItem::class,
        Task::class,
        TaskExecution::class,
        TaskRecord::class,
        V2AutoInspectionConfig::class,
        V2ManualInspectionConfig::class,
        V2InspectionPrefs::class,
    ],
    version = 13,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun engineDao(): EngineDao
    abstract fun taskDao(): TaskDao
    abstract fun v2InspectionConfigDao(): V2InspectionConfigDao
    
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

        // 版本 8→9：V2 检测模式参数表；tasks 增加来源标记
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE tasks ADD COLUMN source TEXT NOT NULL DEFAULT ''",
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS v2_mode_settings (
                        modelId INTEGER NOT NULL,
                        manual INTEGER NOT NULL,
                        settingKey TEXT NOT NULL,
                        value REAL NOT NULL,
                        PRIMARY KEY(modelId, manual, settingKey)
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_v2_mode_settings_modelId ON v2_mode_settings(modelId)",
                )
            }
        }

        // 版本 9→10：键值表改为自动/手动配置实体表 + 上次 UI 模式
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS v2_auto_inspection_configs (
                        modelId INTEGER PRIMARY KEY NOT NULL,
                        autoJogAngle REAL NOT NULL DEFAULT 1.0,
                        baseJogSpeed REAL NOT NULL DEFAULT 1.0,
                        autoContinuous REAL NOT NULL DEFAULT 0.1,
                        reverseSpeed REAL NOT NULL DEFAULT 0.1,
                        autoSpeedStep REAL NOT NULL DEFAULT 0.1,
                        jogHoldSec REAL NOT NULL DEFAULT 2.0,
                        autoTurns REAL NOT NULL DEFAULT 2.0
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS v2_manual_inspection_configs (
                        modelId INTEGER PRIMARY KEY NOT NULL,
                        manualJogAngle REAL NOT NULL DEFAULT 0.1,
                        manualContinuous REAL NOT NULL DEFAULT 0.1,
                        manualSpeedStep REAL NOT NULL DEFAULT 0.1
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS v2_inspection_prefs (
                        modelId INTEGER PRIMARY KEY NOT NULL,
                        lastUiMode TEXT NOT NULL DEFAULT 'AUTO'
                    )
                    """.trimIndent(),
                )
                migrateLegacyModeSettings(database)
                database.execSQL("DROP TABLE IF EXISTS v2_mode_settings")
            }

            private fun migrateLegacyModeSettings(database: SupportSQLiteDatabase) {
                val hasLegacy = database.query(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='v2_mode_settings'",
                ).use { it.count > 0 }
                if (!hasLegacy) return

                database.execSQL(
                    """
                    INSERT OR REPLACE INTO v2_auto_inspection_configs (
                        modelId, autoJogAngle, baseJogSpeed, autoContinuous,
                        reverseSpeed, autoSpeedStep, jogHoldSec, autoTurns
                    )
                    SELECT DISTINCT modelId,
                        COALESCE((SELECT value FROM v2_mode_settings s
                            WHERE s.modelId = m.modelId AND s.manual = 0 AND s.settingKey = 'auto_jog_angle' LIMIT 1), 0.1),
                        COALESCE((SELECT value FROM v2_mode_settings s
                            WHERE s.modelId = m.modelId AND s.manual = 0 AND s.settingKey = 'base_jog_speed' LIMIT 1), 0.1),
                        COALESCE((SELECT value FROM v2_mode_settings s
                            WHERE s.modelId = m.modelId AND s.manual = 0 AND s.settingKey = 'auto_continuous' LIMIT 1), 0.1),
                        COALESCE((SELECT value FROM v2_mode_settings s
                            WHERE s.modelId = m.modelId AND s.manual = 0 AND s.settingKey = 'reverse_speed' LIMIT 1), 0.1),
                        COALESCE((SELECT value FROM v2_mode_settings s
                            WHERE s.modelId = m.modelId AND s.manual = 0 AND s.settingKey = 'auto_speed_step' LIMIT 1), 0.1),
                        COALESCE((SELECT value FROM v2_mode_settings s
                            WHERE s.modelId = m.modelId AND s.manual = 0 AND s.settingKey = 'jog_hold' LIMIT 1), 2.0),
                        COALESCE((SELECT value FROM v2_mode_settings s
                            WHERE s.modelId = m.modelId AND s.manual = 0 AND s.settingKey = 'auto_turns' LIMIT 1), 2.0)
                    FROM (SELECT DISTINCT modelId FROM v2_mode_settings WHERE manual = 0) m
                    """.trimIndent(),
                )
                database.execSQL(
                    """
                    INSERT OR REPLACE INTO v2_manual_inspection_configs (
                        modelId, manualJogAngle, manualContinuous, manualSpeedStep
                    )
                    SELECT DISTINCT modelId,
                        COALESCE((SELECT value FROM v2_mode_settings s
                            WHERE s.modelId = m.modelId AND s.manual = 1 AND s.settingKey = 'manual_jog_angle' LIMIT 1), 0.1),
                        COALESCE((SELECT value FROM v2_mode_settings s
                            WHERE s.modelId = m.modelId AND s.manual = 1 AND s.settingKey = 'manual_continuous' LIMIT 1), 0.1),
                        COALESCE((SELECT value FROM v2_mode_settings s
                            WHERE s.modelId = m.modelId AND s.manual = 1 AND s.settingKey = 'manual_speed_step' LIMIT 1), 0.1)
                    FROM (SELECT DISTINCT modelId FROM v2_mode_settings WHERE manual = 1) m
                    """.trimIndent(),
                )
            }
        }

        // 版本 10→11：基础点动速度改为秒/度（纠正曾按度/分钟存的值）
        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    UPDATE v2_auto_inspection_configs
                    SET baseJogSpeed = 60.0 / baseJogSpeed
                    WHERE baseJogSpeed >= 30.0 AND baseJogSpeed <= 120.0
                    """.trimIndent(),
                )
                database.execSQL(
                    """
                    UPDATE v2_auto_inspection_configs
                    SET baseJogSpeed = 1.0
                    WHERE baseJogSpeed < 1.0 OR baseJogSpeed > 120.0
                    """.trimIndent(),
                )
            }
        }

        // 版本 11→12：新增自动点动速度(°/s)；恢复基础点动速度(°/分钟)
        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE v2_auto_inspection_configs ADD COLUMN autoJogSpeed REAL NOT NULL DEFAULT 1.0",
                )
                // 纠正 v11 误将 baseJogSpeed 按 60/原值 换算后的 1～5 区间
                database.execSQL(
                    """
                    UPDATE v2_auto_inspection_configs
                    SET baseJogSpeed = 60.0 / baseJogSpeed
                    WHERE baseJogSpeed >= 0.5 AND baseJogSpeed <= 10.0
                    """.trimIndent(),
                )
                database.execSQL(
                    """
                    UPDATE v2_auto_inspection_configs
                    SET baseJogSpeed = 60.0
                    WHERE baseJogSpeed < 30.0 OR baseJogSpeed > 120.0
                    """.trimIndent(),
                )
            }
        }

        // 版本 12→13：移除误加的 autoJogSpeed 列
        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS v2_auto_inspection_configs_new (
                        modelId INTEGER PRIMARY KEY NOT NULL,
                        autoJogAngle REAL NOT NULL DEFAULT 1.0,
                        baseJogSpeed REAL NOT NULL DEFAULT 60.0,
                        autoContinuous REAL NOT NULL DEFAULT 0.1,
                        reverseSpeed REAL NOT NULL DEFAULT 0.1,
                        autoSpeedStep REAL NOT NULL DEFAULT 0.1,
                        jogHoldSec REAL NOT NULL DEFAULT 2.0,
                        autoTurns REAL NOT NULL DEFAULT 2.0
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    """
                    INSERT INTO v2_auto_inspection_configs_new (
                        modelId, autoJogAngle, baseJogSpeed, autoContinuous,
                        reverseSpeed, autoSpeedStep, jogHoldSec, autoTurns
                    )
                    SELECT
                        modelId, autoJogAngle, baseJogSpeed, autoContinuous,
                        reverseSpeed, autoSpeedStep, jogHoldSec, autoTurns
                    FROM v2_auto_inspection_configs
                    """.trimIndent(),
                )
                database.execSQL("DROP TABLE v2_auto_inspection_configs")
                database.execSQL(
                    "ALTER TABLE v2_auto_inspection_configs_new RENAME TO v2_auto_inspection_configs",
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
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12,
                        MIGRATION_12_13,
                    )
                    .fallbackToDestructiveMigration() // 仅在开发阶段使用，生产环境应移除
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
