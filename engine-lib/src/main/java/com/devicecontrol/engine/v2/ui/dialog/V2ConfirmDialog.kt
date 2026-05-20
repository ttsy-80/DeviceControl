package com.devicecontrol.engine.v2.ui.dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.log.V2Log
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * V2 风格确认弹窗（对齐 v1 删除二次确认逻辑，样式同 P18 错误弹窗结构）。
 */
object V2ConfirmDialog {

    private const val TAG = "ConfirmDialog"

    fun show(
        context: Context,
        title: String,
        message: String,
        confirmText: String = context.getString(R.string.v2_delete),
        cancelText: String = context.getString(R.string.v2_cancel),
        dangerConfirm: Boolean = true,
        onConfirm: () -> Unit,
        onCancel: (() -> Unit)? = null,
    ): Dialog {
        V2Log.i(TAG, "show confirm: title=$title message=$message")
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_v2_confirm, null)
        view.findViewById<TextView>(R.id.tvConfirmBanner).text = title
        view.findViewById<TextView>(R.id.tvConfirmMessage).text = message

        val btnOk = view.findViewById<TextView>(R.id.btnConfirmOk)
        val btnCancel = view.findViewById<TextView>(R.id.btnConfirmCancel)
        btnOk.text = confirmText
        btnCancel.text = cancelText
        btnOk.setBackgroundResource(
            if (dangerConfirm) R.drawable.bg_v2_pill_red else R.drawable.bg_v2_pill_orange,
        )

        val dialog = MaterialAlertDialogBuilder(context)
            .setView(view)
            .setCancelable(true)
            .create()

        btnCancel.setOnClickListener {
            V2Log.i(TAG, "confirm cancelled")
            onCancel?.invoke()
            dialog.dismiss()
        }
        btnOk.setOnClickListener {
            V2Log.i(TAG, "confirm accepted")
            dialog.dismiss()
            onConfirm()
        }
        dialog.setOnCancelListener { onCancel?.invoke() }
        dialog.show()
        return dialog
    }
}
