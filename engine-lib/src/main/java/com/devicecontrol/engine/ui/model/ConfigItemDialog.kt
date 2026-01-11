package com.devicecontrol.engine.ui.model

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.devicecontrol.engine.R
import com.devicecontrol.engine.databinding.DialogConfigItemBinding

class ConfigItemDialog(
    private val modelId: Long? = null,
    private val onConfirm: (Double, String, Int, Int) -> Unit
) : DialogFragment() {

    private var _binding: DialogConfigItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogConfigItemBinding.inflate(layoutInflater)

        val title = if (modelId == null) {
            getString(R.string.add_config_item)
        } else {
            getString(R.string.edit_config_item)
        }
        binding.tvDialogTitle.text = title

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
