package com.zedalpha.shadowgadgets.demo.topic

import com.zedalpha.shadowgadgets.demo.R

class LimitationsFragment : TopicFragment(R.layout.fragment_limitations) {
    override val targetIds = intArrayOf(
        R.id.view_circle,
        R.id.view_rectangle,
        R.id.view_round_rectangle,
        R.id.view_roundish_rectangle
    )
}