package com.zedalpha.shadowgadgets.view.internal

import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.plane.Plane

internal fun ViewGroup.invalidator(): Invalidator =
    this.invalidator ?: Invalidator(this)

private var ViewGroup.invalidator: Invalidator?
        by viewTag(R.id.invalidator, null)

internal class Invalidator(private val viewGroup: ViewGroup) :
    AutoDisposeSwitchGroup<Plane>() {

    var isInvalidated = false
        private set

    private val resetInvalidated = OnDraw { isInvalidated = false }

    init {
        viewGroup.invalidator = this
        viewGroup.addOnDraw(resetInvalidated)
    }

    override fun dispose() {
        super.dispose()
        viewGroup.invalidator = null
        viewGroup.removeOnDraw(resetInvalidated)
    }

    fun invalidate() {
        if (isInvalidated) return
        viewGroup.invalidate()
        isInvalidated = true
    }
}