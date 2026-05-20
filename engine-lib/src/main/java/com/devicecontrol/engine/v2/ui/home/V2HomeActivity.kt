package com.devicecontrol.engine.v2.ui.home

import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.ui.base.V2BaseShellActivity
import com.devicecontrol.engine.v2.ui.inspection.V2InspectionModelActivity
import com.devicecontrol.engine.v2.ui.model.V2ModelCatalogActivity
import com.devicecontrol.engine.v2.ui.settings.V2SettingsActivity
import com.devicecontrol.engine.v2.viewmodel.V2HomeViewModel

/** 2.0 主界面（P1） */
class V2HomeActivity : V2BaseShellActivity() {

    override val logTag: String = "Home"

    private val viewModel: V2HomeViewModel by viewModels()

    override fun contentLayoutId(): Int = R.layout.content_v2_home

    override fun shellTitleCn(): String = getString(R.string.v2_app_title_cn)
    override fun shellTitleEn(): String = getString(R.string.v2_app_title_en)

    override fun shellBottomAction(): ShellBottomAction = ShellBottomAction(
        labelCn = getString(R.string.v2_setting),
        labelEn = getString(R.string.v2_setting_en),
    )

    override fun onShellBottomActionClick() {
        viewModel.onSettingsClicked()
        startActivity(Intent(this, V2SettingsActivity::class.java))
    }

    override fun onContentCreated(contentRoot: View) {
        contentRoot.findViewById<View>(R.id.cardStartInspection).setOnClickListener {
            viewModel.onStartInspectionClicked()
            startActivity(Intent(this, V2InspectionModelActivity::class.java))
        }
        contentRoot.findViewById<View>(R.id.cardModelManage).setOnClickListener {
            viewModel.onModelManageClicked()
            startActivity(Intent(this, V2ModelCatalogActivity::class.java))
        }
    }
}
