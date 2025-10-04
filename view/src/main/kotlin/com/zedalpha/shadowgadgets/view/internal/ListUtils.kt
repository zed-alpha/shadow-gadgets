package com.zedalpha.shadowgadgets.view.internal

internal inline fun <T> List<T>.iterate(block: (T) -> Unit) {
    for (index in this.indices) block(this[index])
}

internal inline fun <T> List<T>.leading(condition: (T) -> Boolean): T? {
    this.iterate { if (condition(it)) return it }
    return null
}

internal inline fun <T> List<T>.has(condition: (T) -> Boolean): Boolean {
    this.iterate { if (condition(it)) return true }
    return false
}