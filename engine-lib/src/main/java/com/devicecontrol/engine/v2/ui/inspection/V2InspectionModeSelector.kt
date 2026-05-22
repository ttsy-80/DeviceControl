package com.devicecontrol.engine.v2.ui.inspection

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.viewmodel.V2UiOperationMode

/**
 * P7 检测页「自动/手动模式」选择器（深蓝触发钮 + 顶部白梯形尖角 + 白底列表框 + 双色药丸项）。
 */
class V2InspectionModeSelector(
    private val activity: AppCompatActivity,
    private val trigger: TextView,
) {
    private var popup: PopupWindow? = null
    private var selectedMode: V2UiOperationMode = V2UiOperationMode.AUTO
    private var suppressCallback = false

    var onModeSelected: ((V2UiOperationMode) -> Unit)? = null

    fun setup(initial: V2UiOperationMode = V2UiOperationMode.AUTO) {
        selectedMode = initial
        renderTrigger(expanded = false)
        trigger.setOnClickListener {
            if (popup?.isShowing == true) dismiss() else show()
        }
    }

    fun setModeSilently(mode: V2UiOperationMode) {
        suppressCallback = true
        selectedMode = mode
        renderTrigger(expanded = popup?.isShowing == true)
        suppressCallback = false
    }

    fun dismiss() {
        popup?.dismiss()
        popup = null
        renderTrigger(expanded = false)
    }

    private fun show() {
        dismiss()
        val content = LayoutInflater.from(activity).inflate(R.layout.popup_v2_mode_spinner, null)
        content.findViewById<TextView>(R.id.btnModeOptionAuto).setOnClickListener {
            select(V2UiOperationMode.AUTO)
        }
        content.findViewById<TextView>(R.id.btnModeOptionManual).setOnClickListener {
            select(V2UiOperationMode.MANUAL)
        }
        val popupWidth = activity.resources.getDimensionPixelSize(R.dimen.v2_mode_spinner_popup_width)
        val widthSpec = View.MeasureSpec.makeMeasureSpec(popupWidth, View.MeasureSpec.EXACTLY)
        content.measure(
            widthSpec,
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        )
        val window = PopupWindow(
            content,
            popupWidth,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            true,
        ).apply {
            setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            elevation = activity.resources.getDimension(R.dimen.v2_p7_control_btn_elevation)
            isOutsideTouchable = true
            setOnDismissListener { renderTrigger(expanded = false) }
        }
        popup = window
        renderTrigger(expanded = true)
        trigger.post {
            window.showAsDropDown(trigger, popupOffsetX(trigger.width, popupWidth), 0)
        }
    }

    private fun select(mode: V2UiOperationMode) {
        if (selectedMode == mode) {
            dismiss()
            return
        }
        selectedMode = mode
        renderTrigger(expanded = false)
        dismiss()
        if (!suppressCallback) {
            onModeSelected?.invoke(mode)
        }
    }

    private fun renderTrigger(expanded: Boolean) {
        trigger.text = when (selectedMode) {
            V2UiOperationMode.AUTO -> activity.getString(R.string.v2_auto_mode)
            V2UiOperationMode.MANUAL -> activity.getString(R.string.v2_manual_mode)
        }
        val arrowRes = if (expanded) R.drawable.ic_v2_spinner_arrow_up else R.drawable.ic_v2_spinner_arrow
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
            trigger,
            null,
            null,
            ContextCompat.getDrawable(activity, arrowRes),
            null,
        )
        trigger.compoundDrawablePadding = dp(4)
    }

    private fun dp(value: Int): Int =
        (value * activity.resources.displayMetrics.density).toInt()

    /** 弹层水平居中于触发钮；垂直 y=0 使梯形紧贴触发钮底边。 */
    private fun popupOffsetX(triggerWidth: Int, popupWidth: Int): Int =
        ((triggerWidth - popupWidth) / 2f).toInt()
}
