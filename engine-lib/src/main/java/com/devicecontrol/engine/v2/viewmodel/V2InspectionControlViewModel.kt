package com.devicecontrol.engine.v2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.devicecontrol.engine.R
import com.devicecontrol.engine.data.model.ConfigItem
import com.devicecontrol.engine.data.model.EngineModel
import com.devicecontrol.engine.data.model.OperationMode
import com.devicecontrol.engine.data.model.RotationDirection
import com.devicecontrol.engine.data.model.TaskRecord
import com.devicecontrol.engine.data.model.TaskStatus
import com.devicecontrol.engine.data.model.V2AutoInspectionConfig
import com.devicecontrol.engine.data.model.V2ManualInspectionConfig
import com.devicecontrol.engine.data.repository.EngineRepository
import com.devicecontrol.engine.data.repository.TaskRepository
import com.devicecontrol.engine.v2.connection.V2ConnectionRepository
import com.devicecontrol.engine.v2.data.V2InspectionRepository
import com.devicecontrol.engine.v2.inspection.strategy.V2InspectionOperationStrategyProvider
import com.devicecontrol.engine.v2.inspection.V2InspectionCommandContext
import com.devicecontrol.engine.v2.inspection.V2InspectionCommandDispatcher
import com.devicecontrol.engine.v2.inspection.V2InspectionEngineViewModel
import com.devicecontrol.engine.v2.inspection.V2InspectionSession
import com.devicecontrol.engine.v2.log.V2Log
import com.devicecontrol.engine.v2.model.V2RecordRowUi
import kotlinx.coroutines.launch

enum class V2UiOperationMode {
    AUTO,
    MANUAL,
}

