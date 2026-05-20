package com.devicecontrol.engine.v2.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.devicecontrol.engine.R

/**
 * 「引擎信息」标签：可配置背景色、左上圆角、右侧向内斜切（默认 45°），左下角直角。
 */
class V2EngineInfoTabTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var shape: V2EngineInfoTabShape = V2EngineInfoTabShape.default(context)
    private val tabDrawable = V2EngineInfoTabBackgroundDrawable(shape)

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.V2EngineInfoTabTextView)
        val bg = a.getColor(
            R.styleable.V2EngineInfoTabTextView_v2EngineInfoTabBackgroundColor,
            shape.backgroundColor,
        )
        val topLeftRadius = a.getDimension(
            R.styleable.V2EngineInfoTabTextView_v2EngineInfoTabTopLeftRadius,
            shape.topLeftRadiusPx,
        )
        val rightAngle = a.getFloat(
            R.styleable.V2EngineInfoTabTextView_v2EngineInfoTabRightEdgeAngle,
            V2EngineInfoTabShape.DEFAULT_RIGHT_EDGE_ANGLE_DEG,
        )
        a.recycle()
        applyTabShape(V2EngineInfoTabShape(bg, topLeftRadius, rightAngle))
    }

    fun applyTabShape(newShape: V2EngineInfoTabShape) {
        shape = newShape
        tabDrawable.setShape(newShape)
        background = tabDrawable
        invalidate()
    }

    fun applyTabShape(
        backgroundColor: Int = shape.backgroundColor,
        topLeftRadiusPx: Float = shape.topLeftRadiusPx,
        rightEdgeAngleDeg: Float = shape.rightEdgeAngleDeg,
    ) {
        applyTabShape(
            V2EngineInfoTabShape(
                backgroundColor = backgroundColor,
                topLeftRadiusPx = topLeftRadiusPx,
                rightEdgeAngleDeg = rightEdgeAngleDeg,
            ),
        )
    }

    fun currentTabShape(): V2EngineInfoTabShape = shape
}
