package com.zedalpha.shadowgadgets.view.internal

import android.os.Build
import java.lang.reflect.Method

private val getDeclaredMethod: Method? =
    if (Build.VERSION.SDK_INT == 28) {
        try {
            Class::class.java.getDeclaredMethod(
                "getDeclaredMethod",
                String::class.java,
                emptyArray<Class<*>>()::class.java
            )
        } catch (_: Exception) {
            null
        }
    } else {
        null
    }

internal fun getDeclaredMethod(
    clazz: Class<*>,
    name: String,
    vararg parameterTypes: Class<*>
): Method? =
    if (Build.VERSION.SDK_INT == 28) {
        val args = arrayOf(*parameterTypes)
        getDeclaredMethod?.invoke(clazz, name, args) as Method?
    } else {
        clazz.getDeclaredMethod(name, *parameterTypes)
    }

internal fun requireDeclaredMethod(
    clazz: Class<*>,
    name: String,
    vararg parameterTypes: Class<*>
): Method =
    checkNotNull(getDeclaredMethod(clazz, name, *parameterTypes)) {
        val params = parameterTypes.joinToString { it.simpleName }
        "Unable to obtain method $name($params) in ${clazz.name}"
    }