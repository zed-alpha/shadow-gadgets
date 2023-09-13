package com.zedalpha.shadowgadgets.view.shadow

internal interface DrawPlane {

    fun addShadow(shadow: GroupShadow, color: Int) {}

    fun updateColor(shadow: GroupShadow, color: Int)

    fun removeShadow(shadow: GroupShadow) {}

    fun invalidatePlane()

    fun dispose()
}