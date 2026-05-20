package com.devicecontrol.engine.v2.ui.model

import android.graphics.BitmapFactory
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.model.V2ModelEngineFieldUi
import com.devicecontrol.engine.v2.ui.widget.applyV2LandscapeIme
import com.devicecontrol.engine.v2.ui.widget.setV2FormTextWatcher
import java.io.File

object V2ModelEnginePanelBinder {

    private val addEngineFields = listOf(
        V2ModelEngineFieldUi("model_name", "型号名称：", ""),
        V2ModelEngineFieldUi("safe_torque", "安全扭矩：", ""),
        V2ModelEngineFieldUi("gear_ratio", "变速比：", ""),
    )

    /** P14 新建：型号名称 / 安全扭矩 / 变速比 */
    fun setupAddEngineFields(
        container: LinearLayout,
        onModelNameChanged: (String) -> Unit,
    ) {
        if (container.childCount > 0) return
        val inflater = LayoutInflater.from(container.context)
        addEngineFields.forEach { field ->
            val row = inflater.inflate(R.layout.item_v2_model_engine_field, container, false)
            container.addView(row)
            bindAddFieldRow(row, field, onModelNameChanged)
        }
    }

    /**
     * 从详情页进入添加配置项：型号/安全力矩/变速比只读，图片区只展示不可导入。
     */
    fun setupAppendConfigEngineFields(
        panelRoot: View,
        container: LinearLayout,
        modelName: String,
        safeTorque: String,
        gearRatio: String,
        imagePath: String?,
        onModelNameChanged: (String) -> Unit,
    ) {
        if (container.childCount == 0) {
            val inflater = LayoutInflater.from(container.context)
            listOf(
                V2ModelEngineFieldUi("model_name", "型号名称：", modelName, editable = false),
                V2ModelEngineFieldUi("safe_torque", "安全力矩：", safeTorque, editable = false),
                V2ModelEngineFieldUi("gear_ratio", "变速比：", gearRatio, editable = false),
            ).forEach { field ->
                val row = inflater.inflate(R.layout.item_v2_model_engine_field, container, false)
                container.addView(row)
                bindLockedFieldRow(row, field)
            }
        } else {
            bindLockedValues(container, modelName, safeTorque, gearRatio)
        }
        onModelNameChanged(modelName)
        configureImageArea(panelRoot, showImport = false)
        bindEngineImage(panelRoot, imagePath)
//        showModelNameStrip(panelRoot, modelName)
    }

    fun showModelNameStrip(panelRoot: View, modelName: String) {
        panelRoot.findViewById<TextView>(R.id.tvPanelModelName)?.apply {
            text = modelName
            visibility = View.VISIBLE
        }
    }

    fun bindEngineImage(panelRoot: View, imagePath: String?) {
        val iv = panelRoot.findViewById<ImageView>(R.id.ivEnginePhoto) ?: return
        val importBtn = panelRoot.findViewById<View>(R.id.btnImportImage)
        if (imagePath.isNullOrBlank()) {
            iv.isVisible = false
            return
        }
        val file = File(imagePath)
        if (!file.exists()) {
            iv.isVisible = false
            return
        }
        BitmapFactory.decodeFile(imagePath)?.let { iv.setImageBitmap(it) }
        iv.isVisible = true
        importBtn?.isVisible = false
    }

    fun bindFields(
        container: LinearLayout,
        fields: List<V2ModelEngineFieldUi>,
        showEditIcon: Boolean,
        onValueChanged: (String, String) -> Unit,
    ) {
        if (container.childCount != fields.size) {
            container.removeAllViews()
            val inflater = LayoutInflater.from(container.context)
            fields.forEach { _ ->
                val row = inflater.inflate(R.layout.item_v2_model_engine_field, container, false)
                container.addView(row)
            }
        }
        for (i in fields.indices) {
            bindDetailFieldRow(container.getChildAt(i), fields[i], showEditIcon, onValueChanged)
        }
    }

