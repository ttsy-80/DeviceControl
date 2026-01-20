package com.devicecontrol.engine.ui.control

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devicecontrol.engine.R
import com.devicecontrol.engine.data.model.TaskRecord
import com.devicecontrol.engine.databinding.ItemTaskRecordBinding

class TaskRecordAdapter(
    private val onRecordClick: (TaskRecord) -> Unit
) : RecyclerView.Adapter<TaskRecordAdapter.ViewHolder>() {

    private var records: List<TaskRecord> = emptyList()
    private var currentBladeCount: Int = 0

    fun submitList(newRecords: List<TaskRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }
    
    fun setBladeCount(bladeCount: Int) {
        currentBladeCount = bladeCount
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTaskRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(records[position])
    }

    override fun getItemCount(): Int = records.size

    inner class ViewHolder(
        private val binding: ItemTaskRecordBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(record: TaskRecord) {
            // 显示格式：记录X-第Y/总数叶片
            binding.tvRecordNumber.text = "记录${record.recordNumber}-"
            binding.tvBladeInfo.text = "第${record.bladeNumber}/${this@TaskRecordAdapter.currentBladeCount}叶片"
            
            // 整个item可点击查看详情
            binding.root.setOnClickListener {
                onRecordClick(record)
            }
        }
    }
}

