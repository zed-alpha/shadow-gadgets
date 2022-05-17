package com.zedalpha.shadowgadgets.demo.topic

import com.zedalpha.shadowgadgets.demo.R

class IssuesFragment : TopicFragment(R.layout.fragment_issues) {
    override val targetIds = intArrayOf(
        R.id.view_z_minus,
        R.id.view_z_middle,
        R.id.view_z_plus,
        R.id.circle_left,
        R.id.circle_right
    )
}