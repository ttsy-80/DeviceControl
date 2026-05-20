package com.devicecontrol.engine.v2.ui.adapter

import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.ui.widget.applyV2LandscapeIme
import com.devicecontrol.engine.v2.ui.widget.setTextKeepSelection
import com.devicecontrol.engine.v2.viewmodel.V2ModelDetailPageMode
import com.devicecontrol.engine.v2.viewmodel.V2ModelDetailRowUi

class V2ModelDetailRowAdapter(
    private val onAddConfig: () -> Unit,
    private val onDuplicateRow: (V2ModelDetailRowUi) -> Unit,
    private val onRowDelete: (V2ModelDetailRowUi) -> Unit,
) : ListAdapter<V2ModelDetailRowUi, V2ModelDetailRowAdapter.Holder>(Diff) {

    var pageMode: V2ModelDetailPageMode = V2ModelDetailPageMode.VIEW
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun readTableRowValues(recyclerView: RecyclerView): List<V2ModelDetailRowUi> {
        val result = mutableListOf<V2ModelDetailRowUi>()
        for (i in 0 until itemCount) {
            val item = getItem(i)
            val holder = recyclerView.findViewHolderForAdapterPosition(i) as? Holder
            val position = holder?.positionText().orEmpty().ifEmpty { item.position }
            val blades = holder?.bladeText()?.toIntOrNull() ?: item.bladeCount
            result.add(item.copy(position = position, bladeCount = blades, isRowEditing = false))
        }
        return result
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_v2_model_detail_row, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(
            item = getItem(position),
            pageMode = pageMode,
            onAddConfig = onAddConfig,
            onDuplicateRow = onDuplicateRow,
            onRowDelete = onRowDelete,
        )
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rowContent: View = itemView.findViewById(R.id.detailRowContent)
        private val dividerRowBottom: View = itemView.findViewById(R.id.dividerRowBottom)
        private val etPosition: EditText = itemView.findViewById(R.id.etPosition)
        private val etBlade: EditText = itemView.findViewById(R.id.etBladeCount)
        private val btnAdd: TextView = itemView.findViewById(R.id.btnRowAdd)
        private val btnSave: TextView = itemView.findViewById(R.id.btnRowSave)
        private val btnDelete: TextView = itemView.findViewById(R.id.btnRowDelete)
        private var bound = false

        fun positionText(): String = etPosition.text?.toString().orEmpty()

        fun bladeText(): String = etBlade.text?.toString().orEmpty()

        fun bind(
            item: V2ModelDetailRowUi,
            pageMode: V2ModelDetailPageMode,
            onAddConfig: () -> Unit,
            onDuplicateRow: (V2ModelDetailRowUi) -> Unit,
            onRowDelete: (V2ModelDetailRowUi) -> Unit,
        ) {
            val ctx = itemView.context
            val tableEditing = pageMode == V2ModelDetailPageMode.TABLE_EDIT
            val textColor = ContextCompat.getColor(ctx, R.color.v2_text_primary)
            rowContent.setBackgroundColor(ContextCompat.getColor(ctx, R.color.v2_surface))
            etPosition.setTextColor(textColor)
            etBlade.setTextColor(textColor)
            // 非最后一行显示行底橙线；数据变更时按 rowIndex 绑定，避免新增行后上一行不重绑导致缺线
            dividerRowBottom.visibility =
                if (item.showBottomDivider) View.VISIBLE else View.GONE

            if (!bound) {
                etPosition.applyV2LandscapeIme()
                etBlade.applyV2LandscapeIme()
                bound = true
            }

            if (!etPosition.isFocused) {
                etPosition.setTextKeepSelection(item.position)
            }
            if (!etBlade.isFocused) {
                etBlade.setTextKeepSelection(item.bladeCount.toString())
            }

            etPosition.isEnabled = tableEditing
            etBlade.isEnabled = tableEditing
            etPosition.isFocusableInTouchMode = tableEditing
            etBlade.isFocusableInTouchMode = tableEditing
            etPosition.inputType = if (tableEditing) InputType.TYPE_CLASS_TEXT else InputType.TYPE_NULL
            etBlade.inputType = if (tableEditing) InputType.TYPE_CLASS_NUMBER else InputType.TYPE_NULL

            if (tableEditing) {
                etPosition.setBackgroundResource(R.drawable.bg_v2_mode_setting_value)
                etBlade.setBackgroundResource(R.drawable.bg_v2_mode_setting_value)
            } else {
                etPosition.background = null
                etBlade.background = null
            }

            btnSave.visibility = View.GONE
            btnAdd.visibility = View.VISIBLE

            btnAdd.setOnClickListener {
                if (tableEditing) {
                    onDuplicateRow(item)
                } else {
                    onAddConfig()
                }
            }
            btnDelete.setOnClickListener { onRowDelete(item) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<V2ModelDetailRowUi>() {
        override fun areItemsTheSame(a: V2ModelDetailRowUi, b: V2ModelDetailRowUi) =
            a.configItemId == b.configItemId

        override fun areContentsTheSame(a: V2ModelDetailRowUi, b: V2ModelDetailRowUi) =
            a == b && a.listGeneration == b.listGeneration
    }
}
