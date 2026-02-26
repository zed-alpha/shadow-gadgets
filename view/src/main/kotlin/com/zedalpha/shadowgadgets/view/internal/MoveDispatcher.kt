package com.zedalpha.shadowgadgets.view.internal

import android.view.View
import com.zedalpha.shadowgadgets.view.R

internal fun interface OnMove {
    fun onMove()
}

internal fun View.addOnMove(action: OnMove) =
    (this.moveDispatcher ?: MoveDispatcher(this)).add(action)

internal fun View.removeOnMove(action: OnMove) =
    this.moveDispatcher?.remove(action)

private var View.moveDispatcher: MoveDispatcher?
        by viewTag(R.id.move_dispatcher, null)

private class MoveDispatcher(private val view: View) :
    AutoDisposeSwitchGroup<OnMove>() {

    private val checkLocationChange =
        OnPreDraw { if (hasMoved()) iterate { it.onMove() } }

    init {
        view.moveDispatcher = this
        view.addOnPreDraw(checkLocationChange)
    }

    override fun dispose() {
        super.dispose()
        view.moveDispatcher = null
        view.removeOnPreDraw(checkLocationChange)
    }

    private val location =
        IntArray(2).also { view.getLocationOnScreen(it) }

    private fun hasMoved(): Boolean {
        val location = this.location
        val (lastX, lastY) = location
        view.getLocationOnScreen(location)
        return location[0] != lastX || location[1] != lastY
    }
}