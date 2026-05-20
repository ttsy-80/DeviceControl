package com.devicecontrol.engine.v2.ui.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.model.V2ModelCardUi
import com.google.android.material.card.MaterialCardView

/** P6 / P13 型号宫格：橙/桃选中、蓝色「新增」卡 */
class V2ModelGridAdapter(
    private val onClick: (V2ModelCardUi) -> Unit,
) : ListAdapter<V2ModelCardUi, V2ModelGridAdapter.Holder>(Diff) {

    var selectedId: Long? = null
        set(value) {
            if (field == value) return
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_v2_model_grid_card, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position), selectedId, onClick)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.cardRoot)
        private val tvName: TextView = itemView.findViewById(R.id.tvModelName)
        private val ivAdd: ImageView = itemView.findViewById(R.id.ivAddIcon)

        fun bind(item: V2ModelCardUi, selectedId: Long?, onClick: (V2ModelCardUi) -> Unit) {
            val ctx = itemView.context
            if (item.isAddCard) {
                card.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.v2_catalog_add_blue))
                tvName.visibility = View.GONE
                ivAdd.setImageResource(R.drawable.ic_v2_add_model)
                ImageViewCompat.setImageTintList(
                    ivAdd,
                    ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.v2_on_primary)),
                )
                ivAdd.visibility = View.VISIBLE
                card.setOnClickListener { onClick(item) }
                return
            }
            tvName.visibility = View.VISIBLE
            ivAdd.visibility = View.GONE
            tvName.text = item.name
            val bgRes = if (item.id == selectedId) R.color.v2_peach else R.color.v2_primary
            card.setCardBackgroundColor(ContextCompat.getColor(ctx, bgRes))
            card.setOnClickListener { onClick(item) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<V2ModelCardUi>() {
        override fun areItemsTheSame(a: V2ModelCardUi, b: V2ModelCardUi) =
            a.id == b.id && a.isAddCard == b.isAddCard

        override fun areContentsTheSame(a: V2ModelCardUi, b: V2ModelCardUi) = a == b
    }
}
