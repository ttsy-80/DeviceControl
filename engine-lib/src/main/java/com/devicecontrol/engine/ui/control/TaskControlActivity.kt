package com.devicecontrol.engine.ui.control

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.devicecontrol.engine.R
import com.devicecontrol.engine.data.database.AppDatabase
import com.devicecontrol.engine.data.model.TaskStatus
import com.devicecontrol.engine.data.repository.EngineRepository
import com.devicecontrol.engine.data.repository.TaskRepository
import com.devicecontrol.engine.databinding.ActivityTaskControlBinding
import com.devicecontrol.engine.databinding.DialogRecordDetailBinding
import com.devicecontrol.engine.viewmodel.TaskControlViewModel
import java.text.DecimalFormat

class TaskControlActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskControlBinding
    private lateinit var recordAdapter: TaskRecordAdapter

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val taskRepository by lazy { TaskRepository(database.taskDao()) }
    private val engineRepository by lazy { EngineRepository(database.engineDao()) }
    private val viewModel: TaskControlViewModel by viewModels {
        TaskControlViewModelFactory(taskRepository, engineRepository)
    }

    private val decimalFormat = DecimalFormat("#0.0")

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

    private fun setupObservers() {
        viewModel.displayInfo.observe(this) { info ->
            // 更新状态栏显示
            updateStatusBar(info)
        }

        viewModel.taskIndex.observe(this) { index ->
            binding.tvTaskIndex.text = "任务: $index"
        }

        viewModel.taskExecution.observe(this) { execution ->
            execution?.let {
                updateStatusBar(viewModel.displayInfo.value ?: "")
            }
        }

        viewModel.taskExecution.observe(this) { execution ->
            execution?.let {
                updateSpeedDisplay(it.speed)
                updateButtonStates(it)
            }
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
        }
        
        viewModel.taskRecords.observe(this) { records ->
            recordAdapter.submitList(records)
        }
        
        viewModel.currentConfigItem.observe(this) { configItem ->
            configItem?.let {
                recordAdapter.setBladeCount(it.bladeCount)
            }
        }
    }

    private fun updateSpeedDisplay(speed: Double) {
        // 速度显示已整合到状态栏中
    }
    
    private fun updateStatusBar(displayInfo: String) {
        val execution = viewModel.taskExecution.value
        val speed = execution?.speed ?: 0.0
        val speedText = "速度: ${decimalFormat.format(speed)}${getString(R.string.speed_unit)}"
        binding.tvStatusBar.text = "状态栏: $displayInfo $speedText"
    }

    private fun updateButtonStates(execution: com.devicecontrol.engine.data.model.TaskExecution) {
        val activeColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FF6B35"))
        val inactiveColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E7E0EC"))
        val activeTextColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFFFFF"))
        val inactiveTextColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#49454F"))
        
        // 更新启动/暂停按钮状态
        val isRunning = execution.status == com.devicecontrol.engine.data.model.TaskStatus.RUNNING
        if (isRunning) {
            binding.btnStartPause.backgroundTintList = inactiveColor
            binding.btnStartPause.setTextColor(inactiveTextColor)
            binding.btnPause.backgroundTintList = activeColor
            binding.btnPause.setTextColor(activeTextColor)
        } else {
            binding.btnStartPause.backgroundTintList = activeColor
            binding.btnStartPause.setTextColor(activeTextColor)
            binding.btnPause.backgroundTintList = inactiveColor
            binding.btnPause.setTextColor(inactiveTextColor)
        }
        
        // 更新正转/反转按钮状态
        val isForward = execution.rotationDirection == com.devicecontrol.engine.data.model.RotationDirection.FORWARD
        if (isForward) {
            binding.btnForward.backgroundTintList = activeColor
            binding.btnForward.setTextColor(activeTextColor)
            binding.btnReverse.backgroundTintList = inactiveColor
            binding.btnReverse.setTextColor(inactiveTextColor)
        } else {
            binding.btnForward.backgroundTintList = inactiveColor
            binding.btnForward.setTextColor(inactiveTextColor)
            binding.btnReverse.backgroundTintList = activeColor
            binding.btnReverse.setTextColor(activeTextColor)
        }
        
        // 更新点动/连续按钮状态
        val isJog = execution.operationMode == com.devicecontrol.engine.data.model.OperationMode.JOG
        if (isJog) {
            binding.btnJog.backgroundTintList = inactiveColor
            binding.btnJog.setTextColor(inactiveTextColor)
            binding.btnContinuous.backgroundTintList = activeColor
            binding.btnContinuous.setTextColor(activeTextColor)
        } else {
            binding.btnJog.backgroundTintList = inactiveColor
            binding.btnJog.setTextColor(inactiveTextColor)
            binding.btnContinuous.backgroundTintList = activeColor
            binding.btnContinuous.setTextColor(activeTextColor)
        }
    }
    
    private fun showRecordDetailDialog(record: com.devicecontrol.engine.data.model.TaskRecord) {
        val dialogBinding = DialogRecordDetailBinding.inflate(layoutInflater)
        dialogBinding.tvPosition.text = record.position.toString()
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
            viewModel.start()
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

//        binding.btnPreviousTask.setOnClickListener {
//            viewModel.goToPreviousTask()
//        }
//
//        binding.btnNextTask.setOnClickListener {
//            viewModel.goToNextTask()
//        }

        binding.btnSwitchTask.setOnClickListener {
            // 切换任务功能：可以弹出任务选择对话框或直接切换到下一个
            viewModel.goToNextTask()
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
        
        val dialog = SettingsDialog(
            currentSpeedStep = execution.speedStep,
            currentContinuousCycles = execution.continuousCycles,
            currentJogInterval = execution.jogInterval
        ) { speedStep, continuousCycles, jogInterval ->
            viewModel.updateSettings(speedStep, continuousCycles, jogInterval)
        }
        dialog.show(supportFragmentManager, "SettingsDialog")
    }
    
    private fun showAddRecordDialog() {
        val task = viewModel.task.value ?: return
        val configItem = viewModel.currentConfigItem.value ?: return
        
        // 获取位置数据
        val position = configItem.position.toIntOrNull() ?: 1
        
        // 生成随机叶片数（1到配置项的bladeCount之间）
        val bladeCount = configItem.bladeCount
        val bladeNumber = if (bladeCount > 0) {
            (1..bladeCount).random()
        } else {
            1
        }
        
        // 创建记录
        viewModel.addRecord(position, bladeNumber)
    }
}

// ViewModel Factory
class TaskControlViewModelFactory(
    private val taskRepository: TaskRepository,
    private val engineRepository: EngineRepository
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskControlViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskControlViewModel(taskRepository, engineRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
