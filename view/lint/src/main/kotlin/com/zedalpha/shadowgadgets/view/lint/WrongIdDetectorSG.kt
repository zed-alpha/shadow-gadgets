package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.WrongIdDetector
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.XmlContext
import com.zedalpha.shadowgadgets.view.lint.internal.BaseDetector
import com.zedalpha.shadowgadgets.view.lint.internal.ElementWrapper
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_CONSTRAINT_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_RELATIVE_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.copy
import org.w3c.dom.Attr
import org.w3c.dom.Element

class WrongIdDetectorSG : BaseDetector<WrongIdDetector>(::WrongIdDetector) {

    companion object {

        @JvmField
        val UNKNOWN_ID_SG: Issue =
            WrongIdDetector.UNKNOWN_ID.copy(WrongIdDetectorSG::class.java)

        @JvmField
        val NOT_SIBLING_SG: Issue =
            WrongIdDetector.NOT_SIBLING.copy(WrongIdDetectorSG::class.java)

        @JvmField
        val INVALID_SG: Issue =
            WrongIdDetector.INVALID.copy(WrongIdDetectorSG::class.java)

        @JvmField
        val UNKNOWN_ID_LAYOUT_SG: Issue =
            WrongIdDetector.UNKNOWN_ID_LAYOUT.copy(WrongIdDetectorSG::class.java)
    }

    override val issues: Map<Issue, Issue> =
        mapOf(
            WrongIdDetector.NOT_SIBLING to NOT_SIBLING_SG,
            WrongIdDetector.UNKNOWN_ID to UNKNOWN_ID_SG,
            WrongIdDetector.INVALID to INVALID_SG,
            WrongIdDetector.UNKNOWN_ID_LAYOUT to UNKNOWN_ID_LAYOUT_SG,
        )

    override val elements: Collection<String> =
        listOf(SHADOWS_RELATIVE_LAYOUT, SHADOWS_CONSTRAINT_LAYOUT)

    override fun getApplicableAttributes(): Collection<String>? =
        detector.getApplicableAttributes()

    override fun beforeCheckFile(context: Context) {
        super.beforeCheckFile(context)
        detector.beforeCheckFile(xmlContextWrapper)
    }

    override fun visitAttribute(context: XmlContext, attribute: Attr) =
        detector.visitAttribute(xmlContextWrapper, attribute)

    override fun visitElement(context: XmlContext, element: Element) =
        detector.visitElement(xmlContextWrapper, ElementWrapper(element))

    override fun afterCheckFile(context: Context) =
        detector.afterCheckFile(xmlContextWrapper)

    override fun afterCheckRootProject(context: Context) =
        detector.afterCheckRootProject(contextWrapper)
}