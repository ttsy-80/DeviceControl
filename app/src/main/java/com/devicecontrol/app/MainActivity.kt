package com.devicecontrol.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.devicecontrol.app.databinding.ActivityMainBinding
import com.devicecontrol.engine.log.EngineLog
import com.devicecontrol.engine.ui.control.TaskControlActivity
import com.devicecontrol.engine.ui.model.ModelManagementActivity
import com.devicecontrol.engine.ui.task.TaskCreateActivity
import com.devicecontrol.engine.ui.task.TaskListActivity

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.cardModelManagement.setOnClickListener {
            startActivity(Intent(this, ModelManagementActivity::class.java))
        }
        
        binding.btnModelManagement.setOnClickListener {
            startActivity(Intent(this, ModelManagementActivity::class.java))
        }
        
        binding.cardTaskCreate.setOnClickListener {
            startActivity(Intent(this, TaskCreateActivity::class.java))
        }
        
        binding.btnTaskCreate.setOnClickListener {
            startActivity(Intent(this, TaskCreateActivity::class.java))
        }
        
        binding.cardTaskList.setOnClickListener {
            startActivity(Intent(this, TaskListActivity::class.java))
        }
        
        binding.btnTaskList.setOnClickListener {
            startActivity(Intent(this, TaskListActivity::class.java))
        }
        
        binding.cardTaskControl.setOnClickListener {
            startActivity(Intent(this, TaskControlActivity::class.java))
        }
        
        binding.btnTaskControl.setOnClickListener {
            startActivity(Intent(this, TaskControlActivity::class.java))
        }

        EngineLog.setFileLogger(this.application)
    }
}
