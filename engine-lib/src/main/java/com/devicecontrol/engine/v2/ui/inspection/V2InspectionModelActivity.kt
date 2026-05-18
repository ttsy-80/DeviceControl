package com.devicecontrol.engine.v2.ui.inspection

import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.model.V2ModelCardUi
import com.devicecontrol.engine.v2.ui.adapter.V2InspectionModelAdapter
import com.devicecontrol.engine.v2.ui.base.V2BaseShellActivity
import com.devicecontrol.engine.v2.ui.widget.V2GridSpacingDecoration
import com.devicecontrol.engine.v2.viewmodel.V2EngineViewModelFactory
import com.devicecontrol.engine.v2.viewmodel.V2InspectionModelViewModel

/** 发动机检测 - 型号选择（P6） */
class V2InspectionModelActivity : V2BaseShellActivity() {

    override val logTag: String = "InspectionModel"

    private val viewModel: V2InspectionModelViewModel by viewModels {
        V2EngineViewModelFactory(application)
    }
    private lateinit var adapter: V2InspectionModelAdapter

    override fun contentLayoutId(): Int = R.layout.content_v2_inspection_models

    override fun shellTitleCn(): String = getString(R.string.v2_inspection_title_cn)
    override fun shellTitleEn(): String = getString(R.string.v2_inspection_title_en)

    @DrawableRes
    override fun shellTitleIcon(): Int = R.drawable.ic_v2_inspection_title

    /** P6 稿面：返回首页在内容区底栏行，不用顶栏返回 */
    override fun showBackHome(): Boolean = false

    /** 使用内容区自带底栏（公司名 + 返回首页），避免与壳层底栏重复 */
    override fun showShellBottomBar(): Boolean = false

    override fun onContentCreated(contentRoot: View) {
        adapter = V2InspectionModelAdapter { card -> onModelCardClick(card) }
        // 稿面固定 4 列，不随屏宽折行
        val span = P6_GRID_SPAN
        val spacing = resources.getDimensionPixelSize(R.dimen.v2_p6_grid_spacing)
        contentRoot.findViewById<RecyclerView>(R.id.rvModels).apply {
            layoutManager = GridLayoutManager(this@V2InspectionModelActivity, span)
            addItemDecoration(V2GridSpacingDecoration(span, spacing))
            adapter = this@V2InspectionModelActivity.adapter
            itemAnimator = null
        }

        viewModel.models.observe(this) { adapter.submitList(it) }
        viewModel.selectedModelId.observe(this) { id -> adapter.selectedId = id }

        contentRoot.findViewById<View>(R.id.btnReturnHome).setOnClickListener { navigateToHome() }
    }

    companion object {
        private const val P6_GRID_SPAN = 4
    }

    private fun onModelCardClick(card: V2ModelCardUi) {
        viewModel.selectModel(card.id)
        startActivity(
            Intent(this, V2InspectionControlActivity::class.java).apply {
                putExtra(V2InspectionControlActivity.EXTRA_MODEL_NAME, card.name)
            },
        )
    }
}
