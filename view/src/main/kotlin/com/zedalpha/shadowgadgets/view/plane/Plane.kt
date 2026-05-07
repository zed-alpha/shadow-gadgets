package com.zedalpha.shadowgadgets.view.plane

import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.proxy.ShadowProxy
import com.zedalpha.shadowgadgets.view.shadow.Shadow

internal interface Plane {
    val viewGroup: ViewGroup?
    fun addProxy(proxy: ShadowProxy)
    fun updateLayer(proxy: ShadowProxy)
    fun removeProxy(proxy: ShadowProxy)
    fun Shadow.differsFrom(target: View): Boolean
    fun invalidate()

    companion object {
        val Null: Plane = Void()
        val Error: Plane = Void()
    }

    private class Void : Plane {
        override val viewGroup: ViewGroup? = null
        override fun addProxy(proxy: ShadowProxy) {}
        override fun updateLayer(proxy: ShadowProxy) {}
        override fun removeProxy(proxy: ShadowProxy) {}
        override fun Shadow.differsFrom(target: View): Boolean = false
        override fun invalidate() {}
    }
}

internal fun Plane.isInvalid(proxy: ShadowProxy): Boolean =
    proxy.isShown && proxy.shadow.differsFrom(proxy.target)