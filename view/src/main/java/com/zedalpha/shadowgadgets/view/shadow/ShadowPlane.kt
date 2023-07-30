package com.zedalpha.shadowgadgets.view.shadow


internal interface ShadowPlane {

    val delegatesFiltering: Boolean get() = true

    fun showShadow(shadow: GroupShadow) {}

    fun hideShadow(shadow: GroupShadow) {}

    fun invalidatePlane()
}