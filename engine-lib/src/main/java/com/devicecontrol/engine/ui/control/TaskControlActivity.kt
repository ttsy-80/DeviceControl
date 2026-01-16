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
            binding.tvTaskInfo.text = info
        }

        viewModel.taskIndex.observe(this) { index ->
            binding.tvTaskIndex.text = index
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
                android.util.Log.d("TaskControl", "Task loaded: configItemIds.size = ${it.configItemIds.size}, hasMultipleTasks = $hasMultipleTasks")
                if (hasMultipleTasks) {
                    binding.tvTaskIndex.visibility = android.view.View.VISIBLE
                    // 多任务时：在TaskInfo区域显示导航按钮
                    binding.llTaskNavigation.visibility = android.view.View.VISIBLE
                } else {
                    binding.tvTaskIndex.visibility = android.view.View.GONE
                    // 单任务时：隐藏任务导航容器
                    binding.llTaskNavigation.visibility = android.view.View.GONE
                }
            }
        }

        viewModel.canGoPrevious.observe(this) { canGo ->
            // 更新按钮状态和可见性
            binding.btnPreviousTask.isEnabled = canGo
            binding.btnPreviousTask.alpha = if (canGo) 1.0f else 0.5f
            // 即使不可用也显示按钮，只是置灰
            binding.btnPreviousTask.visibility = android.view.View.VISIBLE
        }

        viewModel.canGoNext.observe(this) { canGo ->
            // 更新按钮状态和可见性
            binding.btnNextTask.isEnabled = canGo
            binding.btnNextTask.alpha = if (canGo) 1.0f else 0.5f
            // 即使不可用也显示按钮，只是置灰
            binding.btnNextTask.visibility = android.view.View.VISIBLE
        }
        
        viewModel.taskRecords.observe(this) { records ->
            recordAdapter.submitList(records)
        }
    }

    private fun updateSpeedDisplay(speed: Double) {
        val display = "速度: ${decimalFormat.format(speed)} ${getString(R.string.speed_unit)}"
        binding.tvSpeed.text = display
    }

    private fun updateButtonStates(execution: com.devicecontrol.engine.data.model.TaskExecution) {
        // 更新启动/暂停按钮状态
        val isRunning = execution.status == com.devicecontrol.engine.data.model.TaskStatus.RUNNING
        // 启动状态时，启动按钮置灰，暂停按钮可用
        binding.btnStartPause.isEnabled = !isRunning
        binding.btnStartPause.alpha = if (isRunning) 0.5f else 1.0f
        binding.btnPause.isEnabled = isRunning
        binding.btnPause.alpha = if (isRunning) 1.0f else 0.5f
        
        // 更新正转/反转按钮状态 - 选中的按钮置灰
        val isForward = execution.rotationDirection == com.devicecontrol.engine.data.model.RotationDirection.FORWARD
        binding.btnForward.isEnabled = !isForward
        binding.btnForward.alpha = if (isForward) 0.5f else 1.0f
        binding.btnReverse.isEnabled = isForward
        binding.btnReverse.alpha = if (isForward) 1.0f else 0.5f
        
        // 更新点动/连续按钮状态 - 选中的按钮置灰
        val isJog = execution.operationMode == com.devicecontrol.engine.data.model.OperationMode.JOG
        binding.btnJog.isEnabled = !isJog
        binding.btnJog.alpha = if (isJog) 0.5f else 1.0f
        binding.btnContinuous.isEnabled = isJog
        binding.btnContinuous.alpha = if (isJog) 1.0f else 0.5f
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

        binding.btnPreviousTask.setOnClickListener {
            viewModel.goToPreviousTask()
        }

        binding.btnNextTask.setOnClickListener {
            viewModel.goToNextTask()
        }

        binding.btnAddRecord.setOnClickListener {
            showAddRecordDialog()
        }
    }
    
    private fun showAddRecordDialog() {
        val task = viewModel.task.value ?: return
        val configItem = viewModel.currentConfigItem.value ?: return
        
        // 这里应该发送指令给电机，然后获取回传的叶片数
        // 暂时使用模拟数据
        val position = configItem.position.toIntOrNull() ?: 1
        val bladeNumber = 1 // TODO: 从电机获取实际数据 - 电机回传的数据
        
        // TODO: 实际应该先发送指令给电机，等待回传数据后再创建记录
        // 这里暂时直接创建记录
        viewModel.addRecord(position, bladeNumber)
        Toast.makeText(this, "记录已添加", Toast.LENGTH_SHORT).show()
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
