package com.zedalpha.shadowgadgets.demo.topic

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.zedalpha.shadowgadgets.clipOutlineShadow
import com.zedalpha.shadowgadgets.demo.R


class ViewGroupsFragment : TopicFragment(R.layout.fragment_view_groups) {
    override val targetIds = intArrayOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fabStart = view.findViewById<FloatingActionButton>(R.id.fab_view_groups_start)
        val fabCenter = view.findViewById<ExtendedFloatingActionButton>(R.id.fab_view_groups_center)
        val fabEnd = view.findViewById<FloatingActionButton>(R.id.fab_view_groups_end)

        val snackbar = Snackbar.make(fabStart, "I'm translucent!", Snackbar.LENGTH_SHORT)
        snackbar.view.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), R.color.see_through_deep_blue)
        )
        snackbar.view.clipOutlineShadow = true

        val callback = object : Snackbar.Callback() {
            override fun onShown(sb: Snackbar?) {
                fabStart.hide(); fabCenter.hide(); fabEnd.hide()
                fabCenter.postDelayed(1000) {
                    fabStart.addOnShowAnimationListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            snackbar.dismiss()
                        }
                    })
                    fabStart.show(); fabCenter.show(); fabEnd.show()
                }
            }
        }

        fabStart.setOnClickListener {
            snackbar.duration = Snackbar.LENGTH_SHORT
            snackbar.removeCallback(callback)
            snackbar.show()
        }
        fabCenter.setOnClickListener {
            snackbar.duration = Snackbar.LENGTH_INDEFINITE
            snackbar.addCallback(callback)
            snackbar.show()
        }
        fabEnd.setOnClickListener {
            snackbar.duration = Snackbar.LENGTH_SHORT
            snackbar.removeCallback(callback)
            fabStart.hide(); fabCenter.hide(); fabEnd.hide()
            fabCenter.postDelayed(1000) {
                fabStart.show(); fabCenter.show(); fabEnd.show()
            }
        }


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