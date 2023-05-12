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
import com.zedalpha.shadowgadgets.demo.databinding.FragmentApplicationBinding


internal object Application : Topic {

    override val descriptionResId = R.string.description_application

    override fun createContentFragment() = Content()

    class Content : ContentFragment(R.layout.fragment_application) {

        override fun loadUi(view: View) {
            val ui = FragmentApplicationBinding.bind(view)
            ui.recycler.adapter = ColorfulAdapter()
        }
    }
}

private class ColorfulAdapter : RecyclerView.Adapter<ColorfulHolder>() {

    private val evaluator = ArgbEvaluator()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = ColorfulHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_colorful, parent, false)
    )

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ColorfulHolder, position: Int) {
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

private class ColorfulHolder(view: View) : RecyclerView.ViewHolder(view) {
    val textView: TextView = view.findViewById(R.id.text)
}

private const val COUNT = 50
private const val HALF_COUNT = 25
private const val ITEM_RED = 0x44FF0000
private const val ITEM_GREEN = 0x4400FF00
private const val ITEM_BLUE = 0x440000FF