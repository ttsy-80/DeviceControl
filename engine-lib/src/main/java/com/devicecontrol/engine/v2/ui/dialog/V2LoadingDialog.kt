package com.devicecontrol.engine.v2.ui.dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.devicecontrol.engine.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/** V2 通用 loading 弹窗（不可取消）。 */
object V2LoadingDialog {

    private var dialog: Dialog? = null

    fun show(context: Context, message: String) {
        dismiss()
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_v2_loading, null)
        view.findViewById<TextView>(R.id.tvLoadingMessage).text = message
        dialog = MaterialAlertDialogBuilder(context)
            .setView(view)
            .setCancelable(false)
            .create()
        dialog?.show()
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }
}
