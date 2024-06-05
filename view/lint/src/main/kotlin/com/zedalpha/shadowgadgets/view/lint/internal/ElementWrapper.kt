package com.zedalpha.shadowgadgets.view.lint.internal

import com.android.AndroidXConstants
import com.android.SdkConstants
import org.w3c.dom.Element
import org.w3c.dom.Node

internal class ElementWrapper(
    private val wrapped: Element,
) : Element by wrapped {

    override fun getParentNode(): Node =
        when (val node = wrapped.parentNode) {
            is Element -> ElementWrapper(node)
            else -> node
        }

    override fun getTagName(): String =
        when (val actual = wrapped.tagName) {
            // NestedScrollingWidgetDetector
            SHADOWS_LIST_VIEW -> SdkConstants.LIST_VIEW
            SHADOWS_GRID_VIEW -> SdkConstants.GRID_VIEW
            // WrongIdDetector
            SHADOWS_RELATIVE_LAYOUT -> SdkConstants.RELATIVE_LAYOUT
            SHADOWS_CONSTRAINT_LAYOUT -> AndroidXConstants.CONSTRAINT_LAYOUT.newName()
            else -> actual
        }

    override fun toString(): String {
        return wrapped.toString()
    }
}