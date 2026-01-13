package com.devicecontrol.engine.ui.model

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicecontrol.engine.R
import com.devicecontrol.engine.data.database.AppDatabase
import com.devicecontrol.engine.data.model.ConfigItem
import com.devicecontrol.engine.data.model.EngineModel
import com.devicecontrol.engine.data.repository.EngineRepository
import com.devicecontrol.engine.databinding.ActivityModelManagementBinding
import com.devicecontrol.engine.viewmodel.ModelManagementViewModel
import kotlinx.coroutines.launch

class ModelManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModelManagementBinding
    private lateinit var adapter: ModelListAdapter
    private lateinit var layoutManager: GridLayoutManager

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { EngineRepository(database.engineDao()) }
    private val viewModel: ModelManagementViewModel by viewModels {
        ModelManagementViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModelManagementBinding.inflate(layoutInflater)
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
        layoutManager = GridLayoutManager(this, 7) // 7列：型号(2) + 变速比(1) + 位置(1) + 叶片数(1) + 点动次数(1) + 操作(1)
        adapter = ModelListAdapter(
            onAddConfigItem = { modelId ->
                showConfigItemDialog(modelId)
            },
            onEditConfigItem = { configItem ->
                showEditConfigItemDialog(configItem)
            },
            onDeleteConfigItem = { configItem ->
                showDeleteConfigItemDialog(configItem)
            },
            onDeleteModel = { modelId ->
                showDeleteModelDialog(modelId)
            }
        )

        binding.rvModelList.layoutManager = layoutManager
        binding.rvModelList.adapter = adapter

        // 设置合并单元格
        // 7列布局：型号(2列) + 变速比(1列) + 位置(1列) + 叶片数(1列) + 点动次数(1列) + 操作(1列)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                // 找到当前position对应的model和configItem
                var currentPosition = 0
                viewModel.modelsWithConfigItems.value?.forEach { model ->
                    if (position < currentPosition + model.configItems.size) {
                        val itemIndex = position - currentPosition
                        // 每个item占据7列
                        return 7
                    }
                    currentPosition += model.configItems.size
                }
                return 7
            }
        }
    }

    private fun setupObservers() {
        viewModel.modelsWithConfigItems.observe(this) { models ->
            adapter.submitList(models)
            updateCurrentModelDisplay(models.firstOrNull())
        }

        viewModel.selectedModel.observe(this) { model ->
            updateCurrentModelDisplay(model)
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun updateCurrentModelDisplay(model: com.devicecontrol.engine.data.model.EngineModelWithConfigItems?) {
        // 已移除当前型号显示卡片，此方法保留用于未来扩展
    }

    private fun setupListeners() {
        binding.btnNewModel.setOnClickListener {
            showNewModelDialog()
        }
    }

    private fun showNewModelDialog() {
        val dialog = NewModelDialog { modelName ->
            // 创建型号后立即弹出配置项添加弹框
            showConfigItemDialogForNewModel(modelName)
        }
        dialog.show(supportFragmentManager, "NewModelDialog")
    }

    private fun showConfigItemDialogForNewModel(modelName: String) {
        val dialog = ConfigItemDialog(modelId = null, existingConfigItem = null) { gearRatio, position, bladeCount, jogCount ->
            viewModel.createModelWithConfigItem(
                modelName = modelName,
                gearRatio = gearRatio,
                position = position,
                bladeCount = bladeCount,
                jogCount = jogCount,
                onSuccess = {
                    Toast.makeText(this, "型号创建成功", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            )
        }
        dialog.show(supportFragmentManager, "ConfigItemDialog")
    }

    private fun showConfigItemDialog(modelId: Long) {
        // 获取该型号的第一个配置项的变速比
        lifecycleScope.launch {
            val configItems = repository.getConfigItemsByModelId(modelId)
            val firstGearRatio = configItems.firstOrNull()?.gearRatio
            
            val dialog = ConfigItemDialog(
                modelId = modelId,
                existingConfigItem = null,
                firstGearRatio = firstGearRatio
            ) { gearRatio, position, bladeCount, jogCount ->
                viewModel.addConfigItem(
                    modelId = modelId,
                    gearRatio = gearRatio,
                    position = position,
                    bladeCount = bladeCount,
                    jogCount = jogCount,
                    onSuccess = {
                        Toast.makeText(this@ModelManagementActivity, "配置项添加成功", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(this@ModelManagementActivity, error, Toast.LENGTH_SHORT).show()
                    }
                )
            }
            dialog.show(supportFragmentManager, "ConfigItemDialog")
        }
    }
    
    private fun showEditConfigItemDialog(configItem: ConfigItem) {
        val dialog = ConfigItemDialog(
            modelId = configItem.modelId,
            existingConfigItem = configItem
        ) { gearRatio, position, bladeCount, jogCount ->
            viewModel.updateConfigItem(
                configItem = configItem,
                gearRatio = gearRatio,
                position = position,
                bladeCount = bladeCount,
                jogCount = jogCount,
                onSuccess = {
                    Toast.makeText(this, "配置项更新成功", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            )
        }
        dialog.show(supportFragmentManager, "EditConfigItemDialog")
    }

    private fun showDeleteConfigItemDialog(configItem: ConfigItem) {
        AlertDialog.Builder(this)
            .setTitle("确认删除")
            .setMessage("确定要删除这个配置项吗？")
            .setPositiveButton("删除") { _, _ ->
                viewModel.deleteConfigItem(configItem) {
                    Toast.makeText(this, "型号已自动删除（无配置项）", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showDeleteModelDialog(modelId: Long) {
        AlertDialog.Builder(this)
            .setTitle("确认删除")
            .setMessage("确定要删除这个型号及其所有配置项吗？")
            .setPositiveButton("删除") { _, _ ->
                lifecycleScope.launch {
                    val model = database.engineDao().getModelById(modelId)
                    model?.let {
                        viewModel.deleteModel(it)
                        Toast.makeText(this@ModelManagementActivity, "删除成功", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
}

// ViewModel Factory
class ModelManagementViewModelFactory(
    private val repository: EngineRepository
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ModelManagementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ModelManagementViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
