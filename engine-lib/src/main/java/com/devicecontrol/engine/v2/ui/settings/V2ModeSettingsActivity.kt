package com.devicecontrol.engine.v2.ui.settings

import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.ui.base.V2BaseShellActivity
import com.devicecontrol.engine.v2.ui.widget.applyV2LandscapeIme
import com.devicecontrol.engine.v2.ui.widget.finishStepperEditing
import com.devicecontrol.engine.v2.ui.widget.hideSoftKeyboard
import com.devicecontrol.engine.v2.viewmodel.V2EngineViewModelFactory
import com.devicecontrol.engine.v2.viewmodel.V2ModeSettingsViewModel
import com.devicecontrol.engine.v2.viewmodel.V2StepperFieldUi
import kotlinx.coroutines.launch

/** 自动/手动模式参数（P8 / P11） */
class V2ModeSettingsActivity : V2BaseShellActivity() {

    override val logTag: String = "ModeSettings"

    private val viewModel: V2ModeSettingsViewModel by viewModels {
        V2EngineViewModelFactory(application)
    }
    private lateinit var stepperContainer: LinearLayout
    private val cellBindings = mutableListOf<CellBinding>()

    override fun contentLayoutId(): Int = R.layout.content_v2_mode_settings

    override fun shellTitleCn(): String =
        if (isManualMode()) getString(R.string.v2_manual_mode_setting_cn)
        else getString(R.string.v2_auto_mode_setting_cn)

    override fun shellTitleEn(): String =
        if (isManualMode()) getString(R.string.v2_manual_mode_setting_en)
        else getString(R.string.v2_auto_mode_setting_en)

    private fun isManualMode(): Boolean =
        intent.getBooleanExtra(EXTRA_MANUAL, false)

    override fun showBackHome(): Boolean = true

    override fun onContentCreated(contentRoot: View) {
        shellBinding.flV2Content.setBackgroundColor(
            ContextCompat.getColor(this, R.color.v2_surface),
        )
        stepperContainer = contentRoot.findViewById(R.id.llSteppers)
        val modelId = intent.getLongExtra(EXTRA_MODEL_ID, 0L)
        viewModel.init(modelId, isManualMode())

        viewModel.fields.observe(this) { fields ->
            if (cellBindings.isEmpty()) {
                renderFields(fields)
            } else {
                syncFieldValues(fields)
            }
        }

        contentRoot.findViewById<View>(R.id.btnConfirmSettings).setOnClickListener {
            commitAllInputs()
            lifecycleScope.launch {
                viewModel.saveSettings()
                finish()
            }
        }
        contentRoot.findViewById<View>(R.id.btnCancelSettings).setOnClickListener { finish() }
    }

    private fun renderFields(fields: List<V2StepperFieldUi>) {
        stepperContainer.removeAllViews()
        cellBindings.clear()
        val inflater = LayoutInflater.from(this)
        if (isManualMode()) {
            fields.forEachIndexed { index, field ->
                val row = inflater.inflate(R.layout.item_v2_mode_setting_cell_manual, stepperContainer, false)
                applyRowBackground(row, index, manualSingleRow = true)
                bindCell(row, field)
                stepperContainer.addView(row)
            }
        } else {
            fields.chunked(2).forEachIndexed { rowIndex, pair ->
                val row = inflater.inflate(R.layout.item_v2_mode_setting_row, stepperContainer, false)
                applyRowBackground(row, rowIndex)
                bindCell(row.findViewById(R.id.cellStart), pair[0])
                val endCell = row.findViewById<View>(R.id.cellEnd)
                if (pair.size > 1) {
                    bindCell(endCell, pair[1])
                } else {
                    bindEmptyCell(endCell)
                }
                stepperContainer.addView(row)
            }
        }
    }

    private fun applyRowBackground(row: View, rowIndex: Int, manualSingleRow: Boolean = false) {
        val colorRes = if (rowIndex % 2 == 0) {
            R.color.v2_mode_setting_row_gray
        } else {
            R.color.v2_table_row_alt
        }
        val color = ContextCompat.getColor(this, colorRes)
        if (manualSingleRow) {
            row.setBackgroundColor(color)
        } else {
            row.findViewById<View>(R.id.modeSettingRowRoot).setBackgroundColor(color)
            row.findViewById<View>(R.id.cellStart).setBackgroundColor(color)
            row.findViewById<View>(R.id.cellEnd).setBackgroundColor(color)
        }
    }

    private fun bindCell(cellRoot: View, field: V2StepperFieldUi) {
        cellRoot.findViewById<ImageView>(R.id.ivModeSettingIcon).apply {
            visibility = View.VISIBLE
            setImageResource(field.iconRes)
        }
        cellRoot.findViewById<TextView>(R.id.tvModeSettingLabel).apply {
            visibility = View.VISIBLE
            text = field.label
        }
        cellRoot.findViewById<View>(R.id.groupStepper)?.visibility = View.VISIBLE
        cellRoot.findViewById<View>(R.id.groupLabel)?.visibility = View.VISIBLE
        cellRoot.findViewById<TextView>(R.id.tvStepperUnit).text = field.unit
        val et = cellRoot.findViewById<EditText>(R.id.etStepperValue)
        et.applyV2LandscapeIme()
        et.setText(viewModel.formatValue(field))
        val binding = CellBinding(field.key, et)
        cellBindings.add(binding)
        cellRoot.findViewById<View>(R.id.btnMinus).setOnClickListener {
            onStepperButtonClicked()
            viewModel.adjustField(field.key, -1)
        }
        cellRoot.findViewById<View>(R.id.btnPlus).setOnClickListener {
            onStepperButtonClicked()
            viewModel.adjustField(field.key, 1)
        }
        et.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                commitInput(binding)
                (v as EditText).finishStepperEditing()
                true
            } else {
                false
            }
        }
        et.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                commitInput(binding)
            }
        }
    }

    private fun bindEmptyCell(cellRoot: View) {
        cellRoot.findViewById<View>(R.id.ivModeSettingIcon).visibility = View.INVISIBLE
        cellRoot.findViewById<View>(R.id.tvModeSettingLabel).visibility = View.INVISIBLE
        cellRoot.findViewById<View>(R.id.groupStepper)?.visibility = View.INVISIBLE
        cellRoot.findViewById<View>(R.id.groupLabel)?.visibility = View.INVISIBLE
    }

    private fun syncFieldValues(fields: List<V2StepperFieldUi>) {
        fields.forEach { field ->
            val binding = cellBindings.firstOrNull { it.key == field.key } ?: return@forEach
            if (!binding.editText.isFocused) {
                binding.editText.setText(viewModel.formatValue(field))
            }
        }
    }

    /** 点击 ± 时：先提交全部输入（含正在编辑项），再取消焦点并收起键盘 */
    private fun onStepperButtonClicked() {
        commitAllInputs()
        finishAllStepperEditing()
    }

    private fun finishAllStepperEditing() {
        cellBindings.forEach { cell ->
            if (cell.editText.isFocused) {
                cell.editText.clearFocus()
            }
        }
        (currentFocus ?: window.decorView).hideSoftKeyboard()
    }

    private fun commitInput(binding: CellBinding) {
        viewModel.setFieldValue(binding.key, binding.editText.text?.toString().orEmpty())
    }

    private fun commitAllInputs() {
        cellBindings.forEach { commitInput(it) }
    }

    private data class CellBinding(
        val key: String,
        val editText: EditText,
    )

    companion object {
        const val EXTRA_MANUAL = "extra_v2_manual_mode"
        const val EXTRA_MODEL_ID = "extra_v2_mode_settings_model_id"
    }
}
