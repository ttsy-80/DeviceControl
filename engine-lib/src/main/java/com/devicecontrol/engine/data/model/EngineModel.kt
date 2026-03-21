package com.devicecontrol.engine.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 发动机/机型实体。
 *
 * 表示一种设备型号，其下可挂多条 [ConfigItem]（变速比、位置、叶片数等配置）。
 * 对应 Room 表 `engine_models`。
 */
@Entity(tableName = "engine_models")
data class EngineModel(
    /** 主键，自增 */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 机型显示名称 */
    val name: String
)
