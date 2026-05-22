package com.devicecontrol.engine.v2.ui.model

import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.model.V2ModelCardUi
import com.devicecontrol.engine.v2.ui.adapter.V2ModelGridAdapter
import com.devicecontrol.engine.v2.ui.base.V2BaseShellActivity
import com.devicecontrol.engine.v2.viewmodel.V2EngineViewModelFactory
import com.devicecontrol.engine.v2.viewmodel.V2ModelCatalogViewModel

/** 型号管理目录（P13） */
class V2ModelCatalogActivity : V2BaseShellActivity() {

    override val logTag: String = "ModelCatalog"

    private val viewModel: V2ModelCatalogViewModel by viewModels {
        V2EngineViewModelFactory(application)
    }
    private lateinit var adapter: V2ModelGridAdapter

    override fun contentLayoutId(): Int = R.layout.content_v2_model_grid_overview

    override fun shellTitleCn(): String = getString(R.string.v2_model_manage)
    override fun shellTitleEn(): String = getString(R.string.v2_model_catalog)

    @DrawableRes
    override fun shellTitleIcon(): Int = R.drawable.ic_v2_home_model_manage

    override fun showBackHome(): Boolean = false

    override fun shellBottomAction(): ShellBottomAction = ShellBottomAction(
        labelCn = getString(R.string.v2_back_home),
    )

    override fun onShellBottomActionClick() = navigateToHome()

    override fun onContentCreated(contentRoot: View) {
        adapter = V2ModelGridAdapter { card -> onCardClick(card) }
        V2ModelGridOverviewUi.setupGrid(contentRoot, adapter)

        viewModel.items.observe(this) { adapter.submitList(it) }
    }

    private fun onCardClick(card: V2ModelCardUi) {
        if (card.isAddCard) {
            viewModel.onAddCardClicked()
            startActivity(Intent(this, V2ModelAddActivity::class.java))
            return
        }
        startActivity(
            Intent(this, V2ModelDetailActivity::class.java).apply {
                putExtra(V2ModelDetailActivity.EXTRA_MODEL_NAME, card.name)
            },
        )
    }
}
