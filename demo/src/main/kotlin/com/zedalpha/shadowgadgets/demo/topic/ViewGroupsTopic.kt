package com.zedalpha.shadowgadgets.demo.topic

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentViewGroupsBinding
import com.zedalpha.shadowgadgets.demo.internal.inflateUnattached
import com.zedalpha.shadowgadgets.demo.internal.toColorStateList

internal val ViewGroupsTopic =
    Topic(
        title = "View: Groups",
        descriptionResId = R.string.description_view_groups,
        fragmentClass = ViewGroupsFragment::class.java
    )

class ViewGroupsFragment :
    TopicFragment<FragmentViewGroupsBinding>(FragmentViewGroupsBinding::inflate) {

    override fun loadUi(ui: FragmentViewGroupsBinding) {
        ui.recycler.adapter = ColorfulAdapter()
    }
}

private class ColorfulAdapter : RecyclerView.Adapter<ColorfulHolder>() {

    private val evaluator = ArgbEvaluator()

    override fun getItemCount() = ItemCount

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ColorfulHolder(parent, evaluator)

    override fun onBindViewHolder(holder: ColorfulHolder, position: Int) =
        holder.bind(position)
}

internal open class ColorfulHolder(
    parent: ViewGroup,
    private val evaluator: ArgbEvaluator
) : RecyclerView.ViewHolder(parent.inflateUnattached(R.layout.item_example)) {

    private val text: TextView = itemView.findViewById(R.id.text)

    protected var color: Int = Color.TRANSPARENT
        private set(value) {
            if (field == value) return
            field = value
            itemView.backgroundTintList = value.toColorStateList()
        }

    open fun bind(position: Int) {
        color = evaluator.evaluate(
            (position % HalfItemCount).toFloat() / HalfItemCount,
            if (position < HalfItemCount) ItemRedInt else ItemGreenInt,
            if (position < HalfItemCount) ItemGreenInt else ItemBlueInt
        ) as Int

        @SuppressLint("SetTextI18n")
        text.text = "Item $position"
    }
}

internal const val ItemCount = 100
internal const val HalfItemCount = ItemCount / 2
internal const val ItemRedInt = 0x44ff0000
internal const val ItemGreenInt = 0x4400ff00
internal const val ItemBlueInt = 0x440000ff