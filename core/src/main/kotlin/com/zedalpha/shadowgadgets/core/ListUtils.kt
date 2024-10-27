package com.zedalpha.shadowgadgets.core

inline fun <T> List<T>.fastForEach(block: (T) -> Unit) {
    for (index in indices) block(get(index))
}