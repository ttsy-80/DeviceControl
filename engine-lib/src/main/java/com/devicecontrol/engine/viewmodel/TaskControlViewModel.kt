package com.devicecontrol.engine.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devicecontrol.engine.data.model.ConfigItem
import com.devicecontrol.engine.data.model.OperationMode
import com.devicecontrol.engine.data.model.RotationDirection
import com.devicecontrol.engine.data.model.Task
import com.devicecontrol.engine.data.model.TaskExecution
import com.devicecontrol.engine.data.model.TaskRecord
import com.devicecontrol.engine.data.model.TaskStatus
import com.devicecontrol.engine.communication.CanUsbInitConfig
import com.devicecontrol.engine.communication.CommunicationManager
import com.devicecontrol.engine.communication.command.EngineControlCommand
import com.devicecontrol.engine.communication.transport.UsbCommunicationTransport
import com.devicecontrol.engine.data.repository.EngineRepository
import com.devicecontrol.engine.data.repository.TaskRepository
import com.devicecontrol.engine.log.DefaultEngineLogger
import com.devicecontrol.engine.log.DebugLogHolder
import com.devicecontrol.engine.log.EngineLog
import com.devicecontrol.engine.log.EngineLogger
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import android.content.Context
import com.devicecontrol.engine.communication.protocol.CanUsbProtocol
import com.devicecontrol.engine.communication.protocol.CANOpenHelper
import com.devicecontrol.engine.communication.protocol.CiA402
import com.devicecontrol.engine.communication.protocol.SlcanManager
import com.devicecontrol.engine.communication.protocol.SlcanRequest
import com.devicecontrol.engine.communication.protocol.SlcanTransport
import com.devicecontrol.engine.lifecycle.SlcanEmergencyClose
import com.devicecontrol.engine.usbserial.UsbSerialVcpCallback
import com.devicecontrol.engine.usbserial.UsbSerialVcpManager
import kotlinx.coroutines.MainScope
import kotlin.math.abs

