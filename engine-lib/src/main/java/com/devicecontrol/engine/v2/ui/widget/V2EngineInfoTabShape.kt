package com.devicecontrol.engine.v2.ui.widget

import android.content.Context
import android.graphics.Path
import android.graphics.RectF
import androidx.core.content.ContextCompat
import com.devicecontrol.engine.R
import kotlin.math.min
import kotlin.math.tan

/**
 * 「引擎信息」标签背景轮廓：左下直角、左上圆角、右侧斜切（默认 45°）。
 */
data class V2EngineInfoTabShape(
    val backgroundColor: Int,
    val topLeftRadiusPx: Float,
    /** 右侧斜切与竖直线的夹角（度）。0° 为竖直右边；45° 时右下角向内收的距离等于高度。 */
    val rightEdgeAngleDeg: Float,
) {
    companion object {
        const val DEFAULT_RIGHT_EDGE_ANGLE_DEG = 45f

        fun default(context: Context): V2EngineInfoTabShape =
            V2EngineInfoTabShape(
                backgroundColor = ContextCompat.getColor(context, R.color.v2_primary),
                topLeftRadiusPx = context.resources.getDimension(R.dimen.v2_engine_info_tab_top_left_radius),
                rightEdgeAngleDeg = DEFAULT_RIGHT_EDGE_ANGLE_DEG,
            )
    }
}

object V2EngineInfoTabPathBuilder {

    fun build(
        width: Float,
        height: Float,
        topLeftRadiusPx: Float,
        rightEdgeAngleDeg: Float,
        outPath: Path = Path(),
    ): Path {
        outPath.reset()
        if (width <= 0f || height <= 0f) return outPath

        val r = topLeftRadiusPx.coerceIn(0f, min(width, height) / 2f)
        val angleDeg = rightEdgeAngleDeg.coerceIn(0f, 89.9f)
        val dx = if (angleDeg <= 0f) {
            0f
        } else {
            (height * tan(Math.toRadians(angleDeg.toDouble()))).toFloat().coerceIn(0f, width)
        }

        outPath.moveTo(0f, height)
        if (r > 0f) {
            outPath.lineTo(0f, r)
            outPath.arcTo(RectF(0f, 0f, 2f * r, 2f * r), 180f, 90f, false)
        } else {
            outPath.lineTo(0f, 0f)
        }
        // 右上满宽 → 右下向内收（斜切向里，非向外鼓出）
        outPath.lineTo(width, 0f)
        val bottomRightX = (width - dx).coerceAtLeast(if (r > 0f) r else 0f)
        outPath.lineTo(bottomRightX, height)
        outPath.close()
        return outPath
    }
}
