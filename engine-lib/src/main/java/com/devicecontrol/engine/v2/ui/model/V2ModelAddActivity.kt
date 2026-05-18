package com.devicecontrol.engine.v2.ui.model

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.model.V2ModelAppendArgs
import com.devicecontrol.engine.v2.ui.base.V2BaseShellActivity
import com.devicecontrol.engine.v2.ui.widget.applyV2LandscapeIme
import com.devicecontrol.engine.v2.viewmodel.V2EngineViewModelFactory
import com.devicecontrol.engine.v2.viewmodel.V2ModelAddViewModel

/** 型号添加（P14），写入 Room（对齐 v1 新建型号 + 首条配置项） */
class V2ModelAddActivity : V2BaseShellActivity() {

    override val logTag: String = "ModelAdd"

    private val viewModel: V2ModelAddViewModel by viewModels {
        V2EngineViewModelFactory(application)
    }
    private lateinit var engineFieldsContainer: LinearLayout
    private lateinit var panelRoot: View
    private lateinit var etDetailPosition: EditText
    private lateinit var etDetailBlade: EditText
    private var appendArgs: V2ModelAppendArgs? = null

    override fun contentLayoutId(): Int = R.layout.content_v2_model_add

    override fun shellTitleCn(): String =
        appendArgs?.modelName ?: getString(R.string.v2_new_model_title)

    override fun shellTitleEn(): String = getString(R.string.v2_engine_model_subtitle)

    override fun shellTitleIcon(): Int = R.drawable.ic_v2_model_manage

    override fun showBackHome(): Boolean = true

    override fun showShellBottomBar(): Boolean = false

    override fun onContentCreated(contentRoot: View) {
        appendArgs = readAppendArgs(intent)
        if (appendArgs != null) {
            viewModel.initAppendConfig(appendArgs!!)
        } else {
            viewModel.initNewModel()
        }

        panelRoot = contentRoot.findViewById(R.id.includeEnginePanel)
        engineFieldsContainer = panelRoot.findViewById(R.id.llEngineFields)

        if (appendArgs != null) {
            val args = appendArgs!!
            V2ModelEnginePanelBinder.setupAppendConfigEngineFields(
                panelRoot = panelRoot,
                container = engineFieldsContainer,
                modelName = args.modelName,
                safeTorque = args.safeTorque,
                gearRatio = args.gearRatio.toString(),
                imagePath = args.imagePath,
                onModelNameChanged = viewModel::updateModelNameTitle,
            )
        } else {
            V2ModelEnginePanelBinder.configureImageArea(panelRoot, showImport = true)
            panelRoot.findViewById<View>(R.id.btnImportImage).setOnClickListener {
                Toast.makeText(this, R.string.v2_import_image, Toast.LENGTH_SHORT).show()
            }
            V2ModelEnginePanelBinder.setupAddEngineFields(engineFieldsContainer) { name ->
                viewModel.updateModelNameTitle(name)
            }
        }

        val detailForm = contentRoot.findViewById<View>(R.id.includeAddDetailForm)
        etDetailPosition = detailForm.findViewById(R.id.etAddDetailPosition)
        etDetailBlade = detailForm.findViewById(R.id.etAddDetailBlade)
        etDetailPosition.applyV2LandscapeIme()
        etDetailBlade.applyV2LandscapeIme()

        viewModel.modelName.observe(this) { name ->
            val title = name.takeIf { it.isNotBlank() }
                ?: getString(R.string.v2_new_model_title)
            shellBinder.bindTitles(title, getString(R.string.v2_engine_model_subtitle))
        }
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
        viewModel.saveSuccess.observe(this) { success ->
            if (success) {
                val msg = if (appendArgs != null) {
                    R.string.v2_config_add_success
                } else {
                    R.string.v2_model_create_success
                }
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                viewModel.consumeSaveSuccess()
                setResult(RESULT_OK)
                finish()
            }
        }

        contentRoot.findViewById<View>(R.id.btnAddConfirm).setOnClickListener { commitFormAndConfirm() }
        contentRoot.findViewById<View>(R.id.btnAddBack).setOnClickListener { finish() }
    }

    private fun commitFormAndConfirm() {
        currentFocus?.clearFocus()
        val engineValues = V2ModelEnginePanelBinder.readFieldValues(engineFieldsContainer)
        val locked = appendArgs
        viewModel.confirm(
            modelName = locked?.modelName ?: engineValues["model_name"].orEmpty(),
            safeTorque = locked?.safeTorque ?: engineValues["safe_torque"].orEmpty(),
            gearRatioText = locked?.gearRatio?.toString() ?: engineValues["gear_ratio"].orEmpty(),
            position = etDetailPosition.text?.toString().orEmpty(),
            bladeText = etDetailBlade.text?.toString().orEmpty(),
        )
    }

    companion object {
        const val EXTRA_APPEND_MODEL_ID = "extra_v2_append_model_id"
        const val EXTRA_APPEND_MODEL_NAME = "extra_v2_append_model_name"
        const val EXTRA_APPEND_SAFE_TORQUE = "extra_v2_append_safe_torque"
        const val EXTRA_APPEND_GEAR_RATIO = "extra_v2_append_gear_ratio"
        const val EXTRA_APPEND_IMAGE_PATH = "extra_v2_append_image_path"

        fun createAppendIntent(context: Context, args: V2ModelAppendArgs): Intent =
            Intent(context, V2ModelAddActivity::class.java).apply {
                putExtra(EXTRA_APPEND_MODEL_ID, args.modelId)
                putExtra(EXTRA_APPEND_MODEL_NAME, args.modelName)
                putExtra(EXTRA_APPEND_SAFE_TORQUE, args.safeTorque)
                putExtra(EXTRA_APPEND_GEAR_RATIO, args.gearRatio)
                putExtra(EXTRA_APPEND_IMAGE_PATH, args.imagePath)
            }

        private fun readAppendArgs(intent: Intent): V2ModelAppendArgs? {
            if (!intent.hasExtra(EXTRA_APPEND_MODEL_ID)) return null
            val modelId = intent.getLongExtra(EXTRA_APPEND_MODEL_ID, 0L)
            if (modelId <= 0L) return null
            return V2ModelAppendArgs(
                modelId = modelId,
                modelName = intent.getStringExtra(EXTRA_APPEND_MODEL_NAME).orEmpty(),
                safeTorque = intent.getStringExtra(EXTRA_APPEND_SAFE_TORQUE).orEmpty(),
                gearRatio = intent.getDoubleExtra(EXTRA_APPEND_GEAR_RATIO, 0.0),
                imagePath = intent.getStringExtra(EXTRA_APPEND_IMAGE_PATH),
            )
        }
    }
}
