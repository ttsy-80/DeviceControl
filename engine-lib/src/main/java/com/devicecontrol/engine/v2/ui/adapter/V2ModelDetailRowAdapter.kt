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
import com.devicecontrol.engine.v2.viewmodel.V2ModelDetailPageMode
import com.devicecontrol.engine.v2.viewmodel.V2ModelDetailRowUi

class V2ModelDetailRowAdapter(
    private val onStartRowEdit: (V2ModelDetailRowUi) -> Unit,
    private val onDuplicateRow: (V2ModelDetailRowUi) -> Unit,
    private val onRowSave: (V2ModelDetailRowUi, String, Int) -> Unit,
    private val onRowDelete: (V2ModelDetailRowUi) -> Unit,
) : ListAdapter<V2ModelDetailRowUi, V2ModelDetailRowAdapter.Holder>(Diff) {

    var pageMode: V2ModelDetailPageMode = V2ModelDetailPageMode.VIEW
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_v2_model_detail_row, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position), pageMode, onStartRowEdit, onDuplicateRow, onRowSave, onRowDelete)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val root: View = itemView.findViewById(R.id.modelDetailRowRoot)
        private val etPosition: EditText = itemView.findViewById(R.id.etPosition)
        private val etBlade: EditText = itemView.findViewById(R.id.etBladeCount)
        private val btnAdd: TextView = itemView.findViewById(R.id.btnRowAdd)
        private val btnSave: TextView = itemView.findViewById(R.id.btnRowSave)
        private val btnDelete: TextView = itemView.findViewById(R.id.btnRowDelete)

        fun bind(
            item: V2ModelDetailRowUi,
            pageMode: V2ModelDetailPageMode,
            onStartRowEdit: (V2ModelDetailRowUi) -> Unit,
            onDuplicateRow: (V2ModelDetailRowUi) -> Unit,
            onRowSave: (V2ModelDetailRowUi, String, Int) -> Unit,
            onRowDelete: (V2ModelDetailRowUi) -> Unit,
        ) {
            val ctx = itemView.context
            val alt = bindingAdapterPosition % 2 == 1
            root.setBackgroundColor(
                ContextCompat.getColor(
                    ctx,
                    when {
                        item.isRowEditing -> R.color.v2_table_row_alt
                        alt -> R.color.v2_table_row_alt
                        else -> R.color.v2_surface
                    },
                ),
            )

            val tableEditing = pageMode == V2ModelDetailPageMode.TABLE_EDIT
            val rowEditing = item.isRowEditing

            etPosition.setText(item.position)
            etBlade.setText(item.bladeCount.toString())

            etPosition.isEnabled = tableEditing || rowEditing
            etBlade.isEnabled = tableEditing || rowEditing
            etPosition.isFocusableInTouchMode = tableEditing || rowEditing
            etBlade.isFocusableInTouchMode = tableEditing || rowEditing
            etPosition.inputType = if (tableEditing || rowEditing) {
                InputType.TYPE_CLASS_TEXT
            } else {
                InputType.TYPE_NULL
            }
            etBlade.inputType = if (tableEditing || rowEditing) {
                InputType.TYPE_CLASS_NUMBER
            } else {
                InputType.TYPE_NULL
            }

            if (tableEditing || rowEditing) {
                etPosition.setBackgroundResource(R.drawable.bg_v2_mode_setting_value)
                etBlade.setBackgroundResource(R.drawable.bg_v2_mode_setting_value)
            } else {
                etPosition.background = null
                etBlade.background = null
            }

            val showRowSave = rowEditing && !tableEditing
            btnSave.visibility = if (showRowSave) View.VISIBLE else View.GONE
            btnAdd.visibility = if (showRowSave) View.GONE else View.VISIBLE

            btnAdd.setOnClickListener {
                if (tableEditing) {
                    onDuplicateRow(item)
                } else {
                    onStartRowEdit(item)
                }
            }
            btnSave.setOnClickListener {
                val blades = etBlade.text?.toString()?.toIntOrNull() ?: item.bladeCount
                onRowSave(item, etPosition.text?.toString().orEmpty(), blades)
            }
            btnDelete.setOnClickListener { onRowDelete(item) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<V2ModelDetailRowUi>() {
        override fun areItemsTheSame(a: V2ModelDetailRowUi, b: V2ModelDetailRowUi) = a.id == b.id
        override fun areContentsTheSame(a: V2ModelDetailRowUi, b: V2ModelDetailRowUi) = a == b
    }
}
