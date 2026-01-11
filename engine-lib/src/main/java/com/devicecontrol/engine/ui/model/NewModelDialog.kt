package com.devicecontrol.engine.ui.model

import android.app.Dialog
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.devicecontrol.engine.R
import com.devicecontrol.engine.databinding.DialogNewModelBinding

class NewModelDialog(
    private val onConfirm: (String) -> Unit
) : DialogFragment() {

    private var _binding: DialogNewModelBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogNewModelBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
        
        // 设置对话框窗口属性，优化横屏显示
        dialog.window?.let { window ->
            val params = window.attributes
            params.width = (resources.displayMetrics.widthPixels * 0.6).toInt()
            params.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
        
        // 配置输入法行为
        binding.etModelName.imeOptions = EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_EXTRACT_UI
        binding.etModelName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // 隐藏输入法
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(binding.etModelName.windowToken, 0)
                // 触发确认按钮
                binding.btnConfirm.performClick()
                true
            } else {
                false
            }
        }

        binding.btnConfirm.setOnClickListener {
            val modelName = binding.etModelName.text.toString().trim()
            if (modelName.isBlank()) {
                Toast.makeText(context, "型号名称不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            onConfirm(modelName)
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
