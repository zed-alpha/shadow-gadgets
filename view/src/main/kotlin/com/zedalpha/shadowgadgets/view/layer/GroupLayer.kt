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
    AutoPositionLayer(
        owner = owner,
        content = { canvas -> proxies.iterate { it.updateAndDraw(canvas) } }
    ),
    Group<ShadowProxy> by proxies {

    constructor(owner: View) : this(owner, SwitchGroup())
}

internal class InertGroupLayer : GroupLayer, SwitchGroup<ShadowProxy>() {

    @Suppress("SetterBackingFieldAssignment")
    override var color: Int = DefaultShadowColor
        set(_) {}

    @Suppress("SetterBackingFieldAssignment")
    override var bounds: Rect = ZeroBounds
        set(_) {}

    override fun draw(canvas: Canvas) = iterate { it.updateAndDraw(canvas) }
    override fun recreate(): Boolean = false
    override fun dispose() {}
}

private val ZeroBounds = Rect()