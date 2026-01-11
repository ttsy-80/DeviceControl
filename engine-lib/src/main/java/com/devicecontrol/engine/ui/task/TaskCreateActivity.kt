package com.devicecontrol.engine.ui.task

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.devicecontrol.engine.R
import com.devicecontrol.engine.data.database.AppDatabase
import com.devicecontrol.engine.data.repository.EngineRepository
import com.devicecontrol.engine.data.repository.TaskRepository
import com.devicecontrol.engine.databinding.ActivityTaskCreateBinding
import com.devicecontrol.engine.ui.control.TaskControlActivity
import com.devicecontrol.engine.viewmodel.TaskCreateViewModel
import kotlinx.coroutines.launch

class TaskCreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskCreateBinding
    private lateinit var gearRatioAdapter: GearRatioAdapter

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val engineRepository by lazy { EngineRepository(database.engineDao()) }
    private val taskRepository by lazy { TaskRepository(database.taskDao()) }
    private val viewModel: TaskCreateViewModel by viewModels {
        TaskCreateViewModelFactory(engineRepository, taskRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        gearRatioAdapter = GearRatioAdapter(emptyList()) { _ ->
            // 选择变化时的回调
        }
        binding.rvGearRatios.layoutManager = LinearLayoutManager(this)
        binding.rvGearRatios.adapter = gearRatioAdapter
    }

    private fun setupObservers() {
        viewModel.modelsWithConfigItems.observe(this) { models ->
            if (models.isNotEmpty()) {
                val modelNames = models.map { it.model.name }
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, modelNames)
                binding.actvModel.setAdapter(adapter)
                binding.actvModel.setText("", false) // 清空文本，显示hint
            } else {
                // 如果没有型号数据，清空适配器
                binding.actvModel.setAdapter(null)
                binding.actvModel.setText("", false)
            }
        }

        viewModel.availableGearRatios.observe(this) { configItems ->
            if (configItems.isNotEmpty()) {
                gearRatioAdapter = GearRatioAdapter(configItems) { _ ->
                    // 选择变化时的回调
                }
                binding.rvGearRatios.adapter = gearRatioAdapter
                gearRatioAdapter.notifyDataSetChanged()
            } else {
                // 清空列表
                gearRatioAdapter = GearRatioAdapter(emptyList()) { }
                binding.rvGearRatios.adapter = gearRatioAdapter
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupListeners() {
        binding.actvModel.setOnItemClickListener { _, _, position, _ ->
            val models = viewModel.modelsWithConfigItems.value
            if (models != null && position < models.size) {
                val selectedModel = models[position]
                viewModel.selectModel(selectedModel)
            }
        }

        binding.btnCreateTask.setOnClickListener {
            val selectedConfigItemIds = gearRatioAdapter.getSelectedConfigItemIds()
            viewModel.createTask(
                selectedConfigItemIds = selectedConfigItemIds,
                onSuccess = { taskId ->
                    Toast.makeText(this, "任务创建成功", Toast.LENGTH_SHORT).show()
                    // 跳转到任务控制页面
                    val intent = Intent(this, TaskControlActivity::class.java)
                    intent.putExtra("taskId", taskId)
                    startActivity(intent)
                    finish()
                },
                onError = { error ->
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

// ViewModel Factory
class TaskCreateViewModelFactory(
    private val engineRepository: EngineRepository,
    private val taskRepository: TaskRepository
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskCreateViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskCreateViewModel(engineRepository, taskRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
