package com.zedalpha.shadowgadgets.view.layer

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import com.zedalpha.shadowgadgets.view.internal.DefaultShadowColor
import com.zedalpha.shadowgadgets.view.internal.Group
import com.zedalpha.shadowgadgets.view.internal.SwitchGroup
import com.zedalpha.shadowgadgets.view.proxy.ShadowProxy

internal interface GroupLayer : Layer, Group<ShadowProxy>

internal fun GroupLayer(owner: View): GroupLayer = GroupLayerImpl(owner)

private class GroupLayerImpl
private constructor(owner: View, proxies: Group<ShadowProxy>) :
    GroupLayer,
    AutomaticLayer(owner, { canvas -> proxies.iterate { it.draw(canvas) } }),
    Group<ShadowProxy> by proxies {

    constructor(owner: View) : this(owner, SwitchGroup())
}

internal class InertGroupLayer : GroupLayer, SwitchGroup<ShadowProxy>() {

    override var color: Int = DefaultShadowColor
        set(_) {}

    override var bounds: Rect = ZeroBounds
        set(_) {}

    override fun draw(canvas: Canvas) = iterate { it.draw(canvas) }
    override fun recreate(): Boolean = false
    override fun dispose() {}
}

private val ZeroBounds = Rect()