package com.devicecontrol.engine.v2.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.devicecontrol.engine.databinding.ActivityV2ShellBinding
import com.devicecontrol.engine.v2.log.V2Log
import com.devicecontrol.engine.v2.ui.shell.V2ShellBottomBarBinder
import com.devicecontrol.engine.v2.ui.shell.V2ShellUiBinder
import com.devicecontrol.engine.v2.ui.widget.applyV2LandscapeIme

/**
 * 2.0 页面基类：顶栏 + 底栏壳层 + 子类内容区。
 * 不修改 1.0 Activity，新页面均继承此类。
 */
abstract class V2BaseShellActivity : AppCompatActivity() {

    protected lateinit var shellBinding: ActivityV2ShellBinding
        private set

    protected lateinit var shellBinder: V2ShellUiBinder
        private set

    protected lateinit var bottomBarBinder: V2ShellBottomBarBinder
        private set

    protected abstract val logTag: String

    /** 底栏右下角操作钮文案；null 表示不显示 */
    data class ShellBottomAction(
        val labelCn: String,
        /** null 或空串时不显示英文行（如 P6/P13「返回首页」） */
        val labelEn: String? = null,
    )

    @LayoutRes
    protected abstract fun contentLayoutId(): Int

    protected abstract fun shellTitleCn(): String

    protected abstract fun shellTitleEn(): String

    /** 是否显示顶栏「返回首页」 */
    protected open fun showBackHome(): Boolean = false

    /** 顶栏标题左侧图标（如 P6 检测放大镜）；null 不显示 */
    @DrawableRes
    protected open fun shellTitleIcon(): Int? = null

    /** 是否显示全局底栏品牌（设置页 P2～P5 使用内容区底栏，需隐藏） */
    protected open fun showShellBottomBar(): Boolean = true

    /** 底栏右下角双语操作钮；默认隐藏 */
    protected open fun shellBottomAction(): ShellBottomAction? = null

    protected open fun onShellBottomActionClick() {}

    protected open fun onContentCreated(contentRoot: View) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shellBinding = ActivityV2ShellBinding.inflate(layoutInflater)
        setContentView(shellBinding.root)

        LayoutInflater.from(this).inflate(contentLayoutId(), shellBinding.flV2Content, true)

        shellBinder = V2ShellUiBinder(shellBinding.includeV2Top.root, this)
        shellBinder.bindTitles(shellTitleCn(), shellTitleEn())
        shellBinder.setTitleIcon(shellTitleIcon())
        shellBinder.setBackHomeVisible(showBackHome()) {
            V2Log.i(logTag, "backHome clicked")
            navigateToHome()
        }
        shellBinder.observeConnection()

        bottomBarBinder = V2ShellBottomBarBinder(shellBinding.includeV2Bottom.root)
        applyShellBottomAction()

        shellBinding.includeV2Bottom.root.visibility =
            if (showShellBottomBar()) View.VISIBLE else View.GONE

        val contentRoot = shellBinding.flV2Content.getChildAt(0)
        onContentCreated(contentRoot)
        applyV2LandscapeImeInTree(contentRoot)
        V2Log.i(logTag, "onCreate ${javaClass.simpleName}")
    }

    /** 覆盖 XML 已配置及后续动态 inflate 的输入框 */
    protected fun applyV2LandscapeImeInTree(root: View?) {
        if (root == null) return
        if (root is EditText) {
            root.applyV2LandscapeIme()
        }
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                applyV2LandscapeImeInTree(root.getChildAt(i))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        shellBinder.startClock()
    }

    override fun onPause() {
        shellBinder.stopClock()
        super.onPause()
    }

    protected fun applyShellBottomAction() {
        val action = shellBottomAction()
        if (action == null) {
            setShellBottomActionVisible(false)
        } else {
            updateShellBottomActionLabels(action.labelCn, action.labelEn)
            setShellBottomActionVisible(true)
            bottomBarBinder.setBottomActionClickListener { onShellBottomActionClick() }
        }
    }

    protected fun setShellBottomActionVisible(visible: Boolean) {
        bottomBarBinder.setBottomActionVisible(visible)
    }

    protected fun updateShellBottomActionLabels(labelCn: String, labelEn: String? = null) {
        bottomBarBinder.bindBottomActionLabels(labelCn, labelEn)
    }

    protected fun setShellBottomActionClickListener(listener: (() -> Unit)?) {
        bottomBarBinder.setBottomActionClickListener(listener)
    }

    protected fun navigateToHome() {
        val intent = android.content.Intent(this, com.devicecontrol.engine.v2.ui.home.V2HomeActivity::class.java)
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }
}
