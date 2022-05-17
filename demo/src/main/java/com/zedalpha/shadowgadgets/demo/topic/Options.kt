package com.zedalpha.shadowgadgets.demo.topic

import com.zedalpha.shadowgadgets.demo.R

class OptionsFragment : TopicFragment(R.layout.fragment_options) {
    override val targetIds = intArrayOf(
        R.id.view_z_minus,
        R.id.circle_above,
        R.id.circle_below,
        R.id.circle_top,
        R.id.circle_middle,
        R.id.circle_bottom,
        R.id.circle_top_fallback,
        R.id.circle_middle_fallback,
        R.id.circle_bottom_fallback
    )
}