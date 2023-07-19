package com.zedalpha.shadowgadgets.core

import android.graphics.Path

fun interface PathProvider {
    fun getPath(path: Path)
}