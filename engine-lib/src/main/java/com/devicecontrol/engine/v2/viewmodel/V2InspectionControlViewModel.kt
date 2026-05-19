package com.devicecontrol.engine.v2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.devicecontrol.engine.data.model.ConfigItem
import com.devicecontrol.engine.data.model.EngineModel
import com.devicecontrol.engine.data.model.OperationMode
import com.devicecontrol.engine.data.model.RotationDirection
import com.devicecontrol.engine.data.model.TaskRecord
import com.devicecontrol.engine.data.model.TaskStatus
import com.devicecontrol.engine.data.repository.EngineRepository
import com.devicecontrol.engine.data.repository.TaskRepository
import com.devicecontrol.engine.v2.connection.V2ConnectionRepository
import com.devicecontrol.engine.v2.data.V2InspectionRepository
import com.devicecontrol.engine.v2.inspection.V2InspectionEngineViewModel
import com.devicecontrol.engine.v2.inspection.V2InspectionSession
import com.devicecontrol.engine.v2.inspection.V2ModeSettingsMapper
import com.devicecontrol.engine.v2.log.V2Log
import com.devicecontrol.engine.v2.model.V2RecordRowUi
import kotlinx.coroutines.launch

enum class V2UiOperationMode {
    AUTO,
    MANUAL,
}

