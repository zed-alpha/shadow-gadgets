package com.zedalpha.shadowgadgets.view.shadow

internal interface DrawPlane {

    fun addShadow(shadow: GroupShadow) {}

    fun removeShadow(shadow: GroupShadow) {}

    fun invalidatePlane()

    fun dispose() {}
}