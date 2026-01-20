package com.devicecontrol.engine.ui.control

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devicecontrol.engine.R
import com.devicecontrol.engine.databinding.ItemTaskSwitchBinding

class TaskSwitchAdapter(
    private val taskItems: List<com.devicecontrol.engine.viewmodel.TaskControlViewModel.TaskItem>,
    private val currentTaskIndex: Int,
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<TaskSwitchAdapter.ViewHolder>() {

    private var selectedIndex: Int = currentTaskIndex
    
    fun getSelectedIndex(): Int = selectedIndex

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTaskSwitchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val taskItem = taskItems[position]
        holder.bind(taskItem, taskItem.index == selectedIndex)
    }

    override fun getItemCount(): Int = taskItems.size

    inner class ViewHolder(
        private val binding: ItemTaskSwitchBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(taskItem: com.devicecontrol.engine.viewmodel.TaskControlViewModel.TaskItem, isSelected: Boolean) {
            binding.tvTaskName.text = taskItem.displayName
            binding.rbTask.isChecked = isSelected

            binding.root.setOnClickListener {
                // 如果点击的是已选中的任务，不需要响应
                if (isSelected) {
                    return@setOnClickListener
                }
                // 更新选中状态
                val oldSelected = selectedIndex
                selectedIndex = taskItem.index
                // 通知所有item更新显示
                notifyDataSetChanged()
                // 通知选中状态变化
                onSelectionChanged(taskItem.index)
            }
        }
    }
}
