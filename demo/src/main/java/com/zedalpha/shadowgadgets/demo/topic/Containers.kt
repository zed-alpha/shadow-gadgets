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
import com.zedalpha.shadowgadgets.demo.R


class ContainersFragment : TopicFragment(R.layout.fragment_containers) {
    override val targetIds = intArrayOf()

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.text)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recycler)
        recycler.adapter =
            object : RecyclerView.Adapter<VH>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                    VH(
                        LayoutInflater.from(requireContext())
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


        val fabStart = view.findViewById<FloatingActionButton>(R.id.fab_containers_start)
        val fabCenter = view.findViewById<ExtendedFloatingActionButton>(R.id.fab_containers_center)
        val fabEnd = view.findViewById<FloatingActionButton>(R.id.fab_containers_end)

        val snackbar = Snackbar.make(fabStart, "I'm translucent!", Snackbar.LENGTH_SHORT)
        snackbar.view.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), R.color.see_through_deep_blue)
        )
        snackbar.view.tag = resources.getString(R.string.clip_outline_shadow_tag_value)

        fabStart.setOnClickListener {
            snackbar.duration = Snackbar.LENGTH_SHORT
            snackbar.show()
        }
        fabCenter.setOnClickListener {
            snackbar.duration = Snackbar.LENGTH_INDEFINITE
            snackbar.addCallback(object : Snackbar.Callback() {
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
            })
            snackbar.show()
        }
        fabEnd.setOnClickListener {
            fabStart.hide(); fabCenter.hide(); fabEnd.hide()
            fabCenter.postDelayed(1000) {
                fabStart.show(); fabCenter.show(); fabEnd.show()
            }
        }
    }
}

const val COUNT = 50
const val HALF_COUNT = 25
const val ITEM_RED = 0x44FF0000
const val ITEM_GREEN = 0x4400FF00
const val ITEM_BLUE = 0x440000FF