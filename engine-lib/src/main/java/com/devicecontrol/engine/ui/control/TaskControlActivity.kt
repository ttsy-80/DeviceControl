package com.devicecontrol.engine.ui.control

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.devicecontrol.engine.R
import com.devicecontrol.engine.data.database.AppDatabase
import com.devicecontrol.engine.data.model.OperationMode
import com.devicecontrol.engine.data.model.TaskStatus
import com.devicecontrol.engine.data.repository.EngineRepository
import com.devicecontrol.engine.data.repository.TaskRepository
import com.devicecontrol.engine.databinding.ActivityTaskControlBinding
import com.devicecontrol.engine.databinding.DialogRecordDetailBinding
import com.devicecontrol.engine.databinding.DialogSendInstructionBinding
import com.devicecontrol.engine.viewmodel.TaskControlViewModel
import kotlin.math.roundToInt

class TaskControlActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskControlBinding
    private lateinit var recordAdapter: TaskRecordAdapter

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val taskRepository by lazy { TaskRepository(database.taskDao()) }
    private val engineRepository by lazy { EngineRepository(database.engineDao()) }
    private val viewModel: TaskControlViewModel by viewModels {
        TaskControlViewModelFactory(taskRepository, engineRepository, applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskControlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Setup Records RecyclerView
        recordAdapter = TaskRecordAdapter { record ->
            showRecordDetailDialog(record)
        }
        binding.rvRecords.layoutManager = LinearLayoutManager(this)
        binding.rvRecords.adapter = recordAdapter

        val taskId = intent.getLongExtra("taskId", -1)
        if (taskId == -1L) {
            Toast.makeText(this, "无效的任务ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.loadTask(taskId)
        setupObservers()
        setupListeners()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(com.devicecontrol.engine.R.menu.menu_task_control, menu)
        menu.findItem(com.devicecontrol.engine.R.id.action_debug_log)?.isVisible = true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            com.devicecontrol.engine.R.id.action_send_instruction -> {
                showSendInstructionDialog()
                true
            }
            com.devicecontrol.engine.R.id.action_debug_log -> {
                startActivity(Intent(this, DebugLogActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSendInstructionDialog() {
        val dialogBinding = DialogSendInstructionBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnConfirm.setOnClickListener {
            val text = dialogBinding.etInstruction.text?.toString().orEmpty()
            viewModel.sendTestInstruction(text)
            if (text.isNotEmpty()) {
//                Toast.makeText(this, "指令已发送", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setupObservers() {
        viewModel.taskIndex.observe(this) { index ->
            binding.tvTaskIndex.text = "$index"
        }

        viewModel.toastMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        viewModel.taskExecution.observe(this) { execution ->
            execution?.let { updateButtonStates(it) }
            updateStatusBar()
        }

        viewModel.task.observe(this) { task ->
            task?.let {
                // 如果任务有多个配置项，显示任务切换按钮和任务序号
                val hasMultipleTasks = it.configItemIds.size > 1
                if (hasMultipleTasks) {
                    binding.btnSwitchTask.visibility = android.view.View.VISIBLE
                    binding.tvTaskIndex.visibility = android.view.View.VISIBLE
                } else {
                    binding.btnSwitchTask.visibility = android.view.View.GONE
                    binding.tvTaskIndex.visibility = android.view.View.GONE
                }
            }
            updateStatusBar()
        }
        
        viewModel.taskRecords.observe(this) { records ->
            recordAdapter.submitList(records)
        }
        
        viewModel.currentConfigItem.observe(this) { configItem ->
            configItem?.let {
                recordAdapter.setBladeCount(it.bladeCount)
            }
            updateStatusBar()
        }

        updateStatusBar()
    }
    
    /** 型号 + 点动/连续 + 每圈耗时文案（与 [formatSpeedPerRevolution] 一致） */
    private fun updateStatusBar() {
        val modelName = viewModel.task.value?.modelName.orEmpty()
        val execution = viewModel.taskExecution.value
        val operationModeText = when (execution?.operationMode) {
            OperationMode.JOG -> getString(R.string.jog)
            OperationMode.CONTINUOUS -> getString(R.string.continuous)
            null -> getString(R.string.jog)
        }
        val speedSec = execution?.speed ?: 0.0
        val speedText = formatSpeedPerRevolution(speedSec)
        binding.tvStatusBar.text = getString(R.string.task_control_status_bar, modelName, operationModeText, speedText)
    }

    /**
     * [speedSecPerRev] 为每圈耗时（秒）。
     * 不足 1 分钟只显示「X秒一圈」；满整分钟显示「X分一圈」；否则「X分Y秒一圈」。
     */
    private fun formatSpeedPerRevolution(speedSecPerRev: Double): String {
        val totalSec = speedSecPerRev.roundToInt().coerceAtLeast(0)
        if (totalSec < 60) {
            return getString(R.string.speed_display_seconds_only, totalSec)
        }
        val minutes = totalSec / 60
        val seconds = totalSec % 60
        return if (seconds == 0) {
            getString(R.string.speed_display_minutes_only, minutes)
        } else {
            getString(R.string.speed_display_min_sec, minutes, seconds)
        }
    }

    private fun updateButtonStates(execution: com.devicecontrol.engine.data.model.TaskExecution) {
        val isRunning = execution.status == TaskStatus.RUNNING
        
        // 更新启动/暂停按钮状态 - 使用enabled状态控制selector
        binding.btnStartPause.isEnabled = !isRunning
        
        if (isRunning) {
            // 运行状态：暂停、速度+/速度-/记录可以点击
            binding.btnPause.isEnabled = true
            binding.btnSpeedPlus.isEnabled = true
            binding.btnSpeedMinus.isEnabled = true
            binding.btnAddRecord.isEnabled = true
            
            // 正转/反转按钮：按照各自现有的状态显示可点击还是不可以点击状态
            // 当前是正转时，正转按钮不可点击（已选中），反转按钮可点击（可切换）
            // 当前是反转时，反转按钮不可点击（已选中），正转按钮可点击（可切换）
            val isForward = execution.rotationDirection == com.devicecontrol.engine.data.model.RotationDirection.FORWARD
            // 当前是正转时，正转按钮禁用（已选中状态），反转按钮启用（可切换）
            // 当前是反转时，反转按钮禁用（已选中状态），正转按钮启用（可切换）
            binding.btnForward.isEnabled = !isForward
            binding.btnReverse.isEnabled = isForward
            
            // 连续/点动按钮：按照各自现有的状态显示可点击还是不可以点击状态
            // 当前是点动时，点动按钮不可点击（已选中），连续按钮可点击（可切换）
            // 当前是连续时，连续按钮不可点击（已选中），点动按钮可点击（可切换）
            val isJog = execution.operationMode == com.devicecontrol.engine.data.model.OperationMode.JOG
            // 当前是点动时，点动按钮禁用（已选中状态），连续按钮启用（可切换）
            // 当前是连续时，连续按钮禁用（已选中状态），点动按钮启用（可切换）
            binding.btnJog.isEnabled = !isJog
            binding.btnContinuous.isEnabled = isJog
            
            // 拍照按钮：运行状态时可以点击
            binding.btnPhoto.isEnabled = true
        } else {
            // 非运行状态：暂停、正转/反转/拍照、速度+/速度-/记录、连续/点动按钮都不能点击
            binding.btnPause.isEnabled = false
            binding.btnForward.isEnabled = false
            binding.btnReverse.isEnabled = false
            binding.btnPhoto.isEnabled = false
            binding.btnSpeedPlus.isEnabled = false
            binding.btnSpeedMinus.isEnabled = false
            binding.btnAddRecord.isEnabled = false
            binding.btnJog.isEnabled = false
            binding.btnContinuous.isEnabled = false
        }
    }
    
    private fun showRecordDetailDialog(record: com.devicecontrol.engine.data.model.TaskRecord) {
        val dialogBinding = DialogRecordDetailBinding.inflate(layoutInflater)
        dialogBinding.tvPosition.text = record.position.toString()
        dialogBinding.tvAngle.text = String.format("%.2f", record.angleDegrees)
        dialogBinding.tvBladeNumber.text = getString(R.string.blade_number_label, record.bladeNumber)
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        
        dialogBinding.btnPlayback.setOnClickListener {
            viewModel.playbackRecord(record)
            dialog.dismiss()
        }
        
        dialog.show()
    }

    private fun setupListeners() {
        binding.btnStartPause.setOnClickListener {
            viewModel.start(true)
        }

        binding.btnPause.setOnClickListener {
            viewModel.pause()
        }

        binding.btnForward.setOnClickListener {
            viewModel.setForward()
        }

        binding.btnReverse.setOnClickListener {
            viewModel.setReverse()
        }

        binding.btnJog.setOnClickListener {
            viewModel.setJog()
        }

        binding.btnContinuous.setOnClickListener {
            viewModel.setContinuous()
        }

        binding.btnSpeedPlus.setOnClickListener {
            viewModel.increaseSpeed()
        }

        binding.btnSpeedMinus.setOnClickListener {
            viewModel.decreaseSpeed()
        }

        binding.btnPhoto.setOnClickListener {
            viewModel.takePhoto()
        }

        binding.btnSwitchTask.setOnClickListener {
            showTaskSwitchDialog()
        }

        binding.btnAddRecord.setOnClickListener {
            showAddRecordDialog()
        }

        binding.btnSettings.setOnClickListener {
            showSettingsDialog()
        }
    }
    
    private fun showSettingsDialog() {
        val execution = viewModel.taskExecution.value ?: return
        
        val minutesPerRev = execution.speed / 60.0
        val dialog = SettingsDialog(
            currentInitialSpeedMinutesPerRev = minutesPerRev,
            currentSpeedStep = execution.speedStep,
            currentContinuousCycles = execution.continuousCycles,
            currentJogInterval = execution.jogInterval,
            currentPlaybackSpeed = execution.playbackSpeed
        ) { initialMinutesPerRev, speedStep, continuousCycles, jogInterval, playbackSpeed ->
            viewModel.updateSettings(initialMinutesPerRev, speedStep, continuousCycles, jogInterval, playbackSpeed)
        }
        dialog.show(supportFragmentManager, "SettingsDialog")
    }
    
    private fun showTaskSwitchDialog() {
        val taskItems = viewModel.getTaskItems()
        if (taskItems.isEmpty()) {
            Toast.makeText(this, "没有可切换的任务", Toast.LENGTH_SHORT).show()
            return
        }
        
        val currentIndex = viewModel.getCurrentTaskIndex()
        val dialog = TaskSwitchDialog(
            taskItems = taskItems,
            currentTaskIndex = currentIndex
        ) { selectedIndex ->
            viewModel.switchToTask(selectedIndex)
        }
        dialog.show(supportFragmentManager, "TaskSwitchDialog")
    }
    
    private fun showAddRecordDialog() {
        val task = viewModel.task.value ?: return
        val configItem = viewModel.currentConfigItem.value ?: return
        
        // 获取位置数据
        val position = configItem.position.toIntOrNull() ?: 1
        
        // 生成随机叶片数（1到配置项的bladeCount之间）
        val bladeCount = configItem.bladeCount
        // 创建记录
        viewModel.addRecord(position, bladeCount)
    }

    override fun onDestroy() {
        viewModel.destroy()
        super.onDestroy()
    }
}

// ViewModel Factory
class TaskControlViewModelFactory(
    private val taskRepository: TaskRepository,
    private val engineRepository: EngineRepository,
    private val applicationContext: Context
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskControlViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskControlViewModel(taskRepository, engineRepository, applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
