package com.devicecontrol.engine.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.devicecontrol.engine.data.model.V2ModeSetting

@Dao
interface V2ModeSettingDao {

    @Query("SELECT * FROM v2_mode_settings WHERE modelId = :modelId AND manual = :manual")
    suspend fun getByModel(modelId: Long, manual: Boolean): List<V2ModeSetting>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(settings: List<V2ModeSetting>)

    @Query("DELETE FROM v2_mode_settings WHERE modelId = :modelId AND manual = :manual")
    suspend fun deleteByModel(modelId: Long, manual: Boolean)
}
