package com.zedalpha.shadowgadgets.demo.topic

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zedalpha.shadowgadgets.demo.R


class ViewGroupsFragment : TopicFragment(R.layout.fragment_view_groups) {
    override val targetIds = intArrayOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recycler)
        recycler.adapter =
            object : RecyclerView.Adapter<VH>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                    VH(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.item_recycler, parent, false)
                    )

                val evaluator = ArgbEvaluator()

                @SuppressLint("SetTextI18n")
                override fun onBindViewHolder(holder: VH, position: Int) {
                    holder.textView.text = "Item $position"

                    val startColor = if (position < HALF_COUNT) ITEM_RED else ITEM_GREEN
                    val endColor = if (position < HALF_COUNT) ITEM_GREEN else ITEM_BLUE
                    holder.itemView.backgroundTintList =
                        ColorStateList.valueOf(
                            evaluator.evaluate(
                                ((position % HALF_COUNT) / HALF_COUNT.toFloat()),
                                startColor,
                                endColor
                            ) as Int
                        )
                }

                override fun getItemCount() = COUNT
            }
    }
}

private class VH(view: View) : RecyclerView.ViewHolder(view) {
    val textView: TextView = view.findViewById(R.id.text)
}

private const val COUNT = 50
private const val HALF_COUNT = 25
private const val ITEM_RED = 0x44FF0000
private const val ITEM_GREEN = 0x4400FF00
private const val ITEM_BLUE = 0x440000FF