package com.zedalpha.shadowgadgets.core.layer

import android.view.View

class LocationTracker(private val view: View) {

    val current = IntArray(2)

    fun initialize() {
        view.getLocationOnScreen(current)
    }

    private val tmp = IntArray(2)

    fun checkLocationChanged(): Boolean = tmp.let { location ->
        view.getLocationOnScreen(location)
        current.checkUpdate(location)
    }

    private fun IntArray.checkUpdate(other: IntArray): Boolean =
        if (this[0] != other[0] || this[1] != other[1]) {
            this[0] = other[0]; this[1] = other[1]
            true
        } else {
            false
        }
}