class V2InspectionControlViewModel(
    application: Application,
    private val engineRepository: EngineRepository,
    private val taskRepository: TaskRepository,
    private val inspectionRepository: V2InspectionRepository,
) : AndroidViewModel(application) {

    private val session = V2InspectionSession(engineRepository, inspectionRepository)
    private val engine = V2InspectionEngineViewModel(
        taskRepository,
        engineRepository,
        application,
    )

    private var modelId: Long = 0
    private var engineModel: EngineModel? = null
    private var configItems: List<ConfigItem> = emptyList()

    private val _engineModelName = MutableLiveData("")
    val engineModelName: LiveData<String> = _engineModelName

    private val _imagePath = MutableLiveData<String?>(null)
    val imagePath: LiveData<String?> = _imagePath

    private val _statusBarText = MutableLiveData("")
    val statusBarText: LiveData<String> = _statusBarText

    private val _operationMode = MutableLiveData(V2UiOperationMode.AUTO)
    val operationMode: LiveData<V2UiOperationMode> = _operationMode

    private val _lpcPositions = MutableLiveData<List<String>>(emptyList())
    val lpcPositions: LiveData<List<String>> = _lpcPositions

    private val _currentLpcIndex = MutableLiveData(0)
    val currentLpcIndex: LiveData<Int> = _currentLpcIndex

    private val _engineParamsText = MutableLiveData("")
    val engineParamsText: LiveData<String> = _engineParamsText

    private val _records = MutableLiveData<List<V2RecordRowUi>>(emptyList())
    val records: LiveData<List<V2RecordRowUi>> = _records

    private val _isRunning = MutableLiveData(false)
    val isRunning: LiveData<Boolean> = _isRunning

    private val _canStart = MediatorLiveData(true)
    val canStart: LiveData<Boolean> = _canStart

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _sessionReady = MutableLiveData(false)
    val sessionReady: LiveData<Boolean> = _sessionReady

    private val taskObservers = MediatorLiveData<Unit>().apply {
        addSource(engine.taskExecution) { exec ->
            _isRunning.value = exec?.status == TaskStatus.RUNNING
            refreshDerivedUi()
        }
        addSource(engine.currentConfigItem) { refreshDerivedUi() }
        addSource(engine.taskRecords) { list ->
            val label = engine.currentConfigItem.value?.position.orEmpty()
            _records.value = list.map { record ->
                V2RecordRowUi(
                    recordId = record.recordId,
                    positionLabel = label,
                    bladeCount = record.bladeNumber,
                    taskRecord = record,
                )
            }
        }
        addSource(engine.toastMessage) { msg ->
            if (!msg.isNullOrBlank()) {
                _errorMessage.value = msg
            }
        }
    }

    init {
        _canStart.addSource(V2ConnectionRepository.connectionState) { updateCanStart() }
        _canStart.addSource(_isRunning) { updateCanStart() }
        _canStart.addSource(_sessionReady) { updateCanStart() }
        _canStart.addSource(taskObservers) { }
    }

    fun initInspection(modelId: Long, modelName: String) {
        this.modelId = modelId
        _engineModelName.value = modelName
        _sessionReady.value = false
        viewModelScope.launch {
            val loaded = session.load(modelId)
            if (loaded == null) {
                _errorMessage.value = "未找到型号或配置项"
                return@launch
            }
            engineModel = loaded.model
            configItems = loaded.configItems
            _imagePath.value = loaded.model.imagePath
            _lpcPositions.value = loaded.configItems.map { it.position }
            _currentLpcIndex.value = 0
            engine.loadTask(loaded.taskId)
            _sessionReady.value = true
            refreshDerivedUi()
            V2Log.i(TAG, "initInspection modelId=$modelId taskId=${loaded.taskId} lpc=${loaded.configItems.size}")
        }
    }

    fun setOperationMode(mode: V2UiOperationMode) {
        if (_isRunning.value == true) {
            engine.pause()
        }
        _operationMode.value = mode
        V2Log.i(TAG, "setOperationMode=$mode")
    }

    fun configBladeCounts(): List<Int> = configItems.map { it.bladeCount }

    fun setLpcIndex(index: Int) {
        if (index < 0 || index >= configItems.size) return
        _currentLpcIndex.value = index
        engine.switchToTask(index)
    }

    fun onStart() {
        if (!canOperate()) return
        viewModelScope.launch {
            val exec = engine.taskExecution.value ?: return@launch
            val manual = _operationMode.value == V2UiOperationMode.MANUAL
            val defaults = if (manual) {
                V2ModeSettingsViewModel.defaultManualFields()
            } else {
                V2ModeSettingsViewModel.defaultAutoFields()
            }
            val snapshot = inspectionRepository.loadModeSettings(modelId, manual, defaults)
            val updated = if (manual) {
                V2ModeSettingsMapper.applyManualToExecution(snapshot, exec)
            } else {
                V2ModeSettingsMapper.applyAutoToExecution(snapshot, exec)
            }
            engine.applyExecutionFromV2 { updated }
            engine.start(first = true)
        }
    }

    fun onPause() {
        engine.pause()
    }

    fun onEnd() {
        engine.pause()
    }

    fun onControlAction(action: String) {
        if (_isRunning.value == true && _operationMode.value == V2UiOperationMode.AUTO) {
            if (action !in AUTO_ALLOWED_WHILE_RUNNING) return
        }
        when (action) {
            "FORWARD" -> engine.setForward()
            "REVERSE" -> engine.setReverse()
            "ACCEL" -> engine.increaseSpeed()
            "DECEL" -> engine.decreaseSpeed()
            "CONTINUOUS" -> engine.setOperationModeContinuousOnly()
            "JOG" -> engine.setOperationModeJogOnly()
            "AUTO_PHOTO" -> engine.takePhoto()
            "BACKLASH", "BACKLASH_ON_RETURN" -> V2Log.i(TAG, "controlAction=$action (stub)")
            "RECORD" -> addRecord()
            else -> V2Log.i(TAG, "controlAction=$action")
        }
    }

    fun playbackRecord(record: TaskRecord) {
        if (!engine.isSlcanReady()) {
            _errorMessage.value = "未连接到设备"
            return
        }
        engine.playbackRecord(record)
    }

    fun deleteRecord(record: TaskRecord) {
        engine.deleteRecord(record)
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun destroySession() {
        engine.destroy()
    }

    private fun addRecord() {
        if (!engine.isSlcanReady()) {
            _errorMessage.value = "未连接到设备"
            return
        }
        val config = engine.currentConfigItem.value ?: return
        val position = config.position.toIntOrNull() ?: 1
        engine.addRecord(position, config.bladeCount)
    }

    private fun canOperate(): Boolean {
        if (_sessionReady.value != true) {
            _errorMessage.value = "检测会话未就绪"
            return false
        }
        if (!V2ConnectionRepository.isConnected() || !engine.isSlcanReady()) {
            _errorMessage.value = "未连接到设备"
            return false
        }
        return true
    }

    private fun updateCanStart() {
        val ready = _sessionReady.value == true &&
            V2ConnectionRepository.isConnected() &&
            engine.isSlcanReady() &&
            _isRunning.value != true
        _canStart.value = ready
    }

    private fun refreshDerivedUi() {
        val model = engineModel
        val config = engine.currentConfigItem.value
        val exec = engine.taskExecution.value
        if (model != null && config != null) {
            _engineParamsText.value = buildEngineParams(model, config, exec)
        }
        if (exec != null) {
            _statusBarText.value = buildStatusBar(exec)
        }
    }

    private fun buildEngineParams(
        model: EngineModel,
        config: ConfigItem,
        exec: com.devicecontrol.engine.data.model.TaskExecution?,
    ): String = buildString {
        appendLine("安全扭矩: ${model.safeTorque.ifBlank { "—" }}")
        appendLine("叶片数: ${config.bladeCount}")
        appendLine("变速比: ${config.gearRatio}")
        appendLine("位置: ${config.position}")
        if (exec != null) {
            appendLine("当前速度: ${formatSecPerRev(exec.speed)}")
            appendLine("设定速度: ${formatSecPerRev(exec.speed)}")
            appendLine("运行状态: ${exec.status}")
            appendLine("方向: ${if (exec.rotationDirection == RotationDirection.FORWARD) "正转" else "反转"}")
        }
    }

    private fun buildStatusBar(exec: com.devicecontrol.engine.data.model.TaskExecution): String {
        val modeLabel = when (exec.operationMode) {
            OperationMode.JOG -> "点动"
            OperationMode.CONTINUOUS -> "连续"
        }
        val dir = if (exec.rotationDirection == RotationDirection.FORWARD) "正转" else "反转"
        val minPerRev = exec.speed / 60.0
        return "${modeLabel} ${"%.1f".format(minPerRev)}分钟/圈 $dir"
    }

    private fun formatSecPerRev(sec: Double): String {
        val min = sec / 60.0
        return "${"%.1f".format(min)} 分钟/圈"
    }

    override fun onCleared() {
        destroySession()
        super.onCleared()
    }

    companion object {
        private const val TAG = "InspectionControlVM"
        private val AUTO_ALLOWED_WHILE_RUNNING = setOf("AUTO_PHOTO")
    }
}
