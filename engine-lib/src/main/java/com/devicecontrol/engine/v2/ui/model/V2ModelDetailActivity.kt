package com.devicecontrol.engine.v2.ui.model

import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.ui.adapter.V2ModelDetailRowAdapter
import com.devicecontrol.engine.v2.ui.base.V2BaseShellActivity
import com.devicecontrol.engine.v2.viewmodel.V2EngineViewModelFactory
import com.devicecontrol.engine.v2.viewmodel.V2ModelDetailPageMode
import com.devicecontrol.engine.v2.viewmodel.V2ModelDetailViewModel

/** 型号详情/编辑（P15～P17） */
class V2ModelDetailActivity : V2BaseShellActivity() {

    override val logTag: String = "ModelDetail"

    private val viewModel: V2ModelDetailViewModel by viewModels {
        V2EngineViewModelFactory(application)
    }
    private lateinit var engineFieldsContainer: LinearLayout
    private lateinit var panelRoot: View
    private lateinit var rowAdapter: V2ModelDetailRowAdapter
    private lateinit var rvDetailRows: RecyclerView

    private val addConfigLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.reload()
        }
    }

    override fun contentLayoutId(): Int = R.layout.content_v2_model_detail

    override fun shellTitleCn(): String =
        intent.getStringExtra(EXTRA_MODEL_NAME).orEmpty().ifEmpty { getString(R.string.v2_model_manage) }

    override fun shellTitleEn(): String = getString(R.string.v2_engine_model_subtitle)

    override fun shellTitleIcon(): Int = R.drawable.ic_v2_model_manage

    override fun showBackHome(): Boolean = true

    override fun showShellBottomBar(): Boolean = false

    override fun onContentCreated(contentRoot: View) {
        val name = intent.getStringExtra(EXTRA_MODEL_NAME).orEmpty()
        viewModel.init(name)

        panelRoot = contentRoot.findViewById(R.id.includeEnginePanel)
        engineFieldsContainer = panelRoot.findViewById(R.id.llEngineFields)
        V2ModelEnginePanelBinder.configureImageArea(panelRoot, showImport = false)

        rowAdapter = V2ModelDetailRowAdapter(
            onAddConfig = { launchAddConfig() },
            onDuplicateRow = { viewModel.duplicateRow(it.configItemId) },
            onRowDelete = { viewModel.deleteRow(it.configItemId) },
        )
        rvDetailRows = contentRoot.findViewById(R.id.rvDetailRows)
        rvDetailRows.layoutManager = LinearLayoutManager(this)
        rvDetailRows.adapter = rowAdapter

        val btnEdit = contentRoot.findViewById<View>(R.id.btnDetailEdit)
        val btnSave = contentRoot.findViewById<View>(R.id.btnDetailSave)

        viewModel.modelName.observe(this) { model ->
            shellBinder.bindTitles(model, getString(R.string.v2_engine_model_subtitle))
            V2ModelEnginePanelBinder.showModelNameStrip(panelRoot, model)
        }
        viewModel.imagePath.observe(this) { path ->
            V2ModelEnginePanelBinder.bindEngineImage(panelRoot, path)
        }
        viewModel.engineFields.observe(this) { fields ->
            if (engineFieldsContainer.childCount == 0) {
                V2ModelEnginePanelBinder.bindFields(
                    engineFieldsContainer,
                    fields,
                    showEditIcon = false,
                    onValueChanged = { _, _ -> },
                )
            }
        }
        viewModel.rows.observe(this) { rowAdapter.submitList(it) }
        viewModel.pageMode.observe(this) { mode ->
            rowAdapter.pageMode = mode
            val editing = mode == V2ModelDetailPageMode.TABLE_EDIT
            btnEdit.visibility = if (editing) View.GONE else View.VISIBLE
            btnSave.visibility = if (editing) View.VISIBLE else View.GONE
        }
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
        viewModel.modelRemoved.observe(this) { removed ->
            if (removed) {
                viewModel.consumeModelRemoved()
                setResult(RESULT_OK)
                finish()
            }
        }

        btnEdit.setOnClickListener { viewModel.enterTableEdit() }
        btnSave.setOnClickListener {
            val rows = rowAdapter.readTableRowValues(rvDetailRows)
            viewModel.exitTableEdit(save = true, tableRows = rows)
            Toast.makeText(this, R.string.v2_save, Toast.LENGTH_SHORT).show()
        }
        contentRoot.findViewById<View>(R.id.btnDetailBack).setOnClickListener {
            if (viewModel.pageMode.value == V2ModelDetailPageMode.TABLE_EDIT) {
                viewModel.exitTableEdit(save = false, tableRows = null)
            } else {
                finish()
            }
        }
    }

    private fun launchAddConfig() {
        val args = viewModel.getAppendArgs()
        if (args == null) {
            Toast.makeText(this, R.string.v2_model_load_failed, Toast.LENGTH_SHORT).show()
            return
        }
        addConfigLauncher.launch(V2ModelAddActivity.createAppendIntent(this, args))
    }

    companion object {
        const val EXTRA_MODEL_NAME = "extra_v2_detail_model"
    }
}
