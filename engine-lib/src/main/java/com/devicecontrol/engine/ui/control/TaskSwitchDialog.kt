package com.devicecontrol.engine.ui.control

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.devicecontrol.engine.R
import com.devicecontrol.engine.databinding.DialogTaskSwitchBinding

class TaskSwitchDialog(
    private val taskItems: List<com.devicecontrol.engine.viewmodel.TaskControlViewModel.TaskItem>,
    private val currentTaskIndex: Int,
    private val onTaskSelected: (Int) -> Unit
) : DialogFragment() {

    private var _binding: DialogTaskSwitchBinding? = null
    private val binding get() = _binding!!
    private var selectedIndex: Int = currentTaskIndex

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogTaskSwitchBinding.inflate(layoutInflater)

        val adapter = TaskSwitchAdapter(taskItems, currentTaskIndex) { index ->
            selectedIndex = index
        }
        binding.rvTaskList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTaskList.adapter = adapter

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
        
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        
        // 设置对话框窗口属性，优化横屏显示
        dialog.window?.let { window ->
            val params = window.attributes
            params.width = (resources.displayMetrics.widthPixels * 0.8).toInt()
            params.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
        
        binding.btnConfirm.setOnClickListener {
            // 使用adapter获取当前选中的索引
            val finalSelectedIndex = adapter.getSelectedIndex()
            onTaskSelected(finalSelectedIndex)
            dialog.dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
