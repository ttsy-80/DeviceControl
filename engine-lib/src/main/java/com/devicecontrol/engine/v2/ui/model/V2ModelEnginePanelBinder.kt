package com.devicecontrol.engine.v2.ui.model

import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.model.V2ModelEngineFieldUi

object V2ModelEnginePanelBinder {

    fun bindFields(
        container: LinearLayout,
        fields: List<V2ModelEngineFieldUi>,
        showEditIcon: Boolean,
        onValueChanged: (String, String) -> Unit,
    ) {
        if (container.childCount == fields.size) {
            for (i in fields.indices) {
                val row = container.getChildAt(i)
                bindRow(row, fields[i], showEditIcon, onValueChanged)
            }
            return
        }
        container.removeAllViews()
        val inflater = LayoutInflater.from(container.context)
        fields.forEach { field ->
            val row = inflater.inflate(R.layout.item_v2_model_engine_field, container, false)
            bindRow(row, field, showEditIcon, onValueChanged)
            container.addView(row)
        }
    }

    private fun bindRow(
        row: View,
        field: V2ModelEngineFieldUi,
        showEditIcon: Boolean,
        onValueChanged: (String, String) -> Unit,
    ) {
        row.findViewById<TextView>(R.id.tvFieldLabel).text = field.label
        val et = row.findViewById<EditText>(R.id.etFieldValue)
        val icon = row.findViewById<ImageView>(R.id.ivFieldEdit)
        icon.visibility = if (showEditIcon && field.editable) View.VISIBLE else View.GONE
        et.isEnabled = field.editable
        et.isFocusableInTouchMode = field.editable
        if (!et.isFocused) {
            et.setText(field.value)
        }
        et.doAfterTextChanged {
            if (et.isFocused) {
                onValueChanged(field.key, it?.toString().orEmpty())
            }
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
}