class TaskControlViewModel(
    private val taskRepository: TaskRepository,
    private val engineRepository: EngineRepository,
    private val applicationContext: Context
) : ViewModel() {

    companion object {
        private const val TAG = "TaskControlVM"
        /** 默认每圈耗时：300 秒 = 5 分/圈 */
        private const val DEFAULT_SPEED_SEC_PER_REV = 300.0
        /** 新建执行记录时默认速度步进（秒），与设置弹框默认值一致 */
        private const val DEFAULT_SPEED_STEP_SEC = 5.0
        /** 每圈时间下限（秒） */
        private const val MIN_SPEED_SEC_PER_REV = 1.0
        /** CiA402 速度换算分母（与减速比、编码器分辨率配套） */
        private const val CAN_VELOCITY_SCALE_DIVISOR = 1857.0
    }

    /**
     * 将「每圈耗时」[secPerRev]（秒/圈）换算为发送给驱动器的速度量值。
     * 耗时越小 → 转速越快 → 量值越大（与 RPM ∝ 1/s 一致）。
     * 历史实现误用 (s/60) 作正比，导致速度+ 后界面时间变短而下发值反而变小；此处改为反比关系。
     * 在 [DEFAULT_SPEED_SEC_PER_REV] 处与旧公式的数值对齐，避免默认工况下整体增益突变。
     */
    private fun canVelocityFromSecPerRev(secPerRev: Double, realGearRatio: Double): Int {
        val s = secPerRev.coerceAtLeast(MIN_SPEED_SEC_PER_REV)
        val factor = 60.0 / s
        return (factor * realGearRatio * 512.0 * 65536.0 / CAN_VELOCITY_SCALE_DIVISOR).toInt()
    }

    private fun getRealGearRatio(): Double {
        val gearRatio = _currentConfigItem.value?.gearRatio ?: 1.0
        return gearRatio * 100
    }

    private val _task = MutableLiveData<Task?>()
    val task: LiveData<Task?> = _task
    
    private val _taskExecution = MutableLiveData<TaskExecution?>()
    val taskExecution: LiveData<TaskExecution?> = _taskExecution
    
    private val _currentConfigItem = MutableLiveData<ConfigItem?>()
    val currentConfigItem: LiveData<ConfigItem?> = _currentConfigItem
    
    private val _displayInfo = MutableLiveData<String>()
    val displayInfo: LiveData<String> = _displayInfo
    
    private val _taskIndex = MutableLiveData<String>()
    val taskIndex: LiveData<String> = _taskIndex
    
    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage
    
    private val _taskRecords = MutableLiveData<List<TaskRecord>>(emptyList())
    val taskRecords: LiveData<List<TaskRecord>> = _taskRecords
    
    private var currentGearRatioIndex: Int = 0
    private var vcpManager: UsbSerialVcpManager? = null
    private var slcanManager: SlcanManager? = null
    
    private var encoderResolution: Int = 65536
    private var jogJob: Job? = null
    
    fun loadTask(taskId: Long) {
//        scanAndConnect()
        scanAndConnect2()
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId)
            EngineLog.d(TAG, "loadTask: id=$taskId, configItemIds=${task?.configItemIds?.size}")
            _task.value = task

            if (task != null) {
                // 必须 suspend 顺序执行：若在内层再 launch，会与 loadTaskExecution 竞态，读库可能早于写库
                resetAllTaskExecutionsStatus(taskId, task.configItemIds.size)
                currentGearRatioIndex = 0
                loadTaskExecution(taskId, currentGearRatioIndex)
            }
        }
    }

    private fun scanAndConnect2() {
        vcpManager = UsbSerialVcpManager(applicationContext)
        vcpManager?.let { SlcanEmergencyClose.bind(it) }
        val transport = object : SlcanTransport {
            override fun send(data: String): Boolean {
                return vcpManager?.sendTextLine(data) ?: false
            }
        }
        slcanManager = SlcanManager(transport)

        val defaultLogger = DefaultEngineLogger("Engine")
        EngineLog.setLogger(object : EngineLogger {
            override fun d(tag: String, message: String) {
                defaultLogger.d(tag, message)
                DebugLogHolder.add("D", tag, message)
            }
            override fun i(tag: String, message: String) {
                defaultLogger.i(tag, message)
                DebugLogHolder.add("I", tag, message)
            }
            override fun w(tag: String, message: String) {
                defaultLogger.w(tag, message)
                DebugLogHolder.add("W", tag, message)
            }
            override fun e(tag: String, message: String) {
                defaultLogger.e(tag, message)
                DebugLogHolder.add("E", tag, message)
            }
            override fun e(tag: String, message: String, throwable: Throwable?) {
                defaultLogger.e(tag, message, throwable)
                DebugLogHolder.add("E", tag, message, throwable)
            }
        })
        vcpManager?.scanAndConnect(object : UsbSerialVcpCallback{
            override fun onConnect(isConnect: Boolean) {
                EngineLog.i(TAG, "通讯回调 onConnect: $isConnect")
                viewModelScope.launch {
                    if (isConnect) {
                        val res = slcanManager?.init()
                        if (res?.success == true) {
                            EngineLog.i(TAG, "SLCAN 握手初始化成功")
                            
                            val encReq = CANOpenHelper.readEncoderResolution()
                            val encRes = slcanManager?.execute(encReq)
                            if (encRes?.success == true) {
                                val resolution = encRes.values[CiA402.EncoderResolution.name] as? Int
                                if (resolution != null && resolution > 0) {
                                    encoderResolution = resolution
                                    EngineLog.i(TAG, "成功读取伺服编码器分辨率: $encoderResolution 脉冲/360°")
                                }
                            } else {
                                EngineLog.w(TAG, "读取编码器分辨率失败，使用默认值 $encoderResolution 脉冲/360°")
                            }
                        } else {
                            EngineLog.e(TAG, "SLCAN 握手初始化失败: ${res?.error}")
                        }
                    } else {
                        slcanManager?.close()
                    }
                }
            }

            override fun onDataReceived(data: ByteArray) {
                slcanManager?.feedData(data)
            }

            override fun onError(error: String) {
                EngineLog.e(TAG, "通讯回调 onError: $error")
            }
        })

    }

    /** 扫描并连接：设置 USB 传输并连接第一个可用设备；若为 CAN-USB 设备则连接成功后自动按手册初始化；主 logger 同时写入 [DebugLogHolder] 供调试页回看 */
    private fun scanAndConnect() {
        val defaultLogger = DefaultEngineLogger("Engine")
        EngineLog.setLogger(object : EngineLogger {
            override fun d(tag: String, message: String) {
                defaultLogger.d(tag, message)
                DebugLogHolder.add("D", tag, message)
            }
            override fun i(tag: String, message: String) {
                defaultLogger.i(tag, message)
                DebugLogHolder.add("I", tag, message)
            }
            override fun w(tag: String, message: String) {
                defaultLogger.w(tag, message)
                DebugLogHolder.add("W", tag, message)
            }
            override fun e(tag: String, message: String) {
                defaultLogger.e(tag, message)
                DebugLogHolder.add("E", tag, message)
            }
            override fun e(tag: String, message: String, throwable: Throwable?) {
                defaultLogger.e(tag, message, throwable)
                DebugLogHolder.add("E", tag, message, throwable)
            }
        })
        val transport = UsbCommunicationTransport(applicationContext)
        val manager = CommunicationManager.getInstance()
        manager.setTransport(transport)
        manager.setCanUsbInitConfig(CanUsbInitConfig(canBaudRate = CanUsbProtocol.CanBaudRate.BPS_500K, openChannel = true))
        manager.setDataCallback(object : com.devicecontrol.engine.communication.DataCallback {
            override fun onTextDataReceived(data: String) {
//                _displayInfo.value = _displayInfo.value +" data1:$data"
                EngineLog.d(TAG, "通讯回调 onTextDataReceived: $data")
            }
            override fun onBinaryDataReceived(data: ByteArray) {
//                _displayInfo.value = _displayInfo.value +" data2:$data"
                EngineLog.d(TAG, "通讯回调 onBinaryDataReceived: $data")
            }
            override fun onError(error: String) {
//                _displayInfo.value = _displayInfo.value +" error:$error"
                EngineLog.w(TAG, "通讯回调 onError: $error")
            }
        })
        val started = manager.scanAndConnect()
        EngineLog.i(TAG, "scanAndConnect: started=$started")
    }
    
    /**
     * 进入任务控制页时：所有已有子任务执行记录置为 STOPPED，并把每圈耗时 [TaskExecution.speed] 恢复为默认 300s。
     * 注意：不能仅在「非 STOPPED」时改 speed，否则已是停止态的旧速度永远不会被清回 300。
     */
    private suspend fun resetAllTaskExecutionsStatus(taskId: Long, configItemCount: Int) {
        for (index in 0 until configItemCount) {
            val execution = taskRepository.getTaskExecutionByTaskIdAndIndex(taskId, index) ?: continue
            val resetExecution = execution.copy(
                status = TaskStatus.STOPPED,
                speed = DEFAULT_SPEED_SEC_PER_REV
            )
            if (resetExecution != execution) {
                taskRepository.insertOrUpdateTaskExecution(resetExecution)
            }
        }
    }
    
    private fun loadTaskExecution(taskId: Long, configItemIndex: Int) {
        viewModelScope.launch {
            val task = _task.value ?: return@launch
            
            // 加载或创建该子任务的执行记录
            var execution = taskRepository.getTaskExecutionByTaskIdAndIndex(taskId, configItemIndex)
            if (execution == null) {
                val previousExecution = if (configItemIndex > 0) {
                    taskRepository.getTaskExecutionByTaskIdAndIndex(taskId, configItemIndex - 1)
                } else {
                    null
                }
                
                execution = TaskExecution(
                    taskId = taskId,
                    gearRatioIndex = configItemIndex,
                    speed = previousExecution?.speed ?: DEFAULT_SPEED_SEC_PER_REV,
                    speedStep = previousExecution?.speedStep ?: DEFAULT_SPEED_STEP_SEC,
                    continuousCycles = previousExecution?.continuousCycles ?: 1,
                    jogInterval = previousExecution?.jogInterval ?: 1,
                    playbackSpeed = previousExecution?.playbackSpeed ?: TaskExecution.DEFAULT_PLAYBACK_SEC_PER_REV
                )
                taskRepository.insertOrUpdateTaskExecution(execution)
            }
            _taskExecution.value = execution
            
            // 加载当前配置项
            loadCurrentConfigItem(task, configItemIndex)
            
            // 更新任务索引显示
            updateTaskIndex(task, configItemIndex)
            
            // 加载记录列表
            loadTaskRecords(taskId, configItemIndex)
        }
    }
    
    private fun loadTaskRecords(taskId: Long, gearRatioIndex: Int) {
        viewModelScope.launch {
            val records = taskRepository.getTaskRecordsByTaskIdAndIndex(taskId, gearRatioIndex)
            _taskRecords.value = records
        }
    }
    
    private fun loadCurrentConfigItem(task: Task, index: Int) {
        viewModelScope.launch {
            val model = engineRepository.getModelWithConfigItemsById(task.modelId)
            if (model != null && index < task.configItemIds.size) {
                val configItemId = task.configItemIds[index]
                val configItem = model.configItems.find { it.id == configItemId }
                _currentConfigItem.value = configItem
                
                // 获取当前执行状态以显示操作模式和旋转方向
                val execution = _taskExecution.value
                val operationModeText = when (execution?.operationMode) {
                    OperationMode.JOG -> "点动"
                    OperationMode.CONTINUOUS -> "连续"
                    null -> "点动"
                }
                val rotationDirectionText = when (execution?.rotationDirection) {
                    RotationDirection.FORWARD -> "正转"
                    RotationDirection.REVERSE -> "反转"
                    null -> "正转"
                }
                
                // 更新显示信息，格式：型号 位置 操作模式
                val displayText = "${task.modelName} ${configItem?.position ?: ""} $operationModeText"
                _displayInfo.value = displayText
            }
        }
    }
    
    private fun updateTaskIndex(task: Task, index: Int) {
        val total = task.configItemIds.size
        _taskIndex.value = "任务: ${index + 1}/$total"
    }
    
    data class TaskItem(
        val index: Int,
        val displayName: String
    )
    
    fun getTaskItems(): List<TaskItem> {
        val task = _task.value ?: return emptyList()
        return task.configItemIds.mapIndexed { index, _ ->
            TaskItem(
                index = index,
                displayName = "任务 ${index + 1}/${task.configItemIds.size}"
            )
        }
    }
    
    fun getCurrentTaskIndex(): Int = currentGearRatioIndex
    
    fun switchToTask(index: Int) {
        val task = _task.value ?: return
        
        if (index >= 0 && index < task.configItemIds.size) {
            // 保存当前任务的状态
            val currentExecution = _taskExecution.value
            
            viewModelScope.launch {
                // 保存当前任务的状态
                if (currentExecution != null) {
                    taskRepository.insertOrUpdateTaskExecution(currentExecution)
                }
                
                // 切换到目标任务，不改变目标任务的状态或配置
                currentGearRatioIndex = index
                loadTaskExecution(task.id, currentGearRatioIndex)
            }
        }
    }
    
    fun start() {
        val task = _task.value ?: return
        val exec = _taskExecution.value ?: return
        val pos = _currentConfigItem.value?.position ?: return

        if (exec.operationMode == OperationMode.JOG) {
            startJogLoop()
            loadCurrentConfigItem(task, currentGearRatioIndex)
        } else {
            val pending = exec.copy(status = TaskStatus.RUNNING)
            val cmd = EngineControlCommand.start(
                modelName = task.modelName,
                position = pos,
                forward = pending.rotationDirection == RotationDirection.FORWARD,
                jog = false,
                speedConfig = pending.speed / 60.0,
                jogInterval = pending.jogInterval,
                continuousCycles = pending.continuousCycles
            )
            sendCommandThenPersist(cmd, pending) {
                loadCurrentConfigItem(task, currentGearRatioIndex)
            }
        }
    }

    private fun startJogLoop() {
        jogJob?.cancel()
        jogJob = viewModelScope.launch(Dispatchers.IO) {
            val initialExec = _taskExecution.value ?: return@launch
            val bladeCount = _currentConfigItem.value?.bladeCount ?: 10
            val validBladeCount = if (bladeCount > 0) bladeCount else 10
            val targetSteps = initialExec.continuousCycles * validBladeCount
            var stepCount = 0
            var runningPersisted = false

            while (isActive) {
                val currentExec = _taskExecution.value ?: return@launch
                if (currentExec.operationMode != OperationMode.JOG) break
                if (runningPersisted && currentExec.status != TaskStatus.RUNNING) break

                if (stepCount >= targetSteps) {
                    val bundle = withContext(Dispatchers.Main) {
                        val cur = _taskExecution.value ?: return@withContext null
                        val n = modelName() ?: return@withContext null
                        val p = position() ?: return@withContext null
                        val pauseCmd = EngineControlCommand.pause(n, p)
                        val reqs = requestsForCommand(pauseCmd, cur)
                        if (reqs.isEmpty()) null else reqs to cur.copy(status = TaskStatus.STOPPED)
                    }
                    if (bundle != null) {
                        val (pauseRequests, stoppedExec) = bundle
                        val res = slcanManager?.execute(pauseRequests)
                        if (res?.success == true) {
                            persistExecutionSync(stoppedExec)
                        } else {
                            _toastMessage.postValue("点动结束停止失败: ${res?.error ?: "未知错误"}")
                        }
                    }
                    break
                }

                val onceRotate = 360.0 / validBladeCount
                val gearRatio = getRealGearRatio()

                val isForward = currentExec.rotationDirection == RotationDirection.FORWARD
                val signedDegrees = if (isForward) onceRotate else -onceRotate
                val relativePulses = ((signedDegrees / 360.0) * encoderResolution * gearRatio).toInt()
                val speedSec = currentExec.speed
                val pbVelocity = canVelocityFromSecPerRev(speedSec, gearRatio)

                val requests = CANOpenHelper.startRelativePositionMode(kotlin.math.abs(pbVelocity), relativePulses)
                val res = slcanManager?.execute(requests)
                if (res?.success == true) {
                    if (!runningPersisted) {
                        runningPersisted = true
                        val cur = withContext(Dispatchers.Main) { _taskExecution.value } ?: break
                        persistExecutionSync(cur.copy(status = TaskStatus.RUNNING))
                    }
                    _toastMessage.postValue("点动执行中: ${stepCount + 1}/$targetSteps (${java.text.DecimalFormat("#0.0").format(onceRotate)}度)")
                } else {
                    _toastMessage.postValue("点动下发失败: ${res?.error ?: "未知错误"}")
                    break
                }

                stepCount++
                delay(currentExec.jogInterval * 1000L)
            }
        }
    }

    fun pause() {
        jogJob?.cancel()
        jogJob = null
        val name = modelName() ?: return
        val pos = position() ?: return
        val exec = _taskExecution.value ?: return
        val pending = exec.copy(status = TaskStatus.PAUSED)
        sendCommandThenPersist(EngineControlCommand.pause(name, pos), pending) {
            val task = _task.value ?: return@sendCommandThenPersist
            loadCurrentConfigItem(task, currentGearRatioIndex)
        }
    }

    fun setForward() {
        val exec = _taskExecution.value ?: return
        val pending = exec.copy(rotationDirection = RotationDirection.FORWARD)
        val n = modelName() ?: return
        val p = position() ?: return
        sendCommandThenPersist(EngineControlCommand.forward(n, p), pending) {
            val task = _task.value ?: return@sendCommandThenPersist
            loadCurrentConfigItem(task, currentGearRatioIndex)
        }
    }

    fun setReverse() {
        val exec = _taskExecution.value ?: return
        val pending = exec.copy(rotationDirection = RotationDirection.REVERSE)
        val n = modelName() ?: return
        val p = position() ?: return
        sendCommandThenPersist(EngineControlCommand.reverse(n, p), pending) {
            val task = _task.value ?: return@sendCommandThenPersist
            loadCurrentConfigItem(task, currentGearRatioIndex)
        }
    }

    fun setJog() {
        updateExecution { it.copy(operationMode = OperationMode.JOG) }
        start()
        val task = _task.value ?: return
        loadCurrentConfigItem(task, currentGearRatioIndex)
    }

    fun setContinuous() {
        val exec = _taskExecution.value ?: return
        val pending = exec.copy(operationMode = OperationMode.CONTINUOUS)
        val n = modelName() ?: return
        val p = position() ?: return
        sendCommandThenPersist(EngineControlCommand.continuous(n, p), pending) {
            val task = _task.value ?: return@sendCommandThenPersist
            loadCurrentConfigItem(task, currentGearRatioIndex)
        }
    }

    fun increaseSpeed() {
        val execution = _taskExecution.value ?: return
        val step = execution.speedStep
        val newSpeed = (execution.speed - step).coerceAtLeast(MIN_SPEED_SEC_PER_REV)
        val pending = execution.copy(speed = newSpeed)
        val n = modelName() ?: return
        val p = position() ?: return
        sendCommandThenPersist(EngineControlCommand.speedPlus(n, p), pending)
    }

    fun decreaseSpeed() {
        val execution = _taskExecution.value ?: return
        val newSpeed = execution.speed + execution.speedStep
        val pending = execution.copy(speed = newSpeed)
        val n = modelName() ?: return
        val p = position() ?: return
        sendCommandThenPersist(EngineControlCommand.speedMinus(n, p), pending)
    }
    
    fun updateSettings(speedStep: Double, continuousCycles: Int, jogInterval: Int, playbackSpeed: Double) {
        updateExecution {
            it.copy(
                speedStep = speedStep,
                continuousCycles = continuousCycles,
                jogInterval = jogInterval,
                playbackSpeed = playbackSpeed
            )
        }
    }
    
    // 获取当前配置项，用于后续对接指令
    fun getCurrentSettings(): Triple<Double, Int, Int>? {
        val execution = _taskExecution.value ?: return null
        return Triple(execution.speedStep, execution.continuousCycles, execution.jogInterval)
    }
    
    fun takePhoto() {
        // 拍照功能：无对应功能，暂时不实现
    }

    fun addRecord(position: Int, bladeNumber: Int) {
        val task = _task.value ?: return
        viewModelScope.launch {
            val req = CANOpenHelper.readPosition()
            val res = slcanManager?.execute(req)
            if (res?.success == true) {
                val actualPos = res.values[CiA402.ActualPosition.name] as? Int
                if (actualPos != null) {
                    val gearRatio = getRealGearRatio()
                    val recordAngle = actualPos * 360.0 / (gearRatio * encoderResolution)
                    val record = TaskRecord(
                        taskId = task.id,
                        gearRatioIndex = currentGearRatioIndex,
                        recordNumber = 0,
                        position = position, // 0.1° 整数刻度，与历史一致
                        angleDegrees = recordAngle.toFloat(),
                        bladeNumber = bladeNumber
                    )
                    taskRepository.insertTaskRecord(record)
                    loadTaskRecords(task.id, currentGearRatioIndex)

                    _toastMessage.postValue("记录位置获取成功: ${record.angleDegrees}")
                }
            } else {
                EngineLog.e(TAG, "记录失败: ${res?.error}")
                _toastMessage.postValue("获取失败: ${res?.error ?: "未知"}")
            }
        }
    }

    fun playbackRecord(record: TaskRecord) {
        val pbSpeed = execution()?.playbackSpeed ?: TaskExecution.DEFAULT_PLAYBACK_SEC_PER_REV
        val gearRatio = getRealGearRatio()
        // 回查速度同为秒/圈，与主运行速度使用同一换算（耗时越短 → 下发速度越大）
        val pbVelocity = canVelocityFromSecPerRev(pbSpeed, gearRatio)
        val positionAngle = record.angleDegrees
        val pulses = ((positionAngle / 360.0) * encoderResolution * gearRatio).toInt()

        viewModelScope.launch {
            val reqs = CANOpenHelper.startPositionMode(Math.abs(pbVelocity), pulses)
            val res = slcanManager?.execute(reqs)
            if (res?.success == true) {
                _toastMessage.postValue("已触发回溯指令: $positionAngle 度")
            } else {
                _toastMessage.postValue("回溯指令失败: ${res?.error ?: "未知"}")
            }
        }
    }
    
    private fun updateExecution(update: (TaskExecution) -> TaskExecution) {
        val execution = _taskExecution.value ?: return
        val updated = update(execution)
        _taskExecution.value = updated
        
        viewModelScope.launch {
            taskRepository.insertOrUpdateTaskExecution(updated)
        }
    }

    /** 在协程内同步落库：主线程更新 LiveData，再 suspend 写 Room（供点动循环等 IO 协程使用） */
    private suspend fun persistExecutionSync(execution: TaskExecution) {
        withContext(Dispatchers.Main) {
            _taskExecution.value = execution
        }
        taskRepository.insertOrUpdateTaskExecution(execution)
    }

    /**
     * 按逻辑指令字符串与期望的执行快照，构建 CAN 写序列（速度/方向取自 [exec]）。
     */
    private fun requestsForCommand(cmd: String, exec: TaskExecution): List<SlcanRequest> {
        val speedSec = exec.speed.coerceAtLeast(MIN_SPEED_SEC_PER_REV)
        val gearRatio = getRealGearRatio()
        val canVelocity = canVelocityFromSecPerRev(speedSec, gearRatio)
        val isForward = exec.rotationDirection == RotationDirection.FORWARD
        val signedVelocity = if (isForward) canVelocity else -canVelocity
        return when {
            cmd.startsWith("START") -> CANOpenHelper.startSpeedMode(signedVelocity)
            cmd.startsWith("PAUSE") -> CANOpenHelper.stop()
            cmd.startsWith("CONTINUOUS") -> CANOpenHelper.startSpeedMode(signedVelocity)
            cmd.startsWith("SPEED_") -> CANOpenHelper.changeVelocity(signedVelocity)
            cmd.startsWith("FORWARD") -> CANOpenHelper.reverseDirection(signedVelocity)
            cmd.startsWith("REVERSE") -> CANOpenHelper.reverseDirection(signedVelocity)
            else -> emptyList()
        }
    }

    /**
     * 先下发 CAN，**仅成功时** 将 [executionAfterSuccess] 落库并刷新 LiveData；失败则提示且不修改数据库。
     */
    private fun sendCommandThenPersist(
        cmd: String,
        executionAfterSuccess: TaskExecution,
        onSuccess: () -> Unit = {}
    ) {
        val requests = requestsForCommand(cmd, executionAfterSuccess)
        if (requests.isEmpty()) {
            EngineLog.d(TAG, "sendCommandThenPersist: 未映射 CAN 指令, $cmd")
            return
        }
        viewModelScope.launch {
            val res = slcanManager?.execute(requests)
            if (res?.success == true) {
                _taskExecution.value = executionAfterSuccess
                taskRepository.insertOrUpdateTaskExecution(executionAfterSuccess)
                EngineLog.i(TAG, "sendCommandThenPersist: 成功 $cmd")
                _toastMessage.value = "命令发送成功"
                onSuccess()
            } else {
                EngineLog.e(TAG, "sendCommandThenPersist: 失败 $cmd, error=${res?.error}")
                _toastMessage.value = "发送失败: ${res?.error ?: "未知错误"}"
            }
        }
    }

    /**
     * 测试发送指令：将用户输入的原始文本通过 [CommunicationManager.sendText] 下发，供调试使用。
     */
    fun sendTestInstruction(text: String) {
//        if (text.isBlank()) return
//        val manager = CommunicationManager.getInstance()
//        val sent = manager.sendText(text)
        val sent = vcpManager?.sendTextLine(text)?:false
        if (sent) EngineLog.d(TAG, "sendTestInstruction: sent, cmd:$text len=${text.length}")
        else EngineLog.w(TAG, "sendTestInstruction: 发送失败 cmd:$text")
    }

    private fun modelName(): String? = _task.value?.modelName
    private fun position(): String? = _currentConfigItem.value?.position
    private fun execution(): TaskExecution? = _taskExecution.value

    override fun onCleared() {
        super.onCleared()
    }

    fun destroy() {
        SlcanEmergencyClose.unbind()
        if (slcanManager?.state == SlcanManager.State.READY) {
            MainScope().launch {
                pause()
                slcanManager?.close()
                vcpManager?.release()
            }
        } else {
            vcpManager?.release()
        }

    }
}

