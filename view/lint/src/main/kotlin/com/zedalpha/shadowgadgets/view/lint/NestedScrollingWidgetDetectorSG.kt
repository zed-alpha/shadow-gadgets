package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.NestedScrollingWidgetDetector
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.XmlContext
import com.zedalpha.shadowgadgets.view.lint.internal.BaseDetector
import com.zedalpha.shadowgadgets.view.lint.internal.CLIPPED_SHADOWS_GRID_VIEW
import com.zedalpha.shadowgadgets.view.lint.internal.CLIPPED_SHADOWS_LIST_VIEW
import com.zedalpha.shadowgadgets.view.lint.internal.ElementWrapper
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_GRID_VIEW
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_LIST_VIEW
import org.w3c.dom.Element

class NestedScrollingWidgetDetectorSG : BaseDetector() {

    companion object {

        @JvmField
        val ISSUE_SG = NestedScrollingWidgetDetector.ISSUE.copy(
            Implementation(
                NestedScrollingWidgetDetectorSG::class.java,
                Scope.RESOURCE_FILE_SCOPE
            )
        )
    }

    override val detector = NestedScrollingWidgetDetector()

    override val issues = mapOf(
        NestedScrollingWidgetDetector.ISSUE to ISSUE_SG
    )

    override val elements = buildList {
        detector.getApplicableElements()?.let { addAll(it) }
        add(SHADOWS_GRID_VIEW)
        add(CLIPPED_SHADOWS_GRID_VIEW)
        add(SHADOWS_LIST_VIEW)
        add(CLIPPED_SHADOWS_LIST_VIEW)
    }

    override fun beforeCheckFile(context: Context) {
        super.beforeCheckFile(context)
        detector.beforeCheckFile(xmlContextWrapper)
    }

    private lateinit var elementWrapper: ElementWrapper

    override fun visitElement(context: XmlContext, element: Element) {
        val wrapped = ElementWrapper(element).also { elementWrapper = it }
        detector.visitElement(xmlContextWrapper, wrapped)
    }

    override fun visitElementAfter(context: XmlContext, element: Element) {
        detector.visitElementAfter(xmlContextWrapper, elementWrapper)
    }
}