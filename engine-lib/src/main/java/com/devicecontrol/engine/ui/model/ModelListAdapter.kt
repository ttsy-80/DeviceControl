package com.devicecontrol.engine.ui.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devicecontrol.engine.R
import com.devicecontrol.engine.data.model.ConfigItem
import com.devicecontrol.engine.data.model.EngineModelWithConfigItems

class ModelListAdapter(
    private val onAddConfigItem: (Long) -> Unit,
    private val onEditConfigItem: (ConfigItem) -> Unit,
    private val onDeleteConfigItem: (ConfigItem) -> Unit,
    private val onDeleteModel: (Long) -> Unit
) : RecyclerView.Adapter<ModelListAdapter.ViewHolder>() {

    private var models: List<EngineModelWithConfigItems> = emptyList()
    private var totalItems = 0

    fun submitList(newModels: List<EngineModelWithConfigItems>) {
        models = newModels
        totalItems = models.sumOf { it.configItems.size }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = totalItems

    override fun getItemViewType(position: Int): Int {
        var currentPosition = 0
        for (model in models) {
            if (position < currentPosition + model.configItems.size) {
                val itemIndex = position - currentPosition
                return if (itemIndex == 0) VIEW_TYPE_FIRST_ITEM else VIEW_TYPE_NORMAL_ITEM
            }
            currentPosition += model.configItems.size
        }
        return VIEW_TYPE_NORMAL_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_model_config, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var currentPosition = 0
        for (model in models) {
            if (position < currentPosition + model.configItems.size) {
                val itemIndex = position - currentPosition
                val configItem = model.configItems[itemIndex]
                val isFirstItem = itemIndex == 0
                
                holder.bind(
                    model = model,
                    configItem = configItem,
                    isFirstItem = isFirstItem,
                    isLastItem = itemIndex == model.configItems.size - 1,
                    spanCount = model.configItems.size,
                    onAddConfigItem = onAddConfigItem,
                    onEditConfigItem = onEditConfigItem,
                    onDeleteConfigItem = onDeleteConfigItem,
                    onDeleteModel = onDeleteModel
                )
                return
            }
            currentPosition += model.configItems.size
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvModelName: TextView = itemView.findViewById(R.id.tvModelName)
        private val tvGearRatio: TextView = itemView.findViewById(R.id.tvGearRatio)
        private val tvPosition: TextView = itemView.findViewById(R.id.tvPosition)
        private val tvBladeCount: TextView = itemView.findViewById(R.id.tvBladeCount)
        private val tvJogCount: TextView = itemView.findViewById(R.id.tvJogCount)
        private val btnAddConfigItem: Button = itemView.findViewById(R.id.btnAddConfigItem)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(
            model: EngineModelWithConfigItems,
            configItem: ConfigItem,
            isFirstItem: Boolean,
            isLastItem: Boolean,
            spanCount: Int,
            onAddConfigItem: (Long) -> Unit,
            onEditConfigItem: (ConfigItem) -> Unit,
            onDeleteConfigItem: (ConfigItem) -> Unit,
            onDeleteModel: (Long) -> Unit
        ) {
            // 型号名称只在第一行显示
            if (isFirstItem) {
                tvModelName.text = model.model.name
                tvModelName.visibility = View.VISIBLE
            } else {
                tvModelName.visibility = View.INVISIBLE
            }


            tvGearRatio.text = configItem.gearRatio.toString()
            tvPosition.text = configItem.position
            tvBladeCount.text = configItem.bladeCount.toString()
            // 隐藏点动次数显示
            tvJogCount.visibility = android.view.View.GONE

            // 重置所有按钮状态，确保正确显示
            btnAddConfigItem.visibility = View.GONE
            btnEdit.visibility = View.GONE
            btnDelete.visibility = View.GONE
            
            // "添加配置项"按钮只在最后一行显示
            if (isLastItem) {
                btnAddConfigItem.visibility = View.VISIBLE
                btnAddConfigItem.setOnClickListener { onAddConfigItem(model.model.id) }
            }

            // 编辑按钮：所有配置项都可以编辑
            btnEdit.visibility = View.VISIBLE
            btnEdit.setOnClickListener {
                onEditConfigItem(configItem)
            }

            // 删除按钮：所有配置项都可以删除
            btnDelete.visibility = View.VISIBLE
            btnDelete.setOnClickListener {
                if (spanCount == 1) {
                    // 如果只有一个配置项，删除型号
                    onDeleteModel(model.model.id)
                } else {
                    // 否则只删除配置项
                    onDeleteConfigItem(configItem)
                }
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_FIRST_ITEM = 0
        private const val VIEW_TYPE_NORMAL_ITEM = 1
    }
}
