package com.devicecontrol.engine.ui.task

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
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
        gearRatioAdapter = GearRatioAdapter(emptyList()) { selectedRatios ->
            // 选择变化时的回调
        }
        binding.rvGearRatios.adapter = gearRatioAdapter
    }

    private fun setupObservers() {
        viewModel.modelsWithConfigItems.observe(this) { models ->
            val modelNames = models.map { it.model.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, modelNames)
            binding.actvModel.setAdapter(adapter)
        }

        viewModel.availableGearRatios.observe(this) { configItems ->
            gearRatioAdapter = GearRatioAdapter(configItems) { selectedRatios ->
                // 选择变化时的回调
            }
            binding.rvGearRatios.adapter = gearRatioAdapter
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
                viewModel.selectModel(models[position])
            }
        }

        binding.btnCreateTask.setOnClickListener {
            val selectedRatios = gearRatioAdapter.getSelectedGearRatios()
            viewModel.createTask(
                selectedGearRatios = selectedRatios,
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
