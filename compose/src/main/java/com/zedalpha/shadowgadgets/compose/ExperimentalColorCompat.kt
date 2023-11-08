package com.zedalpha.shadowgadgets.compose

/**
 * Marks declarations that involve color compat as experimental.
 */
@RequiresOptIn("Color compat requires @OptIn. It is currently experimental.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class ExperimentalColorCompat