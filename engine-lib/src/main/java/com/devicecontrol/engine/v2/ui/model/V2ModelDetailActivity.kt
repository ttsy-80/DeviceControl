package com.devicecontrol.engine.v2.ui.model

import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.ui.adapter.V2ModelDetailRowAdapter
import com.devicecontrol.engine.v2.ui.base.V2BaseShellActivity
import com.devicecontrol.engine.v2.viewmodel.V2ModelDetailPageMode
import com.devicecontrol.engine.v2.viewmodel.V2ModelDetailViewModel

/** 型号详情/编辑（P15～P17） */
class V2ModelDetailActivity : V2BaseShellActivity() {

    override val logTag: String = "ModelDetail"

    private val viewModel: V2ModelDetailViewModel by viewModels()
    private lateinit var engineFieldsContainer: LinearLayout
    private lateinit var rowAdapter: V2ModelDetailRowAdapter

    override fun contentLayoutId(): Int = R.layout.content_v2_model_detail

    override fun shellTitleCn(): String =
        intent.getStringExtra(EXTRA_MODEL_NAME) ?: "CFM56-3B"

    override fun shellTitleEn(): String = getString(R.string.v2_engine_model_subtitle)

    override fun shellTitleIcon(): Int = R.drawable.ic_v2_model_manage

    override fun showBackHome(): Boolean = true

    override fun showShellBottomBar(): Boolean = false

    override fun onContentCreated(contentRoot: View) {
        val name = intent.getStringExtra(EXTRA_MODEL_NAME) ?: "CFM56-3B"
        viewModel.init(name)

        val panel = contentRoot.findViewById<View>(R.id.includeEnginePanel)
        engineFieldsContainer = panel.findViewById(R.id.llEngineFields)
        V2ModelEnginePanelBinder.configureImageArea(panel, showImport = false)

        rowAdapter = V2ModelDetailRowAdapter(
            onStartRowEdit = { viewModel.startRowEdit(it.id) },
            onDuplicateRow = { viewModel.duplicateRow(it.id) },
            onRowSave = { row, position, blades ->
                viewModel.saveRowEdit(row.id, position, blades)
            },
            onRowDelete = { viewModel.deleteRow(it.id) },
        )
        contentRoot.findViewById<RecyclerView>(R.id.rvDetailRows).apply {
            layoutManager = LinearLayoutManager(this@V2ModelDetailActivity)
            adapter = rowAdapter
        }

        val btnEdit = contentRoot.findViewById<View>(R.id.btnDetailEdit)
        val btnSave = contentRoot.findViewById<View>(R.id.btnDetailSave)

        viewModel.modelName.observe(this) { model ->
            shellBinder.bindTitles(model, getString(R.string.v2_engine_model_subtitle))
        }
        viewModel.engineFields.observe(this) { fields ->
            V2ModelEnginePanelBinder.bindFields(
                engineFieldsContainer,
                fields,
                showEditIcon = false,
                onValueChanged = { _, _ -> },
            )
        }
        viewModel.rows.observe(this) { rowAdapter.submitList(it) }
        viewModel.pageMode.observe(this) { mode ->
            rowAdapter.pageMode = mode
            val editing = mode == V2ModelDetailPageMode.TABLE_EDIT
            btnEdit.visibility = if (editing) View.GONE else View.VISIBLE
            btnSave.visibility = if (editing) View.VISIBLE else View.GONE
        }

        btnEdit.setOnClickListener { viewModel.enterTableEdit() }
        btnSave.setOnClickListener {
            viewModel.exitTableEdit(save = true)
            Toast.makeText(this, R.string.v2_save, Toast.LENGTH_SHORT).show()
        }
        contentRoot.findViewById<View>(R.id.btnDetailBack).setOnClickListener {
            if (viewModel.pageMode.value == V2ModelDetailPageMode.TABLE_EDIT) {
                viewModel.exitTableEdit(save = false)
            } else {
                finish()
            }
        }
    }

    companion object {
        const val EXTRA_MODEL_NAME = "extra_v2_detail_model"
    }
}
