package com.devicecontrol.engine.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 配置项实体（变速比档位/子任务维度配置）。
 *
 * 隶属于某个 [EngineModel]；删除机型时级联删除其下所有配置项。
 * 对应 Room 表 `config_items`。
 */
@Entity(
    tableName = "config_items",
    foreignKeys = [
        ForeignKey(
            entity = EngineModel::class,
            parentColumns = ["id"],
            childColumns = ["modelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["modelId"])]
)
data class ConfigItem(
    /** 主键，自增 */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 所属机型 ID，外键指向 [EngineModel.id] */
    val modelId: Long,
    /** 变速比 */
    val gearRatio: Double,
    /** 位置描述或位置参数（字符串形式，由业务约定） */
    val position: String,
    /** 叶片数 */
    val bladeCount: Int,
    /** 点动次数相关配置 no use */
    val jogCount: Int
)
