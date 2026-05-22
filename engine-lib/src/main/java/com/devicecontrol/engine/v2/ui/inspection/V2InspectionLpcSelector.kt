package com.devicecontrol.engine.v2.ui.inspection

import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.log.V2Log

/**
 * P7 检测页 LPC 位置选择（对齐稿面：深蓝触发钮 + 底部白条衔接 + 取消/选择 + 表头 + 高亮行）。
 */
class V2InspectionLpcSelector(
    private val activity: AppCompatActivity,
    private val trigger: TextView,
) {
    private var popup: PopupWindow? = null
    private var positions: List<String> = emptyList()
    private var bladeCounts: List<Int> = emptyList()
    private var selectedIndex: Int = 0
    private var pickedIndex: Int = 0
    private var suppressCallback = false

    var onIndexSelected: ((Int) -> Unit)? = null

    fun setup(positions: List<String>, bladeCounts: List<Int>, selectedIndex: Int) {
        updateData(positions, bladeCounts, selectedIndex)
        bindTriggerClick()
    }

    fun updateData(positions: List<String>, bladeCounts: List<Int>, selectedIndex: Int) {
        this.positions = positions
        this.bladeCounts = bladeCounts
        if (positions.isEmpty()) {
//            selectedIndex = 0
            pickedIndex = 0
            trigger.isEnabled = false
            trigger.isClickable = false
            renderTrigger(expanded = false)
            return
        }
        this.selectedIndex = selectedIndex.coerceIn(0, positions.lastIndex)
        pickedIndex = this.selectedIndex
        trigger.isEnabled = true
        trigger.isClickable = true
        renderTrigger(expanded = popup?.isShowing == true)
        bindTriggerClick()
    }

    private fun bindTriggerClick() {
        trigger.setOnClickListener {
            if (positions.isEmpty()) return@setOnClickListener
            if (popup?.isShowing == true) dismiss() else show()
        }
    }

    fun setIndexSilently(index: Int) {
        suppressCallback = true
        selectedIndex = index.coerceIn(0, (positions.size - 1).coerceAtLeast(0))
        pickedIndex = selectedIndex
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
        pickedIndex = selectedIndex
        val content = LayoutInflater.from(activity).inflate(R.layout.popup_v2_lpc_spinner, null)
        val rv = content.findViewById<RecyclerView>(R.id.rvLpcList)
        val adapter = LpcPickAdapter()
        rv.layoutManager = LinearLayoutManager(activity)
        rv.adapter = adapter
        if (pickedIndex in positions.indices) {
            rv.scrollToPosition(pickedIndex)
        }

        content.findViewById<View>(R.id.btnLpcCancel).setOnClickListener { dismiss() }
        content.findViewById<View>(R.id.btnLpcSelect).setOnClickListener {
            applySelection(pickedIndex)
        }

        val popupWidth = activity.resources.getDimensionPixelSize(R.dimen.v2_lpc_spinner_popup_width)
        val window = PopupWindow(
            content,
            popupWidth,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true,
        ).apply {
            setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            elevation = activity.resources.getDimension(R.dimen.v2_p7_control_btn_elevation)
            isOutsideTouchable = true
            isFocusable = true
            isTouchable = true
            setOnDismissListener { renderTrigger(expanded = false) }
        }
        popup = window
        renderTrigger(expanded = true)
        if (trigger.isShown && trigger.windowToken != null) {
            showPopupAligned(window, popupWidth)
        } else {
            trigger.post { showPopupAligned(window, popupWidth) }
        }
    }

    private fun showPopupAligned(window: PopupWindow, popupWidth: Int) {
        if (!trigger.isShown || trigger.windowToken == null) return
        val offsetX = ((trigger.width - popupWidth) / 2f).toInt()
        window.showAsDropDown(trigger, offsetX, dp(2))
    }

    private fun applySelection(index: Int) {
        if (index !in positions.indices) {
            dismiss()
            return
        }
        val changed = index != selectedIndex
        selectedIndex = index
        pickedIndex = index
        renderTrigger(expanded = false)
        dismiss()
        if (changed && !suppressCallback) {
            V2Log.i(TAG, "lpc selected index=$index")
            onIndexSelected?.invoke(index)
        }
    }

    private fun renderTrigger(expanded: Boolean) {
        trigger.background = ContextCompat.getDrawable(
            activity,
            if (expanded) R.drawable.bg_v2_lpc_spinner_trigger_open else R.drawable.bg_v2_lpc_spinner_trigger,
        )
        trigger.text = if (selectedIndex in positions.indices) {
            displayPosition(positions[selectedIndex])
        } else {
            ""
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

    /** 直接使用配置项 [ConfigItem.position] 原文，不再强制格式化为 LPC n。 */
    private fun displayPosition(position: String): String = position.trim()

    private fun dp(value: Int): Int =
        (value * activity.resources.displayMetrics.density).toInt()

    private inner class LpcPickAdapter : RecyclerView.Adapter<LpcPickAdapter.VH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val row = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_v2_lpc_pick_row, parent, false)
            return VH(row)
        }

        override fun getItemCount(): Int = positions.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val selected = position == pickedIndex
            holder.tvPosition.text = displayPosition(positions[position])
            holder.tvBlade.text = bladeCounts.getOrNull(position)?.toString().orEmpty()
            holder.itemView.setBackgroundResource(
                if (selected) R.drawable.bg_v2_lpc_row_selected else android.R.color.transparent,
            )
            val textColor = ContextCompat.getColor(
                activity,
                if (selected) R.color.v2_text_primary else R.color.v2_text_secondary,
            )
            holder.tvPosition.setTextColor(textColor)
            holder.tvBlade.setTextColor(textColor)
            holder.tvPosition.setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
            holder.tvBlade.setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
            holder.itemView.setOnClickListener {
                pickedIndex = position
                notifyDataSetChanged()
            }
        }

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvPosition: TextView = view.findViewById(R.id.tvLpcPosition)
            val tvBlade: TextView = view.findViewById(R.id.tvLpcBlade)
        }
    }

    companion object {
        private const val TAG = "InspectionLpcSelector"
    }
}
