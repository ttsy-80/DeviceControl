package com.devicecontrol.engine.v2.ui.shell

import android.view.View
import android.widget.TextView
import com.devicecontrol.engine.R
import com.google.android.material.card.MaterialCardView

/**
 * 绑定 [include_v2_bottom_bar]：品牌区 + 可选右下角双语操作钮（如首页「设置」）。
 */
class V2ShellBottomBarBinder(bottomBarRoot: View) {

    private val cardAction: MaterialCardView = bottomBarRoot.findViewById(R.id.cardV2BottomAction)
    private val tvActionCn: TextView = bottomBarRoot.findViewById(R.id.tvV2BottomActionCn)
    private val tvActionEn: TextView = bottomBarRoot.findViewById(R.id.tvV2BottomActionEn)

    fun setBottomActionVisible(visible: Boolean) {
        cardAction.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun bindBottomActionLabels(labelCn: String, labelEn: String) {
        tvActionCn.text = labelCn
        tvActionEn.text = labelEn
    }

    fun setBottomActionClickListener(listener: (() -> Unit)?) {
        cardAction.setOnClickListener { listener?.invoke() }
    }
}
