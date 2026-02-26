package com.zedalpha.shadowgadgets.view.plane

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.proxy.ShadowProxy
import com.zedalpha.shadowgadgets.view.shadow.Shadow

internal interface Plane {
    val viewGroup: ViewGroup?
    fun addProxy(proxy: ShadowProxy)
    fun updateLayer(proxy: ShadowProxy)
    fun removeProxy(proxy: ShadowProxy)
    fun Shadow.doesNotMatch(target: View): Boolean
    fun invalidate()

    @SuppressLint("StaticFieldLeak")
    data object Initial : Inert()

    @SuppressLint("StaticFieldLeak")
    data object Null : Inert()

    sealed class Inert : Plane {
        override val viewGroup: ViewGroup? = null
        override fun addProxy(proxy: ShadowProxy) {}
        override fun updateLayer(proxy: ShadowProxy) {}
        override fun removeProxy(proxy: ShadowProxy) {}
        override fun Shadow.doesNotMatch(target: View): Boolean = false
        override fun invalidate() {}
    }
}

internal fun Plane.isInvalid(proxy: ShadowProxy): Boolean =
    proxy.isShown && proxy.shadow.doesNotMatch(proxy.target)