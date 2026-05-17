package com.devicecontrol.engine.v2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devicecontrol.engine.v2.log.V2Log
import com.devicecontrol.engine.v2.model.V2RecordRowUi

enum class V2UiOperationMode {
    AUTO,
    MANUAL,
}

/**
 * 检测主控台（P7～P12）UI 状态。控制按钮当前仅打日志，后续拷贝 1.0 [TaskControlViewModel] 逻辑接入。
 */
class V2InspectionControlViewModel : ViewModel() {

    private val _engineModelName = MutableLiveData("CFM56-3B")
    val engineModelName: LiveData<String> = _engineModelName

    private val _statusBarText = MutableLiveData("连续 1.0分钟/圈 反转")
    val statusBarText: LiveData<String> = _statusBarText

    private val _operationMode = MutableLiveData(V2UiOperationMode.AUTO)
    val operationMode: LiveData<V2UiOperationMode> = _operationMode

    private val _lpcIndex = MutableLiveData(1)
    val lpcIndex: LiveData<Int> = _lpcIndex

    private val _records = MutableLiveData(sampleRecords())
    val records: LiveData<List<V2RecordRowUi>> = _records

    private val _engineParamsText = MutableLiveData(
        """
        安全扭矩: 1480 lb·ft
        叶片数: 22
        当前速度:
        设定速度:
        剩余时间:
        转动时间:
        游隙消除:
        运行时间:
        电机力矩:
        """.trimIndent(),
    )
    val engineParamsText: LiveData<String> = _engineParamsText

    fun initEngineModel(name: String) {
        V2Log.i(TAG, "initEngineModel name=$name")
        _engineModelName.value = name
    }

    fun setOperationMode(mode: V2UiOperationMode) {
        V2Log.i(TAG, "setOperationMode=$mode")
        _operationMode.value = mode
    }

    fun setLpcIndex(index: Int) {
        V2Log.i(TAG, "setLpcIndex=$index")
        _lpcIndex.value = index
    }

    fun onControlAction(action: String) {
        V2Log.i(TAG, "controlAction=$action")
    }

    fun onStart() = onControlAction("START")
    fun onPause() = onControlAction("PAUSE")
    fun onEnd() = onControlAction("END")

    companion object {
        private const val TAG = "InspectionControlVM"

        private fun sampleRecords() = listOf(
            V2RecordRowUi("LPC1", 6),
            V2RecordRowUi("LPC1", 20),
            V2RecordRowUi("LPC1", 29),
            V2RecordRowUi("LPC1", 37),
        )
    }
}
