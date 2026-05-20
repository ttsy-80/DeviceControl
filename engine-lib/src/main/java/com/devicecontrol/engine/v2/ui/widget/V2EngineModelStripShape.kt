package com.devicecontrol.engine.v2.ui.widget

import android.content.Context
import android.graphics.Path
import android.graphics.RectF
import androidx.core.content.ContextCompat
import com.devicecontrol.engine.R
import kotlin.math.min
import kotlin.math.tan

/**
 * 引擎信息右侧型号条：左侧自上至下外斜切、右侧圆角、渐变填充。
 */
data class V2EngineModelStripShape(
    val gradientStartColor: Int,
    val gradientEndColor: Int,
    /** 线性渐变角度（度），0=左→右 */
    val gradientAngleDeg: Float,
    /** 左侧外斜切与竖直线夹角（度）。45° 时顶部内缩距离约等于高度。 */
    val leftEdgeAngleDeg: Float,
    val topRightRadiusPx: Float,
    val bottomRightRadiusPx: Float,
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
                topRightRadiusPx = context.resources.getDimension(R.dimen.v2_inspection_panel_radius),
                bottomRightRadiusPx = context.resources.getDimension(R.dimen.v2_inspection_panel_radius),
            )
    }
}

object V2EngineModelStripPathBuilder {

    fun build(
        width: Float,
        height: Float,
        leftEdgeAngleDeg: Float,
        topRightRadiusPx: Float,
        bottomRightRadiusPx: Float,
        outPath: Path = Path(),
    ): Path {
        outPath.reset()
        if (width <= 0f || height <= 0f) return outPath

        val tr = topRightRadiusPx.coerceIn(0f, min(width, height) / 2f)
        val br = bottomRightRadiusPx.coerceIn(0f, min(width, height) / 2f)
        val angleDeg = leftEdgeAngleDeg.coerceIn(0f, 89.9f)
        val leftTopX = if (angleDeg <= 0f) {
            0f
        } else {
            (height * tan(Math.toRadians(angleDeg.toDouble()))).toFloat().coerceIn(0f, width)
        }

        // 左下直角 → 右侧圆角 → 顶部 → 左侧外斜线回左下
        outPath.moveTo(0f, height)
        if (br > 0f) {
            outPath.lineTo(width - br, height)
            outPath.arcTo(RectF(width - 2f * br, height - 2f * br, width, height), 0f, 90f, false)
        } else {
            outPath.lineTo(width, height)
        }
        if (tr > 0f) {
            outPath.lineTo(width, tr)
            outPath.arcTo(RectF(width - 2f * tr, 0f, width, 2f * tr), 90f, 90f, false)
        } else {
            outPath.lineTo(width, 0f)
        }
        val topEndX = leftTopX.coerceAtMost(width - tr)
        outPath.lineTo(topEndX, 0f)
        outPath.lineTo(0f, height)
        outPath.close()
        return outPath
    }
}
