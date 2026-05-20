package com.devicecontrol.engine.v2.ui.widget

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

/** 绘制「引擎信息」标签 [V2EngineInfoTabShape] 背景。 */
class V2EngineInfoTabBackgroundDrawable(
    private var shape: V2EngineInfoTabShape,
) : Drawable() {

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = shape.backgroundColor
    }
    private val path = Path()

    fun setShape(newShape: V2EngineInfoTabShape) {
        shape = newShape
        fillPaint.color = shape.backgroundColor
        rebuildPath()
        invalidateSelf()
    }

    override fun onBoundsChange(bounds: android.graphics.Rect) {
        super.onBoundsChange(bounds)
        rebuildPath()
    }

    private fun rebuildPath() {
        val b = bounds
        if (b.isEmpty) {
            path.reset()
            return
        }
        V2EngineInfoTabPathBuilder.build(
            width = b.width().toFloat(),
            height = b.height().toFloat(),
            topLeftRadiusPx = shape.topLeftRadiusPx,
            rightEdgeAngleDeg = shape.rightEdgeAngleDeg,
            outPath = path,
        )
    }

    override fun draw(canvas: Canvas) {
        if (path.isEmpty) return
        fillPaint.color = shape.backgroundColor
        canvas.save()
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
        canvas.drawPath(path, fillPaint)
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) {
        fillPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        fillPaint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
