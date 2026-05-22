package com.devicecontrol.engine.v2.ui

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/** V2 页面：沉浸式布局 + 系统栏区域透出白底、深色图标/文字。 */
object V2SystemBars {

    /**
     * 横屏 edge-to-edge；[contentRoot] 仅绑定一次 insets（在 setContentView 之后）。
     */
    fun applyImmersiveLightSystemBars(activity: Activity, contentRoot: View) {
        applyWindowAppearance(activity)
        attachSystemBarInsets(contentRoot)
    }

    /** 从后台恢复时仅刷新系统栏样式，避免重复叠加 padding。 */
    fun refreshWindowAppearance(activity: Activity) {
        applyWindowAppearance(activity)
    }

    private fun applyWindowAppearance(activity: Activity) {
        val window = activity.window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
    }

    private fun attachSystemBarInsets(root: View) {
        val initialPadding = InsetsPadding(
            root.paddingLeft,
            root.paddingTop,
            root.paddingRight,
            root.paddingBottom,
        )
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                initialPadding.left + bars.left,
                initialPadding.top + bars.top,
                initialPadding.right + bars.right,
                initialPadding.bottom + bars.bottom,
            )
            insets
        }
        ViewCompat.requestApplyInsets(root)
    }

    private data class InsetsPadding(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int,
    )
}
