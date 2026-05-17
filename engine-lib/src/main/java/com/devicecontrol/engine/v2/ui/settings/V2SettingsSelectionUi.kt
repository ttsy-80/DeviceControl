package com.devicecontrol.engine.v2.ui.settings

import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.devicecontrol.engine.R

/**
 * 设置页列表项选中态：橙色胶囊 + 白字（对齐 PDF P2/P3）。
 */
object V2SettingsSelectionUi {

    fun applyListSelection(context: Context, views: List<TextView>, selectedIndex: Int) {
        val padH = context.resources.getDimensionPixelSize(R.dimen.v2_settings_capsule_pad_h)
        val padV = context.resources.getDimensionPixelSize(R.dimen.v2_settings_capsule_pad_v)
        views.forEachIndexed { index, tv ->
            if (index == selectedIndex) {
                tv.setBackgroundResource(R.drawable.bg_v2_settings_selection_capsule)
                tv.setTextColor(ContextCompat.getColor(context, R.color.v2_on_primary))
                tv.setPadding(padH, padV, padH, padV)
            } else {
                tv.background = null
                tv.setTextColor(ContextCompat.getColor(context, R.color.v2_text_primary))
                tv.setPadding(0, padV, 0, padV)
            }
        }
    }
}
