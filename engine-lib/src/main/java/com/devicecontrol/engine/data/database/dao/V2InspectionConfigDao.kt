package com.devicecontrol.engine.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.devicecontrol.engine.data.model.V2AutoInspectionConfig
import com.devicecontrol.engine.data.model.V2InspectionPrefs
import com.devicecontrol.engine.data.model.V2ManualInspectionConfig

@Dao
interface V2InspectionConfigDao {

    @Query("SELECT * FROM v2_auto_inspection_configs WHERE modelId = :modelId LIMIT 1")
    suspend fun getAutoConfig(modelId: Long): V2AutoInspectionConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAutoConfig(config: V2AutoInspectionConfig)

    @Query("SELECT * FROM v2_manual_inspection_configs WHERE modelId = :modelId LIMIT 1")
    suspend fun getManualConfig(modelId: Long): V2ManualInspectionConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertManualConfig(config: V2ManualInspectionConfig)

    @Query("SELECT * FROM v2_inspection_prefs WHERE modelId = :modelId LIMIT 1")
    suspend fun getPrefs(modelId: Long): V2InspectionPrefs?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPrefs(prefs: V2InspectionPrefs)
}
