package com.devicecontrol.engine.v2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devicecontrol.engine.data.repository.EngineRepository
import com.devicecontrol.engine.v2.log.V2Log
import com.devicecontrol.engine.v2.model.V2ModelCardUi
import kotlinx.coroutines.launch

/** 发动机检测 - 型号选择（P6），数据来自 Room */
class V2InspectionModelViewModel(
    private val repository: EngineRepository,
) : ViewModel() {

    private val _models = MutableLiveData<List<V2ModelCardUi>>(emptyList())
    val models: LiveData<List<V2ModelCardUi>> = _models

    private val _selectedModelId = MutableLiveData<Long?>()
    val selectedModelId: LiveData<Long?> = _selectedModelId

    init {
        observeModels()
    }

    private fun observeModels() {
        viewModelScope.launch {
            repository.getAllModelsWithConfigItems().collect { list ->
                val cards = list.map { V2ModelCardUi(it.model.id, it.model.name) }
                _models.postValue(cards)
                if (_selectedModelId.value == null && cards.isNotEmpty()) {
                    val preferred = cards.firstOrNull { it.name == "CFM56-3B" } ?: cards.first()
                    _selectedModelId.postValue(preferred.id)
                }
                V2Log.d(TAG, "inspection models=${cards.size}")
            }
        }
    }

    fun selectModel(modelId: Long) {
        V2Log.i(TAG, "selectModel id=$modelId")
        _selectedModelId.value = modelId
    }

    fun selectedModelName(): String? =
        _models.value?.firstOrNull { it.id == _selectedModelId.value }?.name

    companion object {
        private const val TAG = "InspectionModelVM"
    }
}
