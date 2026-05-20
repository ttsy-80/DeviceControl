package com.devicecontrol.engine.v2.ui.widget

import android.content.Context
import android.graphics.Path
import androidx.core.content.ContextCompat
import com.devicecontrol.engine.R
import kotlin.math.tan

/**
 * 引擎信息右侧型号条：左侧外斜切（略不规则）、右侧直角、渐变填充。
 */
data class V2EngineModelStripShape(
    val gradientStartColor: Int,
    val gradientEndColor: Int,
    /** 线性渐变角度（度），0=左→右 */
    val gradientAngleDeg: Float,
    /** 左侧外斜切与竖直线夹角（度）。45° 时顶部内缩距离约等于高度。 */
    val leftEdgeAngleDeg: Float,
) {
    companion object {
        const val DEFAULT_LEFT_EDGE_ANGLE_DEG = 45f
        const val DEFAULT_GRADIENT_ANGLE_DEG = 0f

        fun default(context: Context): V2EngineModelStripShape =
            V2EngineModelStripShape(
                gradientStartColor = ContextCompat.getColor(context, R.color.v2_peach),
                gradientEndColor = ContextCompat.getColor(context, R.color.v2_surface),
                gradientAngleDeg = DEFAULT_GRADIENT_ANGLE_DEG,
                leftEdgeAngleDeg = DEFAULT_LEFT_EDGE_ANGLE_DEG,
            )
    }
}

object V2EngineModelStripPathBuilder {

    fun build(
        width: Float,
        height: Float,
        leftEdgeAngleDeg: Float,
        outPath: Path = Path(),
    ): Path {
        outPath.reset()
        if (width <= 0f || height <= 0f) return outPath

        val angleDeg = leftEdgeAngleDeg.coerceIn(0f, 89.9f)
        val leftTopX = if (angleDeg <= 0f) {
            0f
        } else {
            (height * tan(Math.toRadians(angleDeg.toDouble()))).toFloat().coerceIn(0f, width)
        }

        // 梯形：左侧外斜线，右侧竖直（无圆角）
        outPath.moveTo(0f, height)
        outPath.lineTo(width, height)
        outPath.lineTo(width, 0f)
        outPath.lineTo(leftTopX, 0f)
        outPath.close()
        return outPath
    }
}
