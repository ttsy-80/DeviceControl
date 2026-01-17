package com.devicecontrol.engine.ui.task

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devicecontrol.engine.R
import com.devicecontrol.engine.data.model.ConfigItem

class GearRatioAdapter(
    private val configItems: List<ConfigItem>,
    private val onSelectionChanged: (List<Long>) -> Unit
) : RecyclerView.Adapter<GearRatioAdapter.ViewHolder>() {

    private val selectedConfigItemIds = mutableSetOf<Long>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gear_ratio_checkbox, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val configItem = configItems[position]
        holder.bind(configItem, selectedConfigItemIds.contains(configItem.id))
    }

    override fun getItemCount(): Int = configItems.size

    inner class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val cbGearRatio: CheckBox = itemView.findViewById(R.id.cbGearRatio)
        private val tvGearRatioInfo: TextView = itemView.findViewById(R.id.tvGearRatioInfo)

        fun bind(configItem: ConfigItem, isSelected: Boolean) {
            val info = "变速比: ${configItem.gearRatio} | 位置: ${configItem.position} | 叶片数: ${configItem.bladeCount}"
            tvGearRatioInfo.text = info
            cbGearRatio.isChecked = isSelected

            cbGearRatio.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedConfigItemIds.add(configItem.id)
                } else {
                    selectedConfigItemIds.remove(configItem.id)
                }
                onSelectionChanged(selectedConfigItemIds.toList())
            }
        }
    }

    fun getSelectedConfigItemIds(): List<Long> = selectedConfigItemIds.toList()
}
