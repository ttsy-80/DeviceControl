package com.devicecontrol.engine.v2.ui.model

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.ui.adapter.V2ModelGridAdapter
import com.devicecontrol.engine.v2.ui.widget.V2GridSpacingDecoration

/** P6 / P13 宫格区共用绑定（4 列、间距、滚动条） */
object V2ModelGridOverviewUi {

    const val GRID_SPAN = 4

    fun setupGrid(contentRoot: View, adapter: V2ModelGridAdapter): RecyclerView {
        val spacing = contentRoot.resources.getDimensionPixelSize(R.dimen.v2_p6_grid_spacing)
        val rv = contentRoot.findViewById<RecyclerView>(R.id.rvModelGrid)
        rv.layoutManager = GridLayoutManager(contentRoot.context, GRID_SPAN)
        rv.addItemDecoration(V2GridSpacingDecoration(GRID_SPAN, spacing))
        rv.adapter = adapter
        rv.itemAnimator = null
        return rv
    }
}
