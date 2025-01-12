package com.zedalpha.shadowgadgets.compose

/**
 * Marks declarations that involve color compat as experimental.
 *
 * This is @OptIn only because the current implementation requires a separate
 * color layer for each shadow. The API is frozen, however, and this feature is
 * as solid as the clip, so if the overhead is acceptable for a given setup,
 * there shouldn't be any other issues.
 */
@Deprecated("Opt-in requirement for color compat has been removed.")
@RequiresOptIn("Color compat requires @OptIn. It is currently experimental.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
public annotation class ExperimentalColorCompat