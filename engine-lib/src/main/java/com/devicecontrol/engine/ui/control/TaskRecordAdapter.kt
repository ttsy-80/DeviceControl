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

    fun submitList(newRecords: List<TaskRecord>) {
        records = newRecords
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
            // 显示记录序号
            binding.tvRecordNumber.text = binding.root.context.getString(
                R.string.record_number,
                record.recordNumber
            )
            
            // 显示叶片信息
            binding.tvBladeInfo.text = binding.root.context.getString(
                R.string.blade_number_label,
                record.bladeNumber
            )
            
            // 整个item可点击查看详情
            binding.root.setOnClickListener {
                onRecordClick(record)
            }
        }
    }
}

