package com.devicecontrol.engine.v2.ui.widget

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Shader
import android.graphics.drawable.Drawable
import kotlin.math.cos
import kotlin.math.sin

/** 绘制型号条渐变 + 轮廓。 */
class V2EngineModelStripBackgroundDrawable(
    private var shape: V2EngineModelStripShape,
) : Drawable() {

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val path = Path()

    fun setShape(newShape: V2EngineModelStripShape) {
        shape = newShape
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
            fillPaint.shader = null
            return
        }
        V2EngineModelStripPathBuilder.build(
            width = b.width().toFloat(),
            height = b.height().toFloat(),
            leftEdgeAngleDeg = shape.leftEdgeAngleDeg,
            outPath = path,
        )
        fillPaint.shader = createGradientShader(b.width().toFloat(), b.height().toFloat())
    }

    /**
     * 与 [android.graphics.drawable.GradientDrawable] 一致：按视图宽高映射渐变，
     * 避免斜切轮廓内中心放射导致右侧发虚、变形。
     */
    private fun createGradientShader(width: Float, height: Float): Shader {
        val angleDeg = ((shape.gradientAngleDeg % 360f) + 360f) % 360f
        val rad = Math.toRadians(angleDeg.toDouble())
        val y = height / 2f
        val dx = cos(rad).toFloat()
        val dy = sin(rad).toFloat()
        val halfLen = (kotlin.math.abs(width * dx) + kotlin.math.abs(height * dy)) / 2f
        val cx = width / 2f
        val cy = height / 2f
        return LinearGradient(
            cx - dx * halfLen,
            cy - dy * halfLen,
            cx + dx * halfLen,
            cy + dy * halfLen,
            shape.gradientStartColor,
            shape.gradientEndColor,
            Shader.TileMode.CLAMP,
        )
    }

    override fun draw(canvas: Canvas) {
        if (path.isEmpty) return
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
