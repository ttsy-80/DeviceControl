package com.devicecontrol.engine.v2.ui.model

import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.model.V2ModelCardUi
import com.devicecontrol.engine.v2.ui.adapter.V2ModelCardAdapter
import com.devicecontrol.engine.v2.ui.base.V2BaseShellActivity
import com.devicecontrol.engine.v2.viewmodel.V2ModelCatalogViewModel

/** 型号管理目录（P13） */
class V2ModelCatalogActivity : V2BaseShellActivity() {

    override val logTag: String = "ModelCatalog"

    private val viewModel: V2ModelCatalogViewModel by viewModels()
    private lateinit var adapter: V2ModelCardAdapter

    override fun contentLayoutId(): Int = R.layout.content_v2_model_catalog

    override fun shellTitleCn(): String = getString(R.string.v2_model_manage)
    /** 英文副标题仅在顶栏展示（P13），内容区不再重复 */
    override fun shellTitleEn(): String = getString(R.string.v2_model_catalog)

    override fun showBackHome(): Boolean = true

    override fun onContentCreated(contentRoot: View) {
        adapter = V2ModelCardAdapter { card -> onCardClick(card) }
        val span = 4
        contentRoot.findViewById<RecyclerView>(R.id.rvCatalog).apply {
            layoutManager = GridLayoutManager(this@V2ModelCatalogActivity, span)
            adapter = this@V2ModelCatalogActivity.adapter
        }
        viewModel.items.observe(this) { adapter.submitList(it) }
        contentRoot.findViewById<View>(R.id.btnCatalogHome).setOnClickListener { navigateToHome() }
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
