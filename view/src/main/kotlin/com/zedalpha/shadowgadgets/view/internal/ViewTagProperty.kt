package com.zedalpha.shadowgadgets.view.internal

import android.view.View
import androidx.annotation.IdRes
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal fun <T : View, V> viewTag(
    @IdRes id: Int,
    default: V,
    onChange: (T.(new: V) -> Unit)? = null
): ReadWriteProperty<T, V> =
    ViewTagProperty(id, default, onChange)

private class ViewTagProperty<T : View, V>(
    private val id: Int,
    private val default: V,
    private val onChange: (T.(new: V) -> Unit)?
) : ReadWriteProperty<T, V> {

    class Holder<V>(var value: V)

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        @Suppress("UNCHECKED_CAST")
        val holder = thisRef.getTag(id) as? Holder<V>
        return if (holder != null) holder.value else default
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        @Suppress("UNCHECKED_CAST")
        val holder = thisRef.getTag(id) as? Holder<V>

        val initialized = holder != null
        val current = if (initialized) holder.value else default

        val changed = current != value
        if (initialized && !changed) return

        if (!initialized) {
            thisRef.setTag(id, Holder(value))
        } else {
            holder.value = value
        }

        if (changed) onChange?.invoke(thisRef, value)
    }
}