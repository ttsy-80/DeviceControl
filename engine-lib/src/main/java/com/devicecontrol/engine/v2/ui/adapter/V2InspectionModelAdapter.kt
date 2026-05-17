package com.devicecontrol.engine.v2.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.model.V2ModelCardUi
import com.google.android.material.card.MaterialCardView

/** P6 发动机检测 - 型号宫格（橙/桃选中态，无「新增」卡） */
class V2InspectionModelAdapter(
    private val onClick: (V2ModelCardUi) -> Unit,
) : ListAdapter<V2ModelCardUi, V2InspectionModelAdapter.Holder>(Diff) {

    var selectedId: Long? = null
        set(value) {
            if (field == value) return
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_v2_inspection_model_card, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position), selectedId, onClick)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.cardRoot)
        private val tvName: TextView = itemView.findViewById(R.id.tvModelName)

        fun bind(item: V2ModelCardUi, selectedId: Long?, onClick: (V2ModelCardUi) -> Unit) {
            val ctx = itemView.context
            tvName.text = item.name
            val bgRes = if (item.id == selectedId) R.color.v2_peach else R.color.v2_primary
            card.setCardBackgroundColor(ContextCompat.getColor(ctx, bgRes))
            card.setOnClickListener { onClick(item) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<V2ModelCardUi>() {
        override fun areItemsTheSame(a: V2ModelCardUi, b: V2ModelCardUi) = a.id == b.id
        override fun areContentsTheSame(a: V2ModelCardUi, b: V2ModelCardUi) = a == b
    }
}
