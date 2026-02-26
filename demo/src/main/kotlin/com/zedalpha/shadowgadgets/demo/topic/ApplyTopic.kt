package com.zedalpha.shadowgadgets.demo.topic

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentApplyBinding
import com.zedalpha.shadowgadgets.demo.internal.toColorStateList

internal val ApplyTopic =
    Topic(
        title = "Apply",
        descriptionResId = R.string.description_apply,
        fragmentClass = ApplyFragment::class.java
    )

class ApplyFragment :
    TopicFragment<FragmentApplyBinding>(FragmentApplyBinding::inflate) {

    override fun loadUi(ui: FragmentApplyBinding) {
        ui.recycler.adapter = ColorfulAdapter()
    }
}

private class ColorfulAdapter : RecyclerView.Adapter<ItemHolder>() {

    private val evaluator = ArgbEvaluator()

    override fun getItemCount() = ItemCount

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ItemHolder(parent)

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val color =
            evaluator.evaluate(
                (position % HalfItemCount).toFloat() / HalfItemCount,
                if (position < HalfItemCount) ClearRedInt else ClearGreenInt,
                if (position < HalfItemCount) ClearGreenInt else ClearBlueInt
            ) as Int
        holder.itemView.backgroundTintList = color.toColorStateList()
        @SuppressLint("SetTextI18n")
        holder.text.text = "Item $position"
    }
}

internal class ItemHolder(parent: ViewGroup) :
    RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_example, parent, false)
    ) {

    val text: TextView = itemView.findViewById(R.id.text)
}

internal const val ItemCount = 100
internal const val HalfItemCount = ItemCount / 2
internal const val ClearRedInt = 0x44ff0000
internal const val ClearGreenInt = 0x4400ff00
internal const val ClearBlueInt = 0x440000ff