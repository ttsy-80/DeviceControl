package com.devicecontrol.engine.v2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devicecontrol.engine.v2.log.V2Log
import com.devicecontrol.engine.v2.model.V2ModelCardUi

/**
 * 发动机检测 - 型号选择（P6）。当前使用稿面示例数据，后续可接 [EngineRepository]。
 */
class V2InspectionModelViewModel : ViewModel() {

    private val _models = MutableLiveData(sampleModels())
    val models: LiveData<List<V2ModelCardUi>> = _models

    /** 稿面默认选中 CFM56-3B */
    private val _selectedModelId = MutableLiveData<Long?>(2L)
    val selectedModelId: LiveData<Long?> = _selectedModelId

    fun selectModel(modelId: Long) {
        V2Log.i(TAG, "selectModel id=$modelId")
        _selectedModelId.value = modelId
    }

    fun selectedModelName(): String? =
        _models.value?.firstOrNull { it.id == _selectedModelId.value }?.name

    companion object {
        private const val TAG = "InspectionModelVM"

        private fun sampleModels() = listOf(
            V2ModelCardUi(1, "CFM56-5B"),
            V2ModelCardUi(2, "CFM56-3B"),
            V2ModelCardUi(3, "PW4000"),
            V2ModelCardUi(4, "LEAP-1C"),
            V2ModelCardUi(5, "F119-PW-100"),
            V2ModelCardUi(6, "V2500"),
            V2ModelCardUi(7, "GE90-115B"),
            V2ModelCardUi(8, "Trent 700"),
        )
    }
}
