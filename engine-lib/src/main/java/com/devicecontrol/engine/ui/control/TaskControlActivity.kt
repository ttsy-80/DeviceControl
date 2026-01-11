package com.devicecontrol.engine.ui.control

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.devicecontrol.engine.R
import com.devicecontrol.engine.data.database.AppDatabase
import com.devicecontrol.engine.data.model.TaskStatus
import com.devicecontrol.engine.data.repository.EngineRepository
import com.devicecontrol.engine.data.repository.TaskRepository
import com.devicecontrol.engine.databinding.ActivityTaskControlBinding
import com.devicecontrol.engine.viewmodel.TaskControlViewModel
import java.text.DecimalFormat

class TaskControlActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskControlBinding

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
                updateTorqueSpeedDisplay(it.torque, it.speed)
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
                    binding.btnPreviousTask.visibility = android.view.View.VISIBLE
                    binding.btnNextTask.visibility = android.view.View.VISIBLE
                } else {
                    binding.tvTaskIndex.visibility = android.view.View.GONE
                    // 单个任务时隐藏切换按钮
                    binding.btnPreviousTask.visibility = android.view.View.GONE
                    binding.btnNextTask.visibility = android.view.View.GONE
                }
            }
        }

        viewModel.canGoPrevious.observe(this) { canGo ->
            // 只有在按钮可见时才更新状态
            if (binding.btnPreviousTask.visibility == android.view.View.VISIBLE) {
                binding.btnPreviousTask.isEnabled = canGo
            }
        }

        viewModel.canGoNext.observe(this) { canGo ->
            // 只有在按钮可见时才更新状态
            if (binding.btnNextTask.visibility == android.view.View.VISIBLE) {
                binding.btnNextTask.isEnabled = canGo
            }
        }
    }

    private fun updateTorqueSpeedDisplay(torque: Double, speed: Double) {
        val display = "力矩: ${decimalFormat.format(torque)} | 速度: ${decimalFormat.format(speed)}"
        binding.tvTorqueSpeed.text = display
    }

    private fun updateButtonStates(execution: com.devicecontrol.engine.data.model.TaskExecution) {
        // 更新启动/暂停按钮
        when (execution.status) {
            com.devicecontrol.engine.data.model.TaskStatus.RUNNING -> {
                binding.btnStartPause.text = getString(R.string.pause)
            }
            com.devicecontrol.engine.data.model.TaskStatus.PAUSED -> {
                binding.btnStartPause.text = getString(R.string.start)
            }
            com.devicecontrol.engine.data.model.TaskStatus.STOPPED -> {
                binding.btnStartPause.text = getString(R.string.start)
            }
        }
        
        // 更新正转/反转按钮 - 显示当前状态
        binding.btnForwardReverse.text = when (execution.rotationDirection) {
            com.devicecontrol.engine.data.model.RotationDirection.FORWARD -> getString(R.string.forward)
            com.devicecontrol.engine.data.model.RotationDirection.REVERSE -> getString(R.string.reverse)
        }
        
        // 更新点动/连续按钮 - 显示当前状态
        binding.btnJogContinuous.text = when (execution.operationMode) {
            com.devicecontrol.engine.data.model.OperationMode.JOG -> getString(R.string.jog)
            com.devicecontrol.engine.data.model.OperationMode.CONTINUOUS -> getString(R.string.continuous)
        }
    }

    private fun setupListeners() {
        binding.btnStartPause.setOnClickListener {
            viewModel.toggleStartPause()
        }

        binding.btnStop.setOnClickListener {
            viewModel.stop()
        }

        binding.btnForwardReverse.setOnClickListener {
            viewModel.toggleRotationDirection()
        }

        binding.btnJogContinuous.setOnClickListener {
            viewModel.toggleOperationMode()
        }

        binding.btnTorquePlus.setOnClickListener {
            viewModel.increaseTorque()
        }

        binding.btnTorqueMinus.setOnClickListener {
            viewModel.decreaseTorque()
        }

        binding.btnSpeedPlus.setOnClickListener {
            viewModel.increaseSpeed()
        }

        binding.btnSpeedMinus.setOnClickListener {
            viewModel.decreaseSpeed()
        }

        binding.btnPreviousTask.setOnClickListener {
            viewModel.goToPreviousTask()
        }

        binding.btnNextTask.setOnClickListener {
            viewModel.goToNextTask()
        }

        binding.btnSettings.setOnClickListener {
            // TODO: 实现设置功能
            Toast.makeText(this, "设置功能待实现", Toast.LENGTH_SHORT).show()
        }
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