    fun readFieldValues(container: LinearLayout): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (i in 0 until container.childCount) {
            val row = container.getChildAt(i)
            val key = row.getTag(R.id.tag_v2_field_key) as? String ?: continue
            val et = row.findViewById<EditText>(R.id.etFieldValue)
            map[key] = et.text?.toString().orEmpty()
        }
        return map
    }

    private fun bindAddFieldRow(
        row: View,
        field: V2ModelEngineFieldUi,
        onModelNameChanged: (String) -> Unit,
    ) {
        row.setTag(R.id.tag_v2_field_key, field.key)
        row.setTag(R.id.tag_v2_form_bound, true)
        row.findViewById<TextView>(R.id.tvFieldLabel).text = field.label
        val et = row.findViewById<EditText>(R.id.etFieldValue)
        val icon = row.findViewById<ImageView>(R.id.ivFieldEdit)
        icon.visibility = View.VISIBLE
        et.isEnabled = true
        et.isFocusableInTouchMode = true
        val ctx = row.context
        et.hint = engineFieldHint(ctx, field.key)
        et.setHintTextColor(ContextCompat.getColor(ctx, R.color.v2_text_hint))
        et.inputType = engineFieldInputType(field.key)
        et.applyV2LandscapeIme()
        if (field.value.isNotEmpty()) {
            et.setText(field.value)
        }
        et.setV2FormTextWatcher { text ->
            if (field.key == "model_name") onModelNameChanged(text)
        }
        icon.setOnClickListener { et.requestFocus() }
    }

    private fun bindLockedFieldRow(row: View, field: V2ModelEngineFieldUi) {
        row.setTag(R.id.tag_v2_field_key, field.key)
        row.setTag(R.id.tag_v2_form_bound, true)
        row.findViewById<TextView>(R.id.tvFieldLabel).text = field.label
        val et = row.findViewById<EditText>(R.id.etFieldValue)
        row.findViewById<ImageView>(R.id.ivFieldEdit).visibility = View.GONE
        et.setText(field.value)
        et.isEnabled = false
        et.isFocusable = false
        et.isFocusableInTouchMode = false
        et.isClickable = false
        et.inputType = InputType.TYPE_NULL
    }

    private fun bindLockedValues(
        container: LinearLayout,
        modelName: String,
        safeTorque: String,
        gearRatio: String,
    ) {
        for (i in 0 until container.childCount) {
            val row = container.getChildAt(i)
            when (row.getTag(R.id.tag_v2_field_key) as? String) {
                "model_name" -> row.findViewById<EditText>(R.id.etFieldValue).setText(modelName)
                "safe_torque" -> row.findViewById<EditText>(R.id.etFieldValue).setText(safeTorque)
                "gear_ratio" -> row.findViewById<EditText>(R.id.etFieldValue).setText(gearRatio)
            }
        }
    }

    private fun bindDetailFieldRow(
        row: View,
        field: V2ModelEngineFieldUi,
        showEditIcon: Boolean,
        onValueChanged: (String, String) -> Unit,
    ) {
        val bound = row.getTag(R.id.tag_v2_form_bound) == true
        row.setTag(R.id.tag_v2_field_key, field.key)
        row.findViewById<TextView>(R.id.tvFieldLabel).text = field.label
        val et = row.findViewById<EditText>(R.id.etFieldValue)
        val icon = row.findViewById<ImageView>(R.id.ivFieldEdit)
        icon.visibility = if (showEditIcon && field.editable) View.VISIBLE else View.GONE
        et.isEnabled = field.editable
        et.isFocusableInTouchMode = field.editable
        if (!bound) {
            val ctx = row.context
            et.hint = engineFieldHint(ctx, field.key)
            et.setHintTextColor(ContextCompat.getColor(ctx, R.color.v2_text_hint))
            et.inputType = engineFieldInputType(field.key)
            et.applyV2LandscapeIme()
            et.setText(field.value)
            et.setV2FormTextWatcher { text -> onValueChanged(field.key, text) }
            row.setTag(R.id.tag_v2_form_bound, true)
        } else if (!et.isFocused) {
            et.setText(field.value)
        }
        icon.setOnClickListener { et.requestFocus() }
    }

    fun configureImageArea(
        panelRoot: View,
        showImport: Boolean,
    ) {
        panelRoot.findViewById<View>(R.id.btnImportImage).visibility =
            if (showImport) View.VISIBLE else View.GONE
        panelRoot.findViewById<View>(R.id.ivEnginePhoto).visibility =
            if (showImport) View.GONE else View.VISIBLE
    }

    private fun engineFieldHint(ctx: android.content.Context, key: String): String =
        when (key) {
            "model_name" -> ctx.getString(R.string.v2_hint_model_name)
            "safe_torque" -> ctx.getString(R.string.v2_hint_safe_torque)
            "gear_ratio" -> ctx.getString(R.string.v2_hint_gear_ratio)
            else -> ""
        }

    private fun engineFieldInputType(key: String): Int =
        when (key) {
            "gear_ratio" -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            else -> InputType.TYPE_CLASS_TEXT
        }
}
