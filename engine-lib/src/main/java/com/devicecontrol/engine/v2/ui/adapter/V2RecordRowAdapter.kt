package com.devicecontrol.engine.v2.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.model.V2RecordRowUi

class V2RecordRowAdapter : ListAdapter<V2RecordRowUi, V2RecordRowAdapter.Holder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_v2_record_row, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position), position)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rowRoot: View = itemView.findViewById(R.id.rowRoot)
        private val tvPosition: TextView = itemView.findViewById(R.id.tvPosition)
        private val tvBlade: TextView = itemView.findViewById(R.id.tvBlade)

        fun bind(row: V2RecordRowUi, position: Int) {
            tvPosition.text = row.positionLabel
            tvBlade.text = row.bladeCount.toString()
            rowRoot.setBackgroundColor(
                itemView.context.getColor(
                    if (position % 2 == 1) R.color.v2_table_row_alt else R.color.v2_surface,
                ),
            )
        }
    }

    private object Diff : DiffUtil.ItemCallback<V2RecordRowUi>() {
        override fun areItemsTheSame(a: V2RecordRowUi, b: V2RecordRowUi) =
            a.positionLabel == b.positionLabel && a.bladeCount == b.bladeCount

        override fun areContentsTheSame(a: V2RecordRowUi, b: V2RecordRowUi) = a == b
    }
}
