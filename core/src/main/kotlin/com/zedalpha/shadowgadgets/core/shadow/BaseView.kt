package com.zedalpha.shadowgadgets.core.shadow

import android.annotation.SuppressLint
import android.content.Context
import android.view.View

internal abstract class BaseView(context: Context) : View(context) {

    override fun forceLayout() {}

    @SuppressLint("MissingSuperCall") override fun requestLayout() {}

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}
}