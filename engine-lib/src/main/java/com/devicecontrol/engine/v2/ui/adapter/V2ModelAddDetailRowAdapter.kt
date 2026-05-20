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
        holder.bind(items[position])
    }

    class Holder(
        itemView: View,
        private val onValueChanged: (String, String) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvLabel: TextView = itemView.findViewById(R.id.tvDetailLabel)
        private val ivLabelEdit: ImageView = itemView.findViewById(R.id.ivLabelEdit)
        private val etValue: EditText = itemView.findViewById(R.id.etDetailValue)
        private val ivValueEdit: ImageView = itemView.findViewById(R.id.ivValueEdit)

        fun bind(item: V2ModelAddDetailRowUi) {
            val ctx = itemView.context
            val placeholder = item.key.startsWith("empty_")
            itemView.setTag(R.id.tag_v2_detail_row_key, item.key)

            ivValueEdit.visibility = View.VISIBLE
            ivValueEdit.isClickable = false
            ivValueEdit.isFocusable = false

            if (placeholder) {
                tvLabel.visibility = View.GONE
                ivLabelEdit.visibility = View.VISIBLE

                etValue.visibility = View.GONE
                return
            }

            tvLabel.visibility = View.VISIBLE
            tvLabel.text = item.label
            ivLabelEdit.visibility = View.GONE

            etValue.visibility = View.VISIBLE
            etValue.hint = detailFieldHint(ctx, item.key)
            etValue.setHintTextColor(ContextCompat.getColor(ctx, R.color.v2_text_hint))
            etValue.inputType = detailFieldInputType(item.key)
            if (!etValue.isFocused) {
                etValue.setTextKeepSelection(item.value)
            }
            etValue.isEnabled = true
            etValue.isFocusableInTouchMode = true
            etValue.applyV2LandscapeIme()
            etValue.setV2FormTextWatcher { text -> onValueChanged(item.key, text) }
            ivValueEdit.isClickable = true
            ivValueEdit.setOnClickListener { etValue.requestFocus() }
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