/** P7/P10 操作钮高亮（绿=选中态，橙=默认） */
data class V2ControlHighlightState(
    val forwardGreen: Boolean = false,
    val reverseGreen: Boolean = false,
    val continuousGreen: Boolean = false,
    val jogGreen: Boolean = false,
    val autoPhotoGreen: Boolean = false,
)

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
    private var autoConfig: V2AutoInspectionConfig = V2AutoInspectionConfig.defaults(0)
    private var manualConfig: V2ManualInspectionConfig = V2ManualInspectionConfig.defaults(0)

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

    private val _controlHighlight = MutableLiveData(V2ControlHighlightState())
    val controlHighlight: LiveData<V2ControlHighlightState> = _controlHighlight

    private val operationStrategy
        get() = V2InspectionOperationStrategyProvider.strategy

    fun requiresDeviceConnection(): Boolean = operationStrategy.requiresDeviceConnection

    private val taskObservers = MediatorLiveData<Unit>().apply {
        addSource(engine.taskExecution) { exec ->
            _isRunning.value = exec?.status == TaskStatus.RUNNING
            syncCommandContext()
            refreshDerivedUi()
        }
        addSource(engine.currentConfigItem) {
            syncCommandContext()
            refreshDerivedUi()
        }
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
            autoConfig = inspectionRepository.getOrCreateAutoConfig(modelId)
            manualConfig = inspectionRepository.getOrCreateManualConfig(modelId)
            _operationMode.value = inspectionRepository.getLastUiMode(modelId)
            _imagePath.value = loaded.model.imagePath
            _lpcPositions.value = loaded.configItems.map { it.position }
            _currentLpcIndex.value = 0
            engine.prepareInspectionTask(loaded.taskId)
            syncCommandContext()
            _sessionReady.value = true
            refreshDerivedUi()
            V2Log.i(
                TAG,
                "initInspection modelId=$modelId mode=${_operationMode.value} taskId=${loaded.taskId}",
            )
        }
    }

    fun reapplyModeSettings() {
        if (modelId <= 0L || _sessionReady.value != true) return
        viewModelScope.launch {
            autoConfig = inspectionRepository.getOrCreateAutoConfig(modelId)
            manualConfig = inspectionRepository.getOrCreateManualConfig(modelId)
            syncCommandContext()
            refreshDerivedUi()
        }
    }

    fun setOperationMode(mode: V2UiOperationMode) {
        if (_isRunning.value == true) {
            engine.pause()
        }
        _operationMode.value = mode
        viewModelScope.launch {
            inspectionRepository.saveLastUiMode(modelId, mode)
            syncCommandContext()
            refreshDerivedUi()
            V2Log.i(TAG, "setOperationMode=$mode saved")
        }
    }

    fun configBladeCounts(): List<Int> = configItems.map { it.bladeCount }

    fun setLpcIndex(index: Int) {
        if (index < 0 || index >= configItems.size) return
        _currentLpcIndex.value = index
        viewModelScope.launch {
            engine.switchToTaskAndWait(index)
            syncCommandContext()
        }
    }

    fun onStart() {
        if (!canOperate()) return
        viewModelScope.launch {
            syncCommandContext()
            if (_operationMode.value == V2UiOperationMode.AUTO) {
                engine.setOperationModeJogOnly()
            }
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
        viewModelScope.launch {
            syncCommandContext()
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
    }

    fun playbackRecord(record: TaskRecord) {
        if (!canOperate(requireConnection = true)) return
        syncCommandContext()
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

    private fun syncCommandContext() {
        val exec = engine.taskExecution.value ?: return
        engine.bindCommandContext(buildCommandContext(exec))
    }

    private fun buildCommandContext(exec: com.devicecontrol.engine.data.model.TaskExecution): V2InspectionCommandContext {
        val mode = _operationMode.value ?: V2UiOperationMode.AUTO
        return V2InspectionCommandContext(
            uiMode = mode,
            autoConfig = autoConfig,
            manualConfig = manualConfig,
            execution = exec,
            configItem = engine.currentConfigItem.value,
            modelName = engineModel?.name ?: _engineModelName.value,
            position = engine.currentConfigItem.value?.position,
        )
    }

    private fun addRecord() {
        if (!canOperate(requireConnection = true)) return
        val config = engine.currentConfigItem.value ?: return
        val position = config.position.toIntOrNull() ?: 1
        engine.addRecord(position, config.bladeCount)
    }

    private fun canOperate(requireConnection: Boolean = true): Boolean {
        if (_sessionReady.value != true) {
            _errorMessage.value = "检测会话未就绪"
            return false
        }
        if (requireConnection && operationStrategy.requiresDeviceConnection) {
            if (!V2ConnectionRepository.isConnected() || !engine.isSlcanReady()) {
                _errorMessage.value = "未连接到设备"
                return false
            }
        }
        return true
    }

    private fun updateCanStart() {
        val connectionOk = !operationStrategy.requiresDeviceConnection ||
            (V2ConnectionRepository.isConnected() && engine.isSlcanReady())
        val ready = _sessionReady.value == true &&
            connectionOk &&
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
        exec?.let {
            _statusBarText.value = buildStatusBar(it)
            refreshControlHighlight(it)
        }
    }

    private fun buildEngineParams(
        model: EngineModel,
        config: ConfigItem,
        exec: com.devicecontrol.engine.data.model.TaskExecution?,
    ): String {
        fun line(labelRes: Int, value: String?) =
            "${getApplication<Application>().getString(labelRes)}: ${value?.takeIf { it.isNotBlank() } ?: ""}"

        val setSpeed = exec?.let {
            val speedSec = V2InspectionCommandDispatcher.resolveSpeedSecPerRev(buildCommandContext(it))
            formatMinPerRev(speedSec)
        }
        return buildString {
            appendLine(line(R.string.v2_param_safe_torque, model.safeTorque))
            appendLine(line(R.string.v2_param_blade_count, config.bladeCount.toString()))
            appendLine(line(R.string.v2_param_current_speed, null))
            appendLine(line(R.string.v2_param_set_speed, setSpeed))
            appendLine(line(R.string.v2_param_remaining_time, null))
            appendLine(line(R.string.v2_param_rotation_time, null))
            appendLine(line(R.string.v2_param_backlash, null))
            appendLine(line(R.string.v2_param_run_time, null))
            appendLine(line(R.string.v2_param_motor_torque, null))
        }.trimEnd()
    }

    private fun buildStatusBar(exec: com.devicecontrol.engine.data.model.TaskExecution): String {
        val modeWord = when (exec.operationMode) {
            OperationMode.CONTINUOUS -> "连续"
            OperationMode.JOG -> "点动"
        }
        val ctx = buildCommandContext(exec)
        val minPerRev = V2InspectionCommandDispatcher.resolveSpeedSecPerRev(ctx) / 60.0
        val dir = if (exec.rotationDirection == RotationDirection.FORWARD) "正转" else "反转"
        return "$modeWord ${"%.1f".format(minPerRev)}分钟/圈 $dir"
    }

    private fun refreshControlHighlight(exec: com.devicecontrol.engine.data.model.TaskExecution) {
        val uiMode = _operationMode.value ?: V2UiOperationMode.AUTO
        _controlHighlight.value = V2ControlHighlightState(
            forwardGreen = exec.rotationDirection == RotationDirection.FORWARD,
            reverseGreen = exec.rotationDirection == RotationDirection.REVERSE,
            continuousGreen = exec.operationMode == OperationMode.CONTINUOUS,
            jogGreen = exec.operationMode == OperationMode.JOG,
            autoPhotoGreen = uiMode == V2UiOperationMode.AUTO,
        )
    }

    private fun formatMinPerRev(secPerRev: Double): String {
        val min = secPerRev / 60.0
        return "${"%.1f".format(min)}分钟/圈"
    }

    override fun onCleared() {
        destroySession()
        super.onCleared()
    }

    companion object {
        private const val TAG = "InspectionControlVM"
    }
}
