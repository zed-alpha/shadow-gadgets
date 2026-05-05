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
    fun Shadow.differsFrom(target: View): Boolean
    fun invalidate()

    @SuppressLint("StaticFieldLeak")
    data object Null : Void()

    @SuppressLint("StaticFieldLeak")
    data object Error : Void()

    sealed class Void : Plane {
        final override val viewGroup: ViewGroup? = null
        final override fun addProxy(proxy: ShadowProxy) {}
        final override fun updateLayer(proxy: ShadowProxy) {}
        final override fun removeProxy(proxy: ShadowProxy) {}
        final override fun Shadow.differsFrom(target: View): Boolean = false
        final override fun invalidate() {}
    }
}

internal fun Plane.isInvalid(proxy: ShadowProxy): Boolean =
    proxy.isShown && proxy.shadow.differsFrom(proxy.target)