package com.devicecontrol.engine.ui.task

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.devicecontrol.engine.R
import com.devicecontrol.engine.data.database.AppDatabase
import com.devicecontrol.engine.data.repository.TaskRepository
import com.devicecontrol.engine.databinding.ActivityTaskListBinding
import com.devicecontrol.engine.ui.control.TaskControlActivity
import com.devicecontrol.engine.viewmodel.TaskListViewModel

class TaskListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskListBinding
    private lateinit var taskAdapter: TaskListAdapter

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val taskRepository by lazy { TaskRepository(database.taskDao()) }
    private val viewModel: TaskListViewModel by viewModels {
        TaskListViewModelFactory(taskRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Setup RecyclerView
        taskAdapter = TaskListAdapter { task ->
            // 跳转到任务控制页面
            val intent = Intent(this, TaskControlActivity::class.java)
            intent.putExtra("taskId", task.id)
            startActivity(intent)
        }
        binding.rvTaskList.layoutManager = LinearLayoutManager(this)
        binding.rvTaskList.adapter = taskAdapter

        setupObservers()
    }

    private fun setupObservers() {
        viewModel.tasks.observe(this) { tasks ->
            if (tasks.isEmpty()) {
                binding.tvEmptyState.visibility = android.view.View.VISIBLE
                binding.rvTaskList.visibility = android.view.View.GONE
            } else {
                binding.tvEmptyState.visibility = android.view.View.GONE
                binding.rvTaskList.visibility = android.view.View.VISIBLE
                taskAdapter.submitList(tasks)
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// ViewModel Factory
class TaskListViewModelFactory(
    private val taskRepository: TaskRepository
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskListViewModel(taskRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
