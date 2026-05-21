package com.devicecontrol.engine.v2.inspection

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devicecontrol.engine.communication.command.EngineControlCommand
import com.devicecontrol.engine.communication.protocol.CANOpenHelper
import com.devicecontrol.engine.communication.protocol.CiA402
import com.devicecontrol.engine.communication.protocol.SlcanManager
import com.devicecontrol.engine.communication.protocol.SlcanRequest
import com.devicecontrol.engine.communication.protocol.SlcanTransport
import com.devicecontrol.engine.data.model.ConfigItem
import com.devicecontrol.engine.data.model.OperationMode
import com.devicecontrol.engine.data.model.RotationDirection
import com.devicecontrol.engine.data.model.Task
import com.devicecontrol.engine.data.model.TaskExecution
import com.devicecontrol.engine.data.model.TaskRecord
import com.devicecontrol.engine.data.model.TaskStatus
import com.devicecontrol.engine.data.repository.EngineRepository
import com.devicecontrol.engine.data.repository.TaskRepository
import com.devicecontrol.engine.lifecycle.SlcanEmergencyClose
import com.devicecontrol.engine.log.DebugLogHolder
import com.devicecontrol.engine.log.DefaultEngineLogger
import com.devicecontrol.engine.log.EngineLog
import com.devicecontrol.engine.log.EngineLogger
import com.devicecontrol.engine.usbserial.UsbSerialVcpCallback
import com.devicecontrol.engine.usbserial.UsbSerialVcpManager
import com.devicecontrol.engine.v2.connection.V2ConnectionRepository
import com.devicecontrol.engine.v2.connection.V2ConnectionState
import com.devicecontrol.engine.v2.inspection.V2InspectionEngineViewModel.Companion.CONTINUOUS_PROGRESS_TOAST_MS
import com.devicecontrol.engine.v2.inspection.V2InspectionEngineViewModel.Companion.DEFAULT_SPEED_SEC_PER_REV
import com.devicecontrol.engine.v2.inspection.strategy.V2CanExecuteOutcome
import com.devicecontrol.engine.v2.inspection.strategy.V2InspectionOperationStrategyProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * V2 检测页专用机控逻辑（自 v1 [com.devicecontrol.engine.viewmodel.TaskControlViewModel] 复制，
 * 避免改动 1.0 控制页 ViewModel）。
 */
