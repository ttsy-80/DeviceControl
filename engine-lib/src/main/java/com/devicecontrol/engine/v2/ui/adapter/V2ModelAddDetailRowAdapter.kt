package com.devicecontrol.engine.v2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.model.V2ModelAddDetailRowUi

class V2ModelAddDetailRowAdapter(
    private val onValueChanged: (String, String) -> Unit,
) : ListAdapter<V2ModelAddDetailRowUi, V2ModelAddDetailRowAdapter.Holder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_v2_model_add_detail_row, parent, false)
        return Holder(view, onValueChanged)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position), position)
    }

    class Holder(
        itemView: android.view.View,
        private val onValueChanged: (String, String) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvLabel: TextView = itemView.findViewById(R.id.tvDetailLabel)
        private val etValue: EditText = itemView.findViewById(R.id.etDetailValue)

        fun bind(item: V2ModelAddDetailRowUi, position: Int) {
            val ctx = itemView.context
            itemView.setBackgroundColor(
                ContextCompat.getColor(
                    ctx,
                    if (position % 2 == 0) R.color.v2_surface else R.color.v2_table_row_alt,
                ),
            )
            tvLabel.text = item.label
            if (!etValue.isFocused) {
                etValue.setText(item.value)
            }
            etValue.doAfterTextChanged {
                if (etValue.isFocused) {
                    onValueChanged(item.key, it?.toString().orEmpty())
                }
            }
        }
    }

    private object Diff : DiffUtil.ItemCallback<V2ModelAddDetailRowUi>() {
        override fun areItemsTheSame(a: V2ModelAddDetailRowUi, b: V2ModelAddDetailRowUi) = a.key == b.key
        override fun areContentsTheSame(a: V2ModelAddDetailRowUi, b: V2ModelAddDetailRowUi) = a == b
    }
}
