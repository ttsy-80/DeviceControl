package com.devicecontrol.engine.ui.model

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.devicecontrol.engine.R
import com.devicecontrol.engine.databinding.DialogConfigItemBinding

class ConfigItemDialog(
    private val modelId: Long? = null,
    private val existingConfigItem: com.devicecontrol.engine.data.model.ConfigItem? = null,
    private val firstGearRatio: Double? = null, // 第一个配置项的变速比（用于添加模式）
    private val onConfirm: (Double, String, Int, Int) -> Unit
) : DialogFragment() {

    private var _binding: DialogConfigItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogConfigItemBinding.inflate(layoutInflater)

        val isEditMode = existingConfigItem != null
        val title = if (isEditMode) {
            getString(R.string.edit_config_item)
        } else {
            getString(R.string.add_config_item)
        }
        binding.tvDialogTitle.text = title
        
        // 如果是编辑模式，预填充数据
        if (isEditMode && existingConfigItem != null) {
            binding.etGearRatio.setText(existingConfigItem.gearRatio.toString())
            binding.etPosition.setText(existingConfigItem.position)
            binding.etBladeCount.setText(existingConfigItem.bladeCount.toString())
            binding.etJogCount.setText(existingConfigItem.jogCount.toString())
        } else if (!isEditMode && firstGearRatio != null) {
            // 添加模式：如果提供了第一个配置项的变速比，自动填充并设为只读
            binding.etGearRatio.setText(firstGearRatio.toString())
            // 禁用整个 TextInputLayout，这样视觉效果更好
            binding.tilGearRatio.isEnabled = false
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
        
        // 确保对话框显示自定义视图，不显示默认按钮栏
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        
        // 设置对话框窗口属性，优化横屏显示
        dialog.window?.let { window ->
            val params = window.attributes
            params.width = (resources.displayMetrics.widthPixels * 0.8).toInt()
            params.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
        
        // 配置输入法行为
        setupImeActions()
        
        // 确保确认按钮可见且可点击
        binding.btnConfirm.visibility = android.view.View.VISIBLE
        binding.btnConfirm.isEnabled = true

        binding.btnConfirm.setOnClickListener {
            val gearRatioText = binding.etGearRatio.text.toString()
            val position = binding.etPosition.text.toString()
            val bladeCountText = binding.etBladeCount.text.toString()
            val jogCountText = binding.etJogCount.text.toString()

            if (gearRatioText.isBlank() || position.isBlank() || 
                bladeCountText.isBlank() || jogCountText.isBlank()) {
                Toast.makeText(context, "请填写所有字段", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val gearRatio = gearRatioText.toDouble()
                val bladeCount = bladeCountText.toInt()
                val jogCount = jogCountText.toInt()

                if (gearRatio <= 0 || bladeCount <= 0 || jogCount <= 0) {
                    Toast.makeText(context, "请输入有效的数值", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                onConfirm(gearRatio, position, bladeCount, jogCount)
                dialog.dismiss()
            } catch (e: NumberFormatException) {
                Toast.makeText(context, "请输入有效的数值", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }
    
    private fun setupImeActions() {
        // 设置输入法选项（变速比如果是只读的，不需要设置）
        if (binding.etGearRatio.isEnabled) {
            binding.etGearRatio.imeOptions = EditorInfo.IME_ACTION_NEXT or EditorInfo.IME_FLAG_NO_EXTRACT_UI
            // 设置输入法的下一步/完成行为
            binding.etGearRatio.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    binding.etPosition.requestFocus()
                    true
                } else {
                    false
                }
            }
        }
        
        binding.etPosition.imeOptions = EditorInfo.IME_ACTION_NEXT or EditorInfo.IME_FLAG_NO_EXTRACT_UI
        binding.etBladeCount.imeOptions = EditorInfo.IME_ACTION_NEXT or EditorInfo.IME_FLAG_NO_EXTRACT_UI
        binding.etJogCount.imeOptions = EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_EXTRACT_UI
        
        binding.etPosition.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.etBladeCount.requestFocus()
                true
            } else {
                false
            }
        }
        
        binding.etBladeCount.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.etJogCount.requestFocus()
                true
            } else {
                false
            }
        }
        
        binding.etJogCount.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // 隐藏输入法
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(binding.etJogCount.windowToken, 0)
                // 触发确认按钮
                binding.btnConfirm.performClick()
                true
            } else {
                false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
