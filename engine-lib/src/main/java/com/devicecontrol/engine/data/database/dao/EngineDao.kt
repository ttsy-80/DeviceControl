package com.devicecontrol.engine.data.database.dao

import androidx.room.*
import com.devicecontrol.engine.data.model.ConfigItem
import com.devicecontrol.engine.data.model.EngineModel
import com.devicecontrol.engine.data.model.EngineModelWithConfigItems
import kotlinx.coroutines.flow.Flow

@Dao
interface EngineDao {
    
    @Query("SELECT * FROM engine_models ORDER BY name ASC")
    fun getAllModels(): Flow<List<EngineModel>>
    
    @Query("SELECT * FROM engine_models WHERE id = :modelId")
    suspend fun getModelById(modelId: Long): EngineModel?
    
    @Query("SELECT * FROM engine_models WHERE name = :name")
    suspend fun getModelByName(name: String): EngineModel?
    
    @Transaction
    @Query("SELECT * FROM engine_models ORDER BY name ASC")
    fun getAllModelsWithConfigItems(): Flow<List<EngineModelWithConfigItems>>
    
    @Transaction
    @Query("SELECT * FROM engine_models WHERE id = :modelId")
    suspend fun getModelWithConfigItemsById(modelId: Long): EngineModelWithConfigItems?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModel(model: EngineModel): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfigItem(configItem: ConfigItem): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfigItems(configItems: List<ConfigItem>)
    
    @Query("SELECT * FROM config_items WHERE modelId = :modelId ORDER BY gearRatio ASC")
    suspend fun getConfigItemsByModelId(modelId: Long): List<ConfigItem>
    
    @Query("SELECT * FROM config_items WHERE modelId = :modelId ORDER BY gearRatio ASC")
    fun getConfigItemsByModelIdFlow(modelId: Long): Flow<List<ConfigItem>>
    
    @Query("SELECT COUNT(*) FROM config_items WHERE modelId = :modelId")
    suspend fun getConfigItemCount(modelId: Long): Int
    
    @Delete
    suspend fun deleteModel(model: EngineModel)
    
    @Delete
    suspend fun deleteConfigItem(configItem: ConfigItem)
    
    @Update
    suspend fun updateConfigItem(configItem: ConfigItem)
    
    @Query("UPDATE config_items SET gearRatio = :newGearRatio WHERE modelId = :modelId")
    suspend fun updateAllConfigItemsGearRatioByModelId(modelId: Long, newGearRatio: Double)
    
    @Query("DELETE FROM config_items WHERE id = :configItemId")
    suspend fun deleteConfigItemById(configItemId: Long)
    
    @Query("DELETE FROM engine_models WHERE id = :modelId")
    suspend fun deleteModelById(modelId: Long)
}
