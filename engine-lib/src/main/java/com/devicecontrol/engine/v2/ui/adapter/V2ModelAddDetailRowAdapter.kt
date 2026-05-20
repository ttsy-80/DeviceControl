package com.devicecontrol.engine.v2.ui.adapter

import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.model.V2ModelAddDetailRowUi
import com.devicecontrol.engine.v2.ui.widget.applyV2LandscapeIme
import com.devicecontrol.engine.v2.ui.widget.setTextKeepSelection
import com.devicecontrol.engine.v2.ui.widget.setV2FormTextWatcher

class V2ModelAddDetailRowAdapter(
    private val onValueChanged: (String, String) -> Unit,
) : RecyclerView.Adapter<V2ModelAddDetailRowAdapter.Holder>() {

    private val items = mutableListOf<V2ModelAddDetailRowUi>()

    fun submitRows(rows: List<V2ModelAddDetailRowUi>) {
        items.clear()
        items.addAll(rows)
        notifyDataSetChanged()
    }

    fun readRowValues(recyclerView: RecyclerView): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (i in items.indices) {
            val key = items[i].key
            if (key.startsWith("empty_")) continue
            val holder = recyclerView.findViewHolderForAdapterPosition(i) as? Holder
            map[key] = holder?.currentValue() ?: items[i].value
        }
        return map
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_v2_model_add_detail_row, parent, false)
        return Holder(view, onValueChanged)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position], position)
    }

    class Holder(
        itemView: android.view.View,
        private val onValueChanged: (String, String) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvLabel: TextView = itemView.findViewById(R.id.tvDetailLabel)
        private val etValue: EditText = itemView.findViewById(R.id.etDetailValue)
        private val ivEdit: ImageView = itemView.findViewById(R.id.ivDetailEdit)

        fun bind(item: V2ModelAddDetailRowUi, position: Int) {
            val ctx = itemView.context
            val placeholder = item.key.startsWith("empty_")
            itemView.setTag(R.id.tag_v2_detail_row_key, item.key)
            tvLabel.text = item.label
            tvLabel.visibility = if (item.label.isBlank()) View.INVISIBLE else View.VISIBLE
            etValue.hint = if (placeholder) "" else detailFieldHint(ctx, item.key)
            etValue.setHintTextColor(ContextCompat.getColor(ctx, R.color.v2_text_hint))
            etValue.inputType = detailFieldInputType(item.key)
            if (!placeholder) {
                etValue.applyV2LandscapeIme()
            }
            if (!etValue.isFocused) {
                etValue.setTextKeepSelection(item.value)
            }
            if (placeholder) {
                etValue.isEnabled = false
                etValue.isFocusable = false
                etValue.isFocusableInTouchMode = false
                etValue.isClickable = false
                ivEdit.visibility = View.INVISIBLE
                ivEdit.setOnClickListener(null)
                itemView.isClickable = false
                itemView.isFocusable = false
            } else {
                etValue.isEnabled = true
                etValue.isFocusableInTouchMode = true
                etValue.isClickable = true
                ivEdit.visibility = View.VISIBLE
                etValue.setV2FormTextWatcher { text -> onValueChanged(item.key, text) }
                ivEdit.setOnClickListener { etValue.requestFocus() }
                itemView.isClickable = true
            }
        }

        fun currentValue(): String = etValue.text?.toString().orEmpty()
    }

    companion object {
        private fun detailFieldHint(ctx: android.content.Context, key: String): String =
            when (key) {
                "position" -> ctx.getString(R.string.v2_hint_position)
                "blade" -> ctx.getString(R.string.v2_hint_blade_count)
                else -> ""
            }

        private fun detailFieldInputType(key: String): Int =
            when (key) {
                "blade" -> InputType.TYPE_CLASS_NUMBER
                "position" -> InputType.TYPE_CLASS_TEXT
                else -> InputType.TYPE_CLASS_TEXT
            }
    }
}
