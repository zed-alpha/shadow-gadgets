package com.zedalpha.shadowgadgets.demo.topic

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentApplyBinding

internal val ApplyTopic = Topic(
    "Apply",
    R.string.description_apply,
    ApplyFragment::class.java
)

class ApplyFragment : TopicFragment<FragmentApplyBinding>(
    FragmentApplyBinding::inflate
) {
    override fun loadUi(ui: FragmentApplyBinding) {
        ui.recycler.adapter = ColorfulAdapter()
    }
}

private class ColorfulAdapter : RecyclerView.Adapter<ColorfulHolder>() {

    private val evaluator = ArgbEvaluator()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_colorful, parent, false)
            .let { ColorfulHolder(it) }

    override fun onBindViewHolder(holder: ColorfulHolder, position: Int) {
        val color = evaluator.evaluate(
            (position % HALF_ITEM_COUNT).toFloat() / HALF_ITEM_COUNT,
            if (position < HALF_ITEM_COUNT) ITEM_RED else ITEM_GREEN,
            if (position < HALF_ITEM_COUNT) ITEM_GREEN else ITEM_BLUE
        ) as Int
        holder.itemView.backgroundTintList = ColorStateList.valueOf(color)
        @SuppressLint("SetTextI18n")
        holder.textView.text = "Item $position"
    }

    override fun getItemCount() = ITEM_COUNT
}

internal class ColorfulHolder(view: View) : RecyclerView.ViewHolder(view) {
    val textView: TextView = view.findViewById(R.id.text)
}

internal const val ITEM_COUNT = 100
internal const val HALF_ITEM_COUNT = ITEM_COUNT / 2
internal const val ITEM_RED = 0x44ff0000
internal const val ITEM_GREEN = 0x4400ff00
internal const val ITEM_BLUE = 0x440000ff