package com.devicecontrol.engine.ui.control

import android.app.Dialog
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.devicecontrol.engine.R
import com.devicecontrol.engine.databinding.DialogSettingsBinding

class SettingsDialog(
    /** 当前每圈耗时折算为「分钟/圈」，用于输入框预填；≤0 时用默认 2 */
    private val currentInitialSpeedMinutesPerRev: Double,
    private val currentSpeedStep: Double,
    private val currentContinuousCycles: Int,
    private val currentJogInterval: Int,
    private val currentPlaybackSpeed: Double,
    private val onConfirm: (Double, Double, Int, Int, Double) -> Unit
) : DialogFragment() {

    companion object {
        private const val DEFAULT_INITIAL_MINUTES_PER_REV = 2.0
    }

    private var _binding: DialogSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogSettingsBinding.inflate(layoutInflater)

        // 预填充当前值（初始速度：分钟/圈；无有效值时默认 2）
        val initialMin = if (currentInitialSpeedMinutesPerRev > 0) {
            currentInitialSpeedMinutesPerRev
        } else {
            DEFAULT_INITIAL_MINUTES_PER_REV
        }
        binding.etInitialSpeed.setText(formatNumberForEdit(initialMin))
        binding.etSpeedStep.setText(currentSpeedStep.toString())
        binding.etContinuousCycles.setText(currentContinuousCycles.toString())
        binding.etJogInterval.setText(currentJogInterval.toString())
        binding.etPlaybackSpeed.setText(currentPlaybackSpeed.toString())

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
        
        // 配置输入法行为
        setupImeActions()
        
        // 确保确认按钮可见且可点击
        binding.btnConfirm.visibility = android.view.View.VISIBLE
        binding.btnConfirm.isEnabled = true

        binding.btnConfirm.setOnClickListener {
            val initialSpeedText = binding.etInitialSpeed.text.toString().trim()
            val speedStepText = binding.etSpeedStep.text.toString()
            val continuousCyclesText = binding.etContinuousCycles.text.toString()
            val jogIntervalText = binding.etJogInterval.text.toString()
            val playbackSpeedText = binding.etPlaybackSpeed.text.toString()

            if (initialSpeedText.isBlank() || speedStepText.isBlank() || continuousCyclesText.isBlank() || jogIntervalText.isBlank() || playbackSpeedText.isBlank()) {
                Toast.makeText(context, "请填写所有字段", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val initialMinutesPerRev = initialSpeedText.toDouble()
                val speedStep = speedStepText.toDouble()
                val continuousCycles = continuousCyclesText.toInt()
                val jogInterval = jogIntervalText.toInt()
                val playbackSpeed = playbackSpeedText.toDouble()

                if (initialMinutesPerRev <= 0 || speedStep <= 0 || continuousCycles <= 0 || jogInterval <= 0 || playbackSpeed <= 0) {
                    Toast.makeText(context, "请输入有效的数值（必须大于0）", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                onConfirm(initialMinutesPerRev, speedStep, continuousCycles, jogInterval, playbackSpeed)
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
    
    private fun formatNumberForEdit(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return DEFAULT_INITIAL_MINUTES_PER_REV.toString()
        val asLong = value.toLong()
        return if (value == asLong.toDouble()) asLong.toString() else value.toString()
    }

    private fun setupImeActions() {
        binding.etInitialSpeed.imeOptions = EditorInfo.IME_ACTION_NEXT or EditorInfo.IME_FLAG_NO_EXTRACT_UI
        binding.etSpeedStep.imeOptions = EditorInfo.IME_ACTION_NEXT or EditorInfo.IME_FLAG_NO_EXTRACT_UI
        binding.etContinuousCycles.imeOptions = EditorInfo.IME_ACTION_NEXT or EditorInfo.IME_FLAG_NO_EXTRACT_UI
        binding.etJogInterval.imeOptions = EditorInfo.IME_ACTION_NEXT or EditorInfo.IME_FLAG_NO_EXTRACT_UI
        binding.etPlaybackSpeed.imeOptions = EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_EXTRACT_UI

        binding.etInitialSpeed.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.etSpeedStep.requestFocus()
                true
            } else {
                false
            }
        }
        
        binding.etSpeedStep.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.etContinuousCycles.requestFocus()
                true
            } else {
                false
            }
        }
        
        binding.etContinuousCycles.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.etJogInterval.requestFocus()
                true
            } else {
                false
            }
        }
        
        binding.etJogInterval.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.etPlaybackSpeed.requestFocus()
                true
            } else {
                false
            }
        }
        
        binding.etPlaybackSpeed.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(binding.etPlaybackSpeed.windowToken, 0)
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
