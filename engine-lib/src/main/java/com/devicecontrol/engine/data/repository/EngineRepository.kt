package com.devicecontrol.engine.data.repository

import com.devicecontrol.engine.data.database.dao.EngineDao
import com.devicecontrol.engine.data.model.ConfigItem
import com.devicecontrol.engine.data.model.EngineModel
import com.devicecontrol.engine.data.model.EngineModelWithConfigItems
import kotlinx.coroutines.flow.Flow

class EngineRepository(private val engineDao: EngineDao) {
    
    fun getAllModelsWithConfigItems(): Flow<List<EngineModelWithConfigItems>> {
        return engineDao.getAllModelsWithConfigItems()
    }
    
    suspend fun getModelWithConfigItemsById(modelId: Long): EngineModelWithConfigItems? {
        return engineDao.getModelWithConfigItemsById(modelId)
    }
    
    suspend fun getModelByName(name: String): EngineModel? {
        return engineDao.getModelByName(name)
    }
    
    suspend fun getConfigItemsByModelId(modelId: Long): List<ConfigItem> {
        return engineDao.getConfigItemsByModelId(modelId)
    }
    
    suspend fun insertModelWithConfigItem(model: EngineModel, configItem: ConfigItem): Long {
        val modelId = engineDao.insertModel(model)
        val configItemWithModelId = configItem.copy(modelId = modelId)
        engineDao.insertConfigItem(configItemWithModelId)
        return modelId
    }
    
    suspend fun insertConfigItem(configItem: ConfigItem): Long {
        return engineDao.insertConfigItem(configItem)
    }
    
    suspend fun deleteConfigItem(configItem: ConfigItem): Boolean {
        engineDao.deleteConfigItem(configItem)
        // 检查该型号下是否还有其他配置项
        val count = engineDao.getConfigItemCount(configItem.modelId)
        if (count == 0) {
            // 如果没有配置项了，删除型号
            val model = engineDao.getModelById(configItem.modelId)
            model?.let { engineDao.deleteModel(it) }
            return true // 返回true表示型号也被删除了
        }
        return false
    }
    
    suspend fun deleteModel(model: EngineModel) {
        engineDao.deleteModel(model)
        // 外键级联删除会自动删除所有配置项
    }
    
    suspend fun getConfigItemCount(modelId: Long): Int {
        return engineDao.getConfigItemCount(modelId)
    }
    
    suspend fun updateConfigItem(configItem: ConfigItem) {
        engineDao.updateConfigItem(configItem)
    }
}
