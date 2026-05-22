package com.devicecontrol.engine.v2.ui.dialog

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.log.V2Log
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/** P12：LPC 位置选择弹层 */
object V2LpcDialog {

    private const val TAG = "LpcDialog"

    fun show(
        context: Context,
        positions: List<String>,
        bladeCounts: List<Int>,
        selectedIndex: Int,
        onSelected: (Int) -> Unit,
    ) {
        V2Log.i(TAG, "show lpc picker selected=$selectedIndex")
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_v2_lpc, null)
        val rv = view.findViewById<RecyclerView>(R.id.rvLpcList)
        var picked = selectedIndex
        val adapter = object : RecyclerView.Adapter<VH>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
                val row = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_v2_lpc_pick_row, parent, false)
                return VH(row)
            }

            override fun getItemCount() = positions.size

            override fun onBindViewHolder(holder: VH, position: Int) {
                val selected = position == picked
                holder.tvPosition.text = positions[position]
                holder.tvBlade.text = bladeCounts.getOrNull(position)?.toString().orEmpty()
                holder.itemView.setBackgroundResource(
                    if (selected) R.drawable.bg_v2_lpc_row_selected else android.R.color.transparent,
                )
                val textColor = ContextCompat.getColor(
                    holder.itemView.context,
                    if (selected) R.color.v2_text_primary else R.color.v2_text_secondary,
                )
                holder.tvPosition.setTextColor(textColor)
                holder.tvBlade.setTextColor(textColor)
                holder.tvPosition.setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
                holder.tvBlade.setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
                holder.itemView.setOnClickListener {
                    picked = position
                    notifyDataSetChanged()
                }
            }
        }
        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = adapter

        val dialog = MaterialAlertDialogBuilder(context)
            .setView(view)
            .create()
        view.findViewById<View>(R.id.btnLpcCancel).setOnClickListener { dialog.dismiss() }
        view.findViewById<View>(R.id.btnLpcSelect).setOnClickListener {
            V2Log.i(TAG, "lpc selected index=$picked")
            onSelected(picked)
            dialog.dismiss()
        }
        dialog.show()
    }

    private class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvPosition: TextView = view.findViewById(R.id.tvLpcPosition)
        val tvBlade: TextView = view.findViewById(R.id.tvLpcBlade)
    }
}
