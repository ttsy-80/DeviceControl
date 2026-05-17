package com.devicecontrol.engine.v2.ui.dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.log.V2Log
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * P18 风格错误弹窗。
 */
object V2ErrorDialog {

    private const val TAG = "ErrorDialog"

    fun show(
        context: Context,
        message: String,
        onClose: (() -> Unit)? = null,
        onBackHome: (() -> Unit)? = null,
    ): Dialog {
        V2Log.w(TAG, "show error: $message")
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_v2_error, null)
        view.findViewById<android.widget.TextView>(R.id.tvErrorMessage).text = message

        val dialog = MaterialAlertDialogBuilder(context)
            .setView(view)
            .setCancelable(true)
            .create()

        view.findViewById<android.widget.TextView>(R.id.btnCloseHint).setOnClickListener {
            V2Log.i(TAG, "close hint")
            onClose?.invoke()
            dialog.dismiss()
        }
        view.findViewById<android.widget.TextView>(R.id.btnErrorBackHome).setOnClickListener {
            V2Log.i(TAG, "back home from error")
            onBackHome?.invoke()
            dialog.dismiss()
        }
        dialog.show()
        return dialog
    }
}
