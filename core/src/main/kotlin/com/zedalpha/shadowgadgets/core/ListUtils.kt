package com.zedalpha.shadowgadgets.core

public inline fun <T> List<T>.fastForEach(block: (T) -> Unit) {
    for (index in indices) {
        val element = get(index)
        block(element)
    }
}