class V2InspectionEngineViewModel(
    private val taskRepository: TaskRepository,
    private val engineRepository: EngineRepository,
    private val applicationContext: Context
) : ViewModel() {

    companion object {
        private const val TAG = "V2InspectionEngineVM"

        /** 默认每圈耗时：300 秒 = 5 分/圈 */
        private const val DEFAULT_SPEED_SEC_PER_REV = 300.0

        /** 新建执行记录时默认速度步进（秒），与设置弹框默认值一致 */
        private const val DEFAULT_SPEED_STEP_SEC = 5.0

        /** 每圈时间下限（秒） */
        private const val MIN_SPEED_SEC_PER_REV = 1.0

        /** CiA402 速度换算分母（与减速比、编码器分辨率配套） */
        private const val CAN_VELOCITY_SCALE_DIVISOR = 1857.0

        /** 连续模式「等一圈」等待时分段 sleep 的步长（ms），便于调速后尽快按新秒/圈重算剩余等待 */
        private const val CONTINUOUS_SPEED_POLL_MS = 50L
        /** 在「等一圈」分段等待内，进度 Toast 的最小间隔（ms） */
        private const val CONTINUOUS_PROGRESS_TOAST_MS = 3500L
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

    private val operationStrategy
        get() = V2InspectionOperationStrategyProvider.strategy

    private suspend fun executeCanRequests(requests: List<SlcanRequest>): V2CanExecuteOutcome =
        operationStrategy.executeRequests(slcanManager, requests)

    /**
     * 编码器 [actualPos] 可能累计多圈，折算角度会超过 ±360°。
     * 记录展示与叶片序号按「单圈内相位」处理，规范到 [0, 360)（与 [playbackRecord] 使用同一套角度语义）。
     */
    private fun normalizeAngleDegrees0To360(angleDegrees: Double): Double {
        val m = angleDegrees % 360.0
        return if (m < 0) m + 360.0 else m
    }

    private val _task = MutableLiveData<Task?>()
    val task: LiveData<Task?> = _task

    private val _taskExecution = MutableLiveData<TaskExecution?>()
    val taskExecution: LiveData<TaskExecution?> = _taskExecution

    private val _currentConfigItem = MutableLiveData<ConfigItem?>()
    val currentConfigItem: LiveData<ConfigItem?> = _currentConfigItem

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

    /** 点动步进循环或连续模式「按圈计时后自动停」的协程，[pause] 时会取消 */
    private var jogJob: Job? = null

    /** V2 检测指令上下文（含自动/手动配置实体），下发 CAN 前由控制页注入。 */
    @Volatile
    private var commandContext: V2InspectionCommandContext? = null

    fun bindCommandContext(ctx: V2InspectionCommandContext) {
        commandContext = ctx
    }

    private fun commandCtx(exec: TaskExecution = _taskExecution.value!!): V2InspectionCommandContext {
        val base = commandContext
            ?: error("V2InspectionEngineViewModel: commandContext 未绑定")
        return base.copy(execution = exec)
    }

    fun loadTask(taskId: Long) {
        viewModelScope.launch { prepareInspectionTask(taskId) }
    }

    /** V2 检测页：加载任务并等待 [taskExecution] 就绪（供应用 P8/P11 后再下发指令）。 */
    suspend fun prepareInspectionTask(taskId: Long) {
        withContext(Dispatchers.Main) {
            if (vcpManager == null) scanAndConnect2()
        }
        val task = taskRepository.getTaskById(taskId) ?: return
        EngineLog.d(TAG, "prepareInspectionTask: id=$taskId, lpc=${task.configItemIds.size}")
        withContext(Dispatchers.Main) { _task.value = task }
        resetAllTaskExecutionsStatus(taskId, task.configItemIds.size)
        currentGearRatioIndex = 0
        loadTaskExecutionBlocking(taskId, currentGearRatioIndex)
    }

    suspend fun switchToTaskAndWait(index: Int) {
        val task = withContext(Dispatchers.Main) { _task.value } ?: return
        if (index < 0 || index >= task.configItemIds.size) return
        EngineLog.i(TAG, "switchToTaskAndWait: gearIndex=$index (from=$currentGearRatioIndex)")
        if (!stopCurrentTaskMotionBeforeSwitch()) {
            EngineLog.w(TAG, "switchToTaskAndWait: 停止当前运动失败，取消切换")
            return
        }
        val latest = withContext(Dispatchers.Main) { _taskExecution.value }
        if (latest != null) {
            taskRepository.insertOrUpdateTaskExecution(latest)
        }
        currentGearRatioIndex = index
        loadTaskExecutionBlocking(task.id, currentGearRatioIndex)
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
        vcpManager?.scanAndConnect(object : UsbSerialVcpCallback {
            override fun onConnect(isConnect: Boolean) {
                EngineLog.i(TAG, "通讯回调 onConnect: $isConnect")
                V2ConnectionRepository.update(
                    if (isConnect) V2ConnectionState.CONNECTED else V2ConnectionState.DISCONNECTED,
                )
                viewModelScope.launch {
                    if (isConnect) {
                        val res = slcanManager?.init()
                        if (res?.success == true) {
                            EngineLog.i(TAG, "SLCAN 握手初始化成功")

                            val encReq = CANOpenHelper.readEncoderResolution()
                            val encRes = slcanManager?.execute(encReq)
                            if (encRes?.success == true) {
                                val resolution =
                                    encRes.values[CiA402.EncoderResolution.name] as? Int
                                if (resolution != null && resolution > 0) {
                                    encoderResolution = resolution
                                    EngineLog.i(
                                        TAG,
                                        "成功读取伺服编码器分辨率: $encoderResolution 脉冲/360°"
                                    )
                                }
                            } else {
                                EngineLog.w(
                                    TAG,
                                    "读取编码器分辨率失败，使用默认值 $encoderResolution 脉冲/360°"
                                )
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

    /**
     * 进入任务控制页时：所有已有子任务执行记录置为 STOPPED，并把每圈耗时 [TaskExecution.speed] 恢复为默认 300s。
     * 注意：不能仅在「非 STOPPED」时改 speed，否则已是停止态的旧速度永远不会被清回 300。
     * 历史库中若为点动模式，一并改为连续模式（与当前默认一致）。
     */
    private suspend fun resetAllTaskExecutionsStatus(taskId: Long, configItemCount: Int) {
        var updated = 0
        for (index in 0 until configItemCount) {
            val execution =
                taskRepository.getTaskExecutionByTaskIdAndIndex(taskId, index) ?: continue
            val modeTo = if (execution.operationMode == OperationMode.JOG) {
                OperationMode.CONTINUOUS
            } else {
                execution.operationMode
            }
            val resetExecution = execution.copy(
                status = TaskStatus.STOPPED,
                speed = DEFAULT_SPEED_SEC_PER_REV,
                operationMode = modeTo
            )
            if (resetExecution != execution) {
                taskRepository.insertOrUpdateTaskExecution(resetExecution)
                updated++
            }
        }
        EngineLog.i(
            TAG,
            "resetAllTaskExecutionsStatus: taskId=$taskId subtasks=$configItemCount rowsUpdated=$updated"
        )
    }

    private fun loadTaskExecution(taskId: Long, configItemIndex: Int) {
        viewModelScope.launch { loadTaskExecutionBlocking(taskId, configItemIndex) }
    }

    private suspend fun loadTaskExecutionBlocking(taskId: Long, configItemIndex: Int) {
        val task = withContext(Dispatchers.Main) { _task.value }
            ?: taskRepository.getTaskById(taskId)
            ?: return

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
                playbackSpeed = previousExecution?.playbackSpeed
                    ?: TaskExecution.DEFAULT_PLAYBACK_SEC_PER_REV,
            )
            taskRepository.insertOrUpdateTaskExecution(execution)
        }

        val model = engineRepository.getModelWithConfigItemsById(task.modelId)
        val configItem = if (model != null && configItemIndex < task.configItemIds.size) {
            val configItemId = task.configItemIds[configItemIndex]
            model.configItems.find { it.id == configItemId }
        } else {
            null
        }
        val records = taskRepository.getTaskRecordsByTaskIdAndIndex(taskId, configItemIndex)

        withContext(Dispatchers.Main) {
            _taskExecution.value = execution
            _currentConfigItem.value = configItem
            updateTaskIndex(task, configItemIndex)
            _taskRecords.value = records
        }
        EngineLog.i(
            TAG,
            "loadTaskExecutionBlocking: taskId=$taskId gearIndex=$configItemIndex speedSec=${execution.speed} jogInt=${execution.jogInterval}",
        )
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

    /**
     * 切换子任务前：结束点动/连续协程并 [Job.cancelAndJoin]；若仍为 [TaskStatus.RUNNING] 则对当前型号/位置下发 PAUSE/stop 并落库。
     * @return 可安全切换（无需停或停成功）为 true；停失败为 false（不切换子任务）。
     */
    private suspend fun stopCurrentTaskMotionBeforeSwitch(): Boolean {
        val hadJob = jogJob != null
        val job = jogJob
        jogJob = null
        job?.cancelAndJoin()
        if (hadJob) {
            EngineLog.i(TAG, "stopCurrentTaskMotionBeforeSwitch: jogJob 已取消并 join 完成")
        }

        val triple = withContext(Dispatchers.Main) {
            Triple(_taskExecution.value, _task.value?.modelName, _currentConfigItem.value?.position)
        }
        val exec = triple.first
        if (exec?.status != TaskStatus.RUNNING) {
            EngineLog.d(
                TAG,
                "stopCurrentTaskMotionBeforeSwitch: 无需 PAUSE, status=${exec?.status}"
            )
            return true
        }

        val n = triple.second
        val p = triple.third
        if (n == null || p == null) {
            EngineLog.w(TAG, "stopCurrentTaskMotionBeforeSwitch: 缺少型号或位置，无法下发 PAUSE")
            _toastMessage.postValue("切换任务前停止失败: 未选择位置")
            return false
        }

        val pending = exec.copy(status = TaskStatus.PAUSED)
        val reqs = withContext(Dispatchers.Main) {
            requestsForCommand(EngineControlCommand.pause(n, p), commandCtx(pending))
        }
        if (reqs.isEmpty()) {
            EngineLog.w(TAG, "stopCurrentTaskMotionBeforeSwitch: 无 PAUSE CAN 请求")
            _toastMessage.postValue("切换任务前停止失败: 通讯未就绪")
            return false
        }
        EngineLog.i(TAG, "stopCurrentTaskMotionBeforeSwitch: 下发 PAUSE model=$n pos=$p")
        val res = withContext(Dispatchers.IO) { executeCanRequests(reqs) }
        return if (res.success) {
            persistExecutionSync(pending)
            EngineLog.i(TAG, "stopCurrentTaskMotionBeforeSwitch: PAUSE 成功，已落库 PAUSED")
            true
        } else {
            EngineLog.e(TAG, "stopCurrentTaskMotionBeforeSwitch: PAUSE 失败 ${res.error}")
            _toastMessage.postValue("切换任务前停止失败")
            false
        }
    }

    fun switchToTask(index: Int) {
        val task = _task.value ?: return
        if (index < 0 || index >= task.configItemIds.size) return

        EngineLog.i(TAG, "switchToTask: 请求切换到 gearIndex=$index (from=$currentGearRatioIndex)")
        viewModelScope.launch {
            if (!stopCurrentTaskMotionBeforeSwitch()) {
                EngineLog.w(TAG, "switchToTask: 停止当前任务失败，取消切换 index=$index")
                return@launch
            }

            val latest = withContext(Dispatchers.Main) { _taskExecution.value }
            if (latest != null) {
                taskRepository.insertOrUpdateTaskExecution(latest)
            }

            currentGearRatioIndex = index
            loadTaskExecution(task.id, currentGearRatioIndex)
            EngineLog.i(TAG, "switchToTask: 已切换并 loadTaskExecution gearIndex=$index")
        }
    }

    fun start(first: Boolean = false) {
        val task = _task.value ?: return
        val exec = _taskExecution.value ?: return

        EngineLog.i(
            TAG,
            "start(first=$first): mode=${exec.operationMode} status=${exec.status} speedSec=${exec.speed}"
        )
        if (operationStrategy.startCommandPersistsStateOnly) {
            viewModelScope.launch {
                startStateOnly(task, exec, first)
            }
            return
        }
        if (exec.operationMode == OperationMode.JOG) {
            startJogLoop(task, first)
        } else {
            startContinuousLoop(task, first)
        }
    }

    /** 开发模式：开始仅落库 [TaskStatus.RUNNING] 并刷新业务层，不跑点动/连续协程。 */
    private suspend fun startStateOnly(task: Task, exec: TaskExecution, first: Boolean) {
        jogJob?.cancel()
        jogJob = null
        persistExecutionSync(exec.copy(status = TaskStatus.RUNNING))
        withContext(Dispatchers.Main) {
            loadCurrentConfigItem(task, currentGearRatioIndex)
        }
        EngineLog.i(TAG, "startStateOnly: status=RUNNING mode=${exec.operationMode}")
        if (first) {
            _toastMessage.postValue("开发模式：已开始（状态已同步）")
        }
    }

    /** 连续结束：PAUSE/stop 并落库 STOPPED（与点动结束相同，走 [requestsForCommand] 的 PAUSE）。 */
    private suspend fun pauseContinuousAndPersistStopped(toastDone: Boolean) {
        val bundle = withContext(Dispatchers.Main) {
            val cur = _taskExecution.value ?: return@withContext null
            val n = modelName() ?: return@withContext null
            val p = position() ?: return@withContext null
            val pauseCmd = EngineControlCommand.pause(n, p)
            val reqs = requestsForCommand(pauseCmd, commandCtx(cur))
            if (reqs.isEmpty()) null else reqs to cur.copy(status = TaskStatus.STOPPED)
        }
        if (bundle == null) {
            EngineLog.w(
                TAG,
                "pauseContinuousAndPersistStopped(toastDone=$toastDone): 无 bundle，跳过"
            )
            return
        }
        val (pauseRequests, stoppedExec) = bundle
        if (executeCanRequests(pauseRequests).success) {
            persistExecutionSync(stoppedExec)
            EngineLog.i(
                TAG,
                "pauseContinuousAndPersistStopped: 成功，status=STOPPED toastDone=$toastDone"
            )
            if (toastDone) {
                _toastMessage.postValue("连续运行结束，已停止")
            }
        } else {
            EngineLog.e(TAG, "pauseContinuousAndPersistStopped: 失败")
            _toastMessage.postValue("连续结束停止失败")
        }
    }

    /**
     * 等待约「一整圈」对应的墙钟时间（由当前 [TaskExecution.speed] 秒/圈决定）。
     * 分段 [delay] 并每步重新读速度；同时在同一协程内每 [CONTINUOUS_PROGRESS_TOAST_MS] 提示一次进度。
     *
     * @param completedCircles 本段等待开始前已完成的圈数；界面展示为「第 completedCircles+1 圈」进行中。
     */
    private suspend fun delayContinuousRevolutionSlice(completedCircles: Int): Boolean {
        var elapsed = 0L
        var msSinceProgressToast = 0L
        var lastTarget =
            withContext(Dispatchers.Main) {
                _taskExecution.value?.continuousCycles?.coerceAtLeast(1) ?: 1
            }
        while (currentCoroutineContext().isActive) {
            val ex = withContext(Dispatchers.Main) { _taskExecution.value } ?: return false
            if (ex.operationMode != OperationMode.CONTINUOUS || ex.status != TaskStatus.RUNNING) return false
            val realTargetMs =
                (ex.speed.coerceAtLeast(MIN_SPEED_SEC_PER_REV) * 1000.0).toLong().coerceAtLeast(1L)
            val targetMs = operationStrategy.motionDelayMs(realTargetMs)
            if (elapsed >= targetMs) return true
            val remaining = targetMs - elapsed
            val step = minOf(CONTINUOUS_SPEED_POLL_MS, remaining).coerceAtLeast(1L)
            delay(step)
            elapsed += step
            msSinceProgressToast += step

            val exT = withContext(Dispatchers.Main) { _taskExecution.value } ?: return false
            if (exT.operationMode != OperationMode.CONTINUOUS || exT.status != TaskStatus.RUNNING) return false
            val target = exT.continuousCycles.coerceAtLeast(1)
            if (target != lastTarget) {
                lastTarget = target
                msSinceProgressToast = 0L
                if (completedCircles < target) {
                    _toastMessage.postValue("连续执行中: 第${completedCircles + 1}/$target 圈")
                }
            } else if (msSinceProgressToast >= CONTINUOUS_PROGRESS_TOAST_MS) {
                msSinceProgressToast = 0L
                if (completedCircles < target) {
                    _toastMessage.postValue("连续执行中: 第${completedCircles + 1}/$target 圈")
                }
            }
        }
        return false
    }

    /**
     * 连续模式：只下发一次 [EngineControlCommand.continuous]（映射为 [CANOpenHelper.startSpeedMode]）；
     * 多圈仅按「每圈耗时 = speed（秒/圈）」做等待与进度提示，不再每圈重复下发连续指令。
     * 进度提示：每圈在 [delayContinuousRevolutionSlice] 之前先立刻 Toast 一次；片内约每 1s 重复提示，且设置里目标圈数变化时立刻刷新。
     */
    private fun startContinuousLoop(task: Task, first: Boolean) {
        jogJob?.cancel()
        EngineLog.i(TAG, "startContinuousLoop: 启动协程 taskId=${task.id} first=$first")
        jogJob = viewModelScope.launch(Dispatchers.IO) {
            val positionStr = position() ?: "1"

            val execForStart =
                withContext(Dispatchers.Main) { _taskExecution.value } ?: return@launch
            if (execForStart.operationMode != OperationMode.CONTINUOUS) {
                EngineLog.w(TAG, "startContinuousLoop: 非连续模式，退出")
                return@launch
            }

            val continuousCmd = EngineControlCommand.continuous(task.modelName, positionStr)
            val moveRequests = requestsForCommand(continuousCmd, commandCtx(execForStart))
            if (moveRequests.isEmpty()) {
                EngineLog.w(TAG, "startContinuousLoop: 无 CAN 请求")
                return@launch
            }
            if (!executeCanRequests(moveRequests).success) {
                val msg = if (first) "启动失败" else "连续启动失败"
                EngineLog.e(TAG, "startContinuousLoop: CONTINUOUS 下发失败")
                _toastMessage.postValue(msg)
                return@launch
            }

            EngineLog.i(
                TAG,
                "startContinuousLoop: CONTINUOUS 下发成功 pos=$positionStr cycles=${execForStart.continuousCycles}"
            )
            val cur = withContext(Dispatchers.Main) { _taskExecution.value } ?: return@launch
            persistExecutionSync(
                cur.copy(status = TaskStatus.RUNNING, operationMode = OperationMode.CONTINUOUS)
            )
            withContext(Dispatchers.Main) {
                loadCurrentConfigItem(task, currentGearRatioIndex)
            }

            var circlesDone = 0
            while (isActive) {
                val ex = withContext(Dispatchers.Main) { _taskExecution.value } ?: break
                if (ex.operationMode != OperationMode.CONTINUOUS) {
                    EngineLog.i(TAG, "startContinuousLoop: 模式已非连续，停止并落库")
                    pauseContinuousAndPersistStopped(toastDone = false)
                    return@launch
                }
                if (ex.status != TaskStatus.RUNNING) break

                val target = ex.continuousCycles.coerceAtLeast(1)

                if (circlesDone >= target) {
                    EngineLog.i(TAG, "startContinuousLoop: 已达目标圈数 $target，正常结束")
                    pauseContinuousAndPersistStopped(toastDone = true)
                    return@launch
                }

                // 每进入新的一圈（含首次）先立刻提示，再进入等一圈的 delay，避免整段 delay 结束才看到圈数变化
                _toastMessage.postValue("连续执行中: 第${circlesDone + 1}/$target 圈")

                if (!delayContinuousRevolutionSlice(circlesDone)) break

                val exAfter = withContext(Dispatchers.Main) { _taskExecution.value } ?: break
                if (exAfter.operationMode != OperationMode.CONTINUOUS) {
                    pauseContinuousAndPersistStopped(toastDone = false)
                    return@launch
                }
                if (exAfter.status != TaskStatus.RUNNING) break

                circlesDone++
                val tShow = exAfter.continuousCycles.coerceAtLeast(1)

                if (circlesDone >= tShow) {
                    pauseContinuousAndPersistStopped(toastDone = true)
                    return@launch
                }
            }

            pauseContinuousAndPersistStopped(toastDone = false)
        }
    }

    private fun startJogLoop(task: Task, first: Boolean) {
        jogJob?.cancel()
        jogJob = viewModelScope.launch(Dispatchers.IO) {
            val initialExec = _taskExecution.value ?: return@launch
            val ctx = commandContext ?: return@launch
            val bladeCount = _currentConfigItem.value?.bladeCount ?: 10
            val validBladeCount = if (bladeCount > 0) bladeCount else 10
            val cycles = V2InspectionCommandDispatcher.resolveJogCycles(ctx)
            val targetSteps = cycles * validBladeCount
            var stepCount = 0
            var runningPersisted = false

            EngineLog.i(
                TAG,
                "startJogLoop: taskId=${task.id} first=$first targetSteps=$targetSteps blades=$validBladeCount cycles=$cycles"
            )

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
                        val reqs = requestsForCommand(pauseCmd, commandCtx(cur))
                        if (reqs.isEmpty()) null else reqs to cur.copy(status = TaskStatus.STOPPED)
                    }
                    if (bundle != null) {
                        val (pauseRequests, stoppedExec) = bundle
                        if (executeCanRequests(pauseRequests).success) {
                            persistExecutionSync(stoppedExec)
                            EngineLog.i(TAG, "startJogLoop: 点动步数完成，已 PAUSE 落库 STOPPED")
                        } else {
                            EngineLog.e(TAG, "startJogLoop: 点动结束 PAUSE 失败")
                            _toastMessage.postValue("点动结束停止失败")
                        }
                    }
                    break
                }

                val onceRotate = if (ctx.uiMode == com.devicecontrol.engine.v2.viewmodel.V2UiOperationMode.AUTO) {
                    V2InspectionCommandDispatcher.resolveJogStepDegrees(ctx)
                } else {
                    360.0 / validBladeCount
                }
                val gearRatio = getRealGearRatio()

                val isForward = currentExec.rotationDirection == RotationDirection.FORWARD
                val signedDegrees = if (isForward) onceRotate else -onceRotate
                val relativePulses =
                    ((signedDegrees / 360.0) * encoderResolution * gearRatio).toInt()
                val speedSec = V2InspectionCommandDispatcher.resolveSpeedSecPerRev(commandCtx(currentExec))
                val pbVelocity = canVelocityFromSecPerRev(speedSec, gearRatio)

                val requests = CANOpenHelper.startRelativePositionMode(
                    kotlin.math.abs(pbVelocity),
                    relativePulses
                )
                if (executeCanRequests(requests).success) {
                    if (!runningPersisted) {
                        runningPersisted = true
                        val cur = withContext(Dispatchers.Main) { _taskExecution.value } ?: break
                        persistExecutionSync(
                            cur.copy(status = TaskStatus.RUNNING, operationMode = OperationMode.JOG)
                        )

                        withContext(Dispatchers.Main) {
                            loadCurrentConfigItem(task, currentGearRatioIndex)
                        }
                    }
                    val msg = if (first) "启动成功" else "点动执行中"
                    _toastMessage.postValue(
                        "$msg: ${stepCount + 1}/$targetSteps (${
                            java.text.DecimalFormat(
                                "#0.0"
                            ).format(onceRotate)
                        }度)"
                    )
                } else {
                    val msg = if (first) "启动失败" else "点动下发失败"
                    EngineLog.e(TAG, "startJogLoop: 相对位移失败")
                    _toastMessage.postValue(msg)
                    break
                }

                stepCount++
                val holdSec = V2InspectionCommandDispatcher.resolveJogHoldSec(commandCtx(currentExec))
                delay(operationStrategy.motionDelayMs(holdSec * 1000L))
            }
        }
    }

    fun pause() {
        jogJob?.cancel()
        jogJob = null
        val name = modelName() ?: return
        val pos = position() ?: return
        val exec = _taskExecution.value ?: return
        EngineLog.i(TAG, "pause: 用户暂停 model=$name pos=$pos 原状态=${exec.status}")
        val pending = exec.copy(status = TaskStatus.PAUSED)
        sendCommandThenPersist(EngineControlCommand.pause(name, pos), pending) {
            val task = _task.value ?: return@sendCommandThenPersist
            loadCurrentConfigItem(task, currentGearRatioIndex)
        }
    }

    fun setForward() {
        val exec = _taskExecution.value ?: return
        EngineLog.i(TAG, "setForward: 运行中切换正转")
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
        EngineLog.i(TAG, "setReverse: 运行中切换反转")
        val pending = exec.copy(rotationDirection = RotationDirection.REVERSE)
        val n = modelName() ?: return
        val p = position() ?: return
        sendCommandThenPersist(EngineControlCommand.reverse(n, p), pending) {
            val task = _task.value ?: return@sendCommandThenPersist
            loadCurrentConfigItem(task, currentGearRatioIndex)
        }
    }

    fun setJog() {
        EngineLog.i(TAG, "setJog: 切点动并 start")
        applyExecutionToLiveDataOnly { it.copy(operationMode = OperationMode.JOG) }
        start()
    }

    /** 与 [setJog] 对称：只改内存模式，通过 [start] 走 [startContinuousLoop]（每圈相对位移，与点动同下发路径）。 */
    fun setContinuous() {
        EngineLog.i(TAG, "setContinuous: 切连续并 start")
        applyExecutionToLiveDataOnly { it.copy(operationMode = OperationMode.CONTINUOUS) }
        start()
    }

    /** V2 手动：仅切换点动模式，不自动 start */
    fun setOperationModeJogOnly() {
        if (operationStrategy.allowsPersistWithoutCan) {
            updateExecution { it.copy(operationMode = OperationMode.JOG) }
        } else {
            applyExecutionToLiveDataOnly { it.copy(operationMode = OperationMode.JOG) }
        }
    }

    /** V2 手动：仅切换连续模式，不自动 start */
    fun setOperationModeContinuousOnly() {
        if (operationStrategy.allowsPersistWithoutCan) {
            updateExecution { it.copy(operationMode = OperationMode.CONTINUOUS) }
        } else {
            applyExecutionToLiveDataOnly { it.copy(operationMode = OperationMode.CONTINUOUS) }
        }
    }

    fun isSlcanReady(): Boolean = slcanManager?.state == SlcanManager.State.READY

    fun deleteRecord(record: TaskRecord) {
        viewModelScope.launch {
            taskRepository.deleteTaskRecord(record)
            val task = _task.value ?: return@launch
            loadTaskRecords(task.id, currentGearRatioIndex)
        }
    }

    fun increaseSpeed() {
        val execution = _taskExecution.value ?: return
        val ctx = commandContext ?: return
        val current = V2InspectionCommandDispatcher.resolveSpeedSecPerRev(ctx)
        val step = V2InspectionCommandDispatcher.resolveSpeedStepSecPerRev(ctx)
        val newSpeed = (current - step).coerceAtLeast(MIN_SPEED_SEC_PER_REV)
        EngineLog.d(TAG, "increaseSpeed: ${current}s -> ${newSpeed}s per rev")
        commandContext = ctx.copy(speedSecOverride = newSpeed)
        val n = modelName() ?: return
        val p = position() ?: return
        sendCommandThenPersist(EngineControlCommand.speedPlus(n, p), execution)
    }

    fun decreaseSpeed() {
        val execution = _taskExecution.value ?: return
        val ctx = commandContext ?: return
        val current = V2InspectionCommandDispatcher.resolveSpeedSecPerRev(ctx)
        val step = V2InspectionCommandDispatcher.resolveSpeedStepSecPerRev(ctx)
        val newSpeed = current + step
        EngineLog.d(TAG, "decreaseSpeed: ${current}s -> ${newSpeed}s per rev")
        commandContext = ctx.copy(speedSecOverride = newSpeed)
        val n = modelName() ?: return
        val p = position() ?: return
        sendCommandThenPersist(EngineControlCommand.speedMinus(n, p), execution)
    }

    /**
     * @param initialSpeedMinutesPerRev 配置初始速度，单位：分钟/圈；会写入 [TaskExecution.speed]（内部为秒/圈）。
     */
    fun updateSettings(
        initialSpeedMinutesPerRev: Double,
        speedStep: Double,
        continuousCycles: Int,
        jogInterval: Int,
        playbackSpeed: Double
    ) {
        val speedSec = initialSpeedMinutesPerRev * 60.0
        EngineLog.i(
            TAG,
            "updateSettings: speedSec=$speedSec (min/rev=$initialSpeedMinutesPerRev) step=$speedStep cycles=$continuousCycles jogInt=$jogInterval playback=$playbackSpeed"
        )
        updateExecution {
            it.copy(
                speed = speedSec,
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

    fun addRecord(position: Int, bladeCount: Int) {
        val task = _task.value ?: return
        EngineLog.i(
            TAG,
            "addRecord: taskId=${task.id} gearIndex=$currentGearRatioIndex position=$position bladeCount=$bladeCount"
        )
        viewModelScope.launch {
            if (!operationStrategy.requiresDeviceConnection) {
                insertSimulatedRecord(task, position, bladeCount)
                return@launch
            }
            val outcome = executeCanRequests(CANOpenHelper.readPosition())
            if (!outcome.success) {
                EngineLog.e(TAG, "记录失败: ${outcome.error}")
                _toastMessage.postValue("获取失败: ${outcome.error ?: "未知"}")
                return@launch
            }
            val actualPos = outcome.values[CiA402.ActualPosition.name] as? Int
            if (actualPos == null) {
                EngineLog.w(TAG, "addRecord: 读位置成功但 ActualPosition 为空")
                return@launch
            }
            val gearRatio = getRealGearRatio()
            val rawAngle = actualPos * 360.0 / (gearRatio * encoderResolution)
            val recordAngle = normalizeAngleDegrees0To360(rawAngle)
            insertRecordFromAngle(task, position, bladeCount, recordAngle, actualPos)
        }
    }

    private suspend fun insertSimulatedRecord(task: Task, position: Int, bladeCount: Int) {
        val blades = bladeCount.coerceAtLeast(1)
        val existing = taskRepository.getTaskRecordsByTaskIdAndIndex(task.id, currentGearRatioIndex)
        val index = existing.size % blades
        val recordAngle = index * (360.0 / blades)
        insertRecordFromAngle(task, position, bladeCount, recordAngle, actualPosHint = -1)
        _toastMessage.postValue("开发模式：已模拟记录 (${recordAngle.toInt()}°)")
    }

    private suspend fun insertRecordFromAngle(
        task: Task,
        position: Int,
        bladeCount: Int,
        recordAngle: Double,
        actualPosHint: Int,
    ) {
        val blades = bladeCount.coerceAtLeast(1)
        val bladeNumber = (blades * recordAngle / 360.0).toInt().coerceIn(0, blades)
        val record = TaskRecord(
            taskId = task.id,
            gearRatioIndex = currentGearRatioIndex,
            recordNumber = 0,
            position = position,
            angleDegrees = recordAngle.toFloat(),
            bladeNumber = bladeNumber,
        )
        taskRepository.insertTaskRecord(record)
        loadTaskRecords(task.id, currentGearRatioIndex)
        EngineLog.i(
            TAG,
            "addRecord: 成功 actualPos=$actualPosHint angleDeg=${record.angleDegrees} blade=${record.bladeNumber}",
        )
        _toastMessage.postValue("记录位置获取成功: ${record.angleDegrees}")
    }

    fun playbackRecord(record: TaskRecord) {
        EngineLog.i(
            TAG,
            "playbackRecord: recordId=${record.recordId} angle=${record.angleDegrees}° pulses将按当前减速比换算"
        )
        val pbSpeed = commandContext?.let { V2InspectionCommandDispatcher.resolvePlaybackSpeedSecPerRev(it) }
            ?: execution()?.playbackSpeed
            ?: TaskExecution.DEFAULT_PLAYBACK_SEC_PER_REV
        val gearRatio = getRealGearRatio()
        // 回查速度同为秒/圈，与主运行速度使用同一换算（耗时越短 → 下发速度越大）
        val pbVelocity = canVelocityFromSecPerRev(pbSpeed, gearRatio)
        val positionAngle = record.angleDegrees
        val pulses = ((positionAngle / 360.0) * encoderResolution * gearRatio).toInt()

        viewModelScope.launch {
            val reqs = CANOpenHelper.startPositionMode(Math.abs(pbVelocity), pulses)
            if (executeCanRequests(reqs).success) {
                EngineLog.i(
                    TAG,
                    "playbackRecord: 位置模式下发成功 pulses=$pulses profileVel=$pbVelocity"
                )
                val cur = withContext(Dispatchers.Main) { _taskExecution.value }
                if (cur != null && cur.status != TaskStatus.RUNNING) {
                    persistExecutionSync(cur.copy(status = TaskStatus.RUNNING))
                    EngineLog.i(TAG, "playbackRecord: 执行状态已同步为 RUNNING（原=${cur.status}）")
                }
                _toastMessage.postValue(
                    if (operationStrategy.requiresDeviceConnection) {
                        "已触发回溯指令: $positionAngle 度"
                    } else {
                        "开发模式：已模拟回溯 $positionAngle 度"
                    },
                )
            } else {
                EngineLog.e(TAG, "playbackRecord: 失败")
                _toastMessage.postValue("回溯指令失败")
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

    /** 只改 [taskExecution] 内存，不写库；点动/连续以首包 CAN 成功后的 [persistExecutionSync] 再落库。 */
    private fun applyExecutionToLiveDataOnly(update: (TaskExecution) -> TaskExecution) {
        val execution = _taskExecution.value ?: return
        _taskExecution.value = update(execution)
    }

    /** 在协程内同步落库：主线程更新 LiveData，再 suspend 写 Room（供点动循环等 IO 协程使用） */
    private suspend fun persistExecutionSync(execution: TaskExecution) {
        withContext(Dispatchers.Main) {
            _taskExecution.value = execution
        }
        taskRepository.insertOrUpdateTaskExecution(execution)
    }

    /**
     * 按逻辑指令与 [V2InspectionCommandContext] 构建 CAN 写序列（配置实体由 Dispatcher 读取）。
     */
    private fun requestsForCommand(cmd: String, ctx: V2InspectionCommandContext): List<SlcanRequest> =
        V2InspectionCommandDispatcher.buildRequests(cmd, ctx, getRealGearRatio())

    /**
     * 先下发 CAN，**仅成功时** 将 [executionAfterSuccess] 落库并刷新 LiveData；失败则提示且不修改数据库。
     */
    private fun sendCommandThenPersist(
        cmd: String,
        executionAfterSuccess: TaskExecution,
        onSuccess: () -> Unit = {}
    ) {
        val requests = requestsForCommand(cmd, commandCtx(executionAfterSuccess))
        if (requests.isEmpty()) {
            if (!operationStrategy.allowsPersistWithoutCan) {
                EngineLog.d(TAG, "sendCommandThenPersist: 未映射 CAN 指令, $cmd")
                return
            }
            viewModelScope.launch {
                persistExecutionSync(executionAfterSuccess)
                EngineLog.i(TAG, "sendCommandThenPersist: 开发模式无 CAN，直接落库 $cmd")
                onSuccess()
            }
            return
        }
        viewModelScope.launch {
            val outcome = executeCanRequests(requests)
            if (outcome.success) {
                persistExecutionSync(executionAfterSuccess)
                EngineLog.i(TAG, "sendCommandThenPersist: 成功 $cmd")
                if (operationStrategy.requiresDeviceConnection) {
                    _toastMessage.value = "命令发送成功"
                }
                onSuccess()
            } else {
                EngineLog.e(TAG, "sendCommandThenPersist: 失败 $cmd, error=${outcome.error}")
                _toastMessage.value = "发送失败: ${outcome.error ?: "未知错误"}"
            }
        }
    }

    private fun modelName(): String? = _task.value?.modelName
    private fun position(): String? = _currentConfigItem.value?.position
    private fun execution(): TaskExecution? = _taskExecution.value

    override fun onCleared() {
        super.onCleared()
    }

    fun destroy() {
        EngineLog.i(TAG, "destroy: 释放通讯 slcanState=${slcanManager?.state}")
        SlcanEmergencyClose.unbind()
        if (slcanManager?.state == SlcanManager.State.READY) {
            MainScope().launch {
                pause()
                slcanManager?.close()
                vcpManager?.release()
                EngineLog.i(TAG, "destroy: SLCAN 已 close，VCP 已 release")
            }
        } else {
            vcpManager?.release()
            EngineLog.i(TAG, "destroy: 非 READY，仅 release VCP")
        }

    }
}

