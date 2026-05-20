package com.devicecontrol.engine.v2.ui.inspection

import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.model.V2ModelCardUi
import com.devicecontrol.engine.v2.ui.adapter.V2ModelGridAdapter
import com.devicecontrol.engine.v2.ui.base.V2BaseShellActivity
import com.devicecontrol.engine.v2.ui.model.V2ModelGridOverviewUi
import com.devicecontrol.engine.v2.viewmodel.V2EngineViewModelFactory
import com.devicecontrol.engine.v2.viewmodel.V2InspectionModelViewModel

/** 发动机检测 - 型号选择（P6） */
class V2InspectionModelActivity : V2BaseShellActivity() {

    override val logTag: String = "InspectionModel"

    private val viewModel: V2InspectionModelViewModel by viewModels {
        V2EngineViewModelFactory(application)
    }
    private lateinit var adapter: V2ModelGridAdapter

    override fun contentLayoutId(): Int = R.layout.content_v2_model_grid_overview

    override fun shellTitleCn(): String = getString(R.string.v2_inspection_title_cn)
    override fun shellTitleEn(): String = getString(R.string.v2_inspection_title_en)

    @DrawableRes
    override fun shellTitleIcon(): Int = R.drawable.ic_v2_inspection_title

    override fun showBackHome(): Boolean = false

    override fun shellBottomAction(): ShellBottomAction = ShellBottomAction(
        labelCn = getString(R.string.v2_back_home),
    )

    override fun onShellBottomActionClick() = navigateToHome()

    override fun onContentCreated(contentRoot: View) {
        adapter = V2ModelGridAdapter { card -> onModelCardClick(card) }
        V2ModelGridOverviewUi.setupGrid(contentRoot, adapter)

        viewModel.models.observe(this) { adapter.submitList(it) }
        viewModel.selectedModelId.observe(this) { id -> adapter.selectedId = id }
    }

    private fun onModelCardClick(card: V2ModelCardUi) {
        viewModel.selectModel(card.id)
        startActivity(
            Intent(this, V2InspectionControlActivity::class.java).apply {
                putExtra(V2InspectionControlActivity.EXTRA_MODEL_ID, card.id)
                putExtra(V2InspectionControlActivity.EXTRA_MODEL_NAME, card.name)
            },
        )
    }
}
