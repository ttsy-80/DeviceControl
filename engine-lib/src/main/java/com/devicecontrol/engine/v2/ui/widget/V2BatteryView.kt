package com.devicecontrol.engine.v2.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.devicecontrol.engine.R
import kotlin.math.max

/**
 * 顶栏电量：白描边电池外形 + 内部填充比例（对齐 PDF 顶栏）。
 */
class V2BatteryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    /** 0～100 */
    var level: Int = 100
        set(value) {
            val next = value.coerceIn(0, 100)
            if (field != next) {
                field = next
                invalidate()
            }
        }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.v2_on_primary)
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.v2_on_primary)
    }
    private val bodyRect = RectF()
    private val fillRect = RectF()
    private val tipRect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val stroke = max(1.5f, h * 0.09f)
        strokePaint.strokeWidth = stroke
        val inset = stroke * 0.5f + 1f
        val tipW = h * 0.28f
        val gap = h * 0.12f
        val bodyW = w - tipW - gap
        val corner = h * 0.22f

        bodyRect.set(inset, inset, inset + bodyW, h - inset)
        tipRect.set(bodyRect.right + gap, h * 0.32f, w - inset, h * 0.68f)

        canvas.drawRoundRect(bodyRect, corner, corner, strokePaint)
        canvas.drawRoundRect(tipRect, tipW * 0.2f, tipW * 0.2f, fillPaint)

        val innerPad = stroke + 2f
        val innerL = bodyRect.left + innerPad
        val innerR = bodyRect.right - innerPad
        val innerT = bodyRect.top + innerPad
        val innerB = bodyRect.bottom - innerPad
        val innerW = max(0f, innerR - innerL)
        val fillW = innerW * (level / 100f)
        if (fillW > 0f) {
            fillRect.set(innerL, innerT, innerL + fillW, innerB)
            canvas.drawRoundRect(fillRect, corner * 0.6f, corner * 0.6f, fillPaint)
        }
    }
}
