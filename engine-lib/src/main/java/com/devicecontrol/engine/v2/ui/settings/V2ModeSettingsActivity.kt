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
import androidx.core.widget.doAfterTextChanged
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.ui.base.V2BaseShellActivity
import com.devicecontrol.engine.v2.viewmodel.V2ModeSettingsViewModel
import com.devicecontrol.engine.v2.viewmodel.V2StepperFieldUi

/** 自动/手动模式参数（P8 / P11） */
class V2ModeSettingsActivity : V2BaseShellActivity() {

    override val logTag: String = "ModeSettings"

    private val viewModel: V2ModeSettingsViewModel by viewModels()
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
        stepperContainer = contentRoot.findViewById(R.id.llSteppers)
        if (isManualMode()) viewModel.loadManualMode() else viewModel.loadAutoMode()

        viewModel.fields.observe(this) { fields ->
            if (cellBindings.isEmpty()) {
                renderFields(fields)
            } else {
                syncFieldValues(fields)
            }
        }

        contentRoot.findViewById<View>(R.id.btnConfirmSettings).setOnClickListener {
            commitAllInputs()
            viewModel.confirm()
            finish()
        }
        contentRoot.findViewById<View>(R.id.btnCancelSettings).setOnClickListener { finish() }
    }

    private fun renderFields(fields: List<V2StepperFieldUi>) {
        stepperContainer.removeAllViews()
        cellBindings.clear()
        val inflater = LayoutInflater.from(this)
        if (isManualMode()) {
            fields.forEachIndexed { index, field ->
                val row = inflater.inflate(R.layout.item_v2_mode_setting_row, stepperContainer, false)
                applyRowBackground(row, index)
                row.findViewById<View>(R.id.rowDivider).visibility = View.GONE
                row.findViewById<View>(R.id.cellEnd).visibility = View.GONE
                bindCell(row.findViewById(R.id.cellStart), field)
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
                    row.findViewById<View>(R.id.rowDivider).visibility = View.GONE
                    endCell.visibility = View.GONE
                }
                stepperContainer.addView(row)
            }
        }
    }

    private fun applyRowBackground(row: View, rowIndex: Int) {
        val colorRes = if (rowIndex % 2 == 0) R.color.v2_surface else R.color.v2_table_row_alt
        val color = ContextCompat.getColor(this, colorRes)
        row.findViewById<View>(R.id.modeSettingRowRoot).setBackgroundColor(color)
        row.findViewById<View>(R.id.cellStart).setBackgroundColor(color)
        row.findViewById<View>(R.id.cellEnd).setBackgroundColor(color)
    }

    private fun bindCell(cellRoot: View, field: V2StepperFieldUi) {
        cellRoot.findViewById<ImageView>(R.id.ivModeSettingIcon).setImageResource(field.iconRes)
        cellRoot.findViewById<TextView>(R.id.tvModeSettingLabel).text = field.label
        cellRoot.findViewById<TextView>(R.id.tvStepperUnit).text = field.unit
        val et = cellRoot.findViewById<EditText>(R.id.etStepperValue)
        et.setText(viewModel.formatValue(field))
        val binding = CellBinding(field.key, et)
        cellBindings.add(binding)
        cellRoot.findViewById<View>(R.id.btnMinus).setOnClickListener {
            commitInput(binding)
            viewModel.adjustField(field.key, -1)
        }
        cellRoot.findViewById<View>(R.id.btnPlus).setOnClickListener {
            commitInput(binding)
            viewModel.adjustField(field.key, 1)
        }
        et.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                commitInput(binding)
                v.clearFocus()
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

    private fun syncFieldValues(fields: List<V2StepperFieldUi>) {
        fields.forEach { field ->
            val binding = cellBindings.firstOrNull { it.key == field.key } ?: return@forEach
            if (!binding.editText.isFocused) {
                binding.editText.setText(viewModel.formatValue(field))
            }
        }
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
    }
}
