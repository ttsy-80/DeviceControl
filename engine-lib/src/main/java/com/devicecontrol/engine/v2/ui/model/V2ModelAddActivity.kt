package com.devicecontrol.engine.v2.ui.model

import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.ui.adapter.V2ModelAddDetailRowAdapter
import com.devicecontrol.engine.v2.ui.base.V2BaseShellActivity
import com.devicecontrol.engine.v2.viewmodel.V2ModelAddViewModel

/** 型号添加（P14） */
class V2ModelAddActivity : V2BaseShellActivity() {

    override val logTag: String = "ModelAdd"

    private val viewModel: V2ModelAddViewModel by viewModels()
    private lateinit var engineFieldsContainer: LinearLayout
    private lateinit var detailAdapter: V2ModelAddDetailRowAdapter

    override fun contentLayoutId(): Int = R.layout.content_v2_model_add

    override fun shellTitleCn(): String =
        viewModel.modelName.value?.takeIf { it.isNotBlank() }
            ?: getString(R.string.v2_new_model_title)

    override fun shellTitleEn(): String = getString(R.string.v2_engine_model_subtitle)

    override fun shellTitleIcon(): Int = R.drawable.ic_v2_model_manage

    override fun showBackHome(): Boolean = true

    override fun showShellBottomBar(): Boolean = false

    override fun onContentCreated(contentRoot: View) {
        viewModel.load()

        val panel = contentRoot.findViewById<View>(R.id.includeEnginePanel)
        engineFieldsContainer = panel.findViewById(R.id.llEngineFields)
        V2ModelEnginePanelBinder.configureImageArea(panel, showImport = true)
        panel.findViewById<View>(R.id.btnImportImage).setOnClickListener {
            Toast.makeText(this, R.string.v2_import_image, Toast.LENGTH_SHORT).show()
        }

        detailAdapter = V2ModelAddDetailRowAdapter { key, value ->
            viewModel.updateDetailRow(key, value)
        }
        contentRoot.findViewById<RecyclerView>(R.id.rvAddDetailRows).apply {
            layoutManager = LinearLayoutManager(this@V2ModelAddActivity)
            adapter = detailAdapter
        }

        viewModel.engineFields.observe(this) { fields ->
            V2ModelEnginePanelBinder.bindFields(
                engineFieldsContainer,
                fields,
                showEditIcon = true,
                onValueChanged = viewModel::updateEngineField,
            )
        }
        viewModel.detailRows.observe(this) { detailAdapter.submitList(it) }
        viewModel.modelName.observe(this) { name ->
            shellBinder.bindTitles(
                name.takeIf { it.isNotBlank() } ?: getString(R.string.v2_new_model_title),
                getString(R.string.v2_engine_model_subtitle),
            )
        }

        contentRoot.findViewById<View>(R.id.btnAddConfirm).setOnClickListener {
            viewModel.confirm(
                onSuccess = { finish() },
                onEmptyName = {
                    Toast.makeText(this, R.string.v2_model_name_required, Toast.LENGTH_SHORT).show()
                },
            )
        }
        contentRoot.findViewById<View>(R.id.btnAddBack).setOnClickListener { finish() }
    }
}
