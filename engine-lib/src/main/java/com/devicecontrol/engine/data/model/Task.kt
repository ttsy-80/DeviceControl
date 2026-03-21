package com.devicecontrol.engine.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.devicecontrol.engine.data.database.Converters

/**
 * 任务实体。
 *
 * 表示基于某机型创建的一条任务，通过 [configItemIds] 引用该机型下的若干 [ConfigItem]，
 * 用于区分「变速比列表」等业务含义（存 ID 列表而非冗余变速比数值）。
 * 对应 Room 表 `tasks`；[List] 类型由 [Converters] 持久化。
 */
@Entity(tableName = "tasks")
@TypeConverters(Converters::class)
data class Task(
    /** 主键，自增 */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 关联机型 ID */
    val modelId: Long,
    /** 机型名称快照，便于展示与历史追溯 */
    val modelName: String,
    /** 本任务选用的配置项 ID 列表（对应 [ConfigItem.id]） */
    val configItemIds: List<Long>
)
