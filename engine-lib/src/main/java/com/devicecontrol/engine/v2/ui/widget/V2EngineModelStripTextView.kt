package com.devicecontrol.engine.v2.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.devicecontrol.engine.R

/**
 * 引擎信息右侧型号条：渐变背景、左侧外斜切（默认 45°）、右侧直角。
 */
class V2EngineModelStripTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var shape: V2EngineModelStripShape = V2EngineModelStripShape.default(context)
    private val stripDrawable = V2EngineModelStripBackgroundDrawable(shape)

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.V2EngineModelStripTextView)
        val startColor = a.getColor(
            R.styleable.V2EngineModelStripTextView_v2EngineStripGradientStartColor,
            shape.gradientStartColor,
        )
        val endColor = a.getColor(
            R.styleable.V2EngineModelStripTextView_v2EngineStripGradientEndColor,
            shape.gradientEndColor,
        )
        val gradientAngle = a.getFloat(
            R.styleable.V2EngineModelStripTextView_v2EngineStripGradientAngle,
            V2EngineModelStripShape.DEFAULT_GRADIENT_ANGLE_DEG,
        )
        val leftAngle = a.getFloat(
            R.styleable.V2EngineModelStripTextView_v2EngineStripLeftEdgeAngle,
            V2EngineModelStripShape.DEFAULT_LEFT_EDGE_ANGLE_DEG,
        )
        a.recycle()
        applyStripShape(
            V2EngineModelStripShape(
                gradientStartColor = startColor,
                gradientEndColor = endColor,
                gradientAngleDeg = gradientAngle,
                leftEdgeAngleDeg = leftAngle,
            ),
        )
    }

    fun applyStripShape(newShape: V2EngineModelStripShape) {
        shape = newShape
        stripDrawable.setShape(newShape)
        background = stripDrawable
        invalidate()
    }

    fun currentStripShape(): V2EngineModelStripShape = shape
}
