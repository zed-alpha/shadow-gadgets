package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.WrongIdDetector
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.XmlContext
import com.zedalpha.shadowgadgets.view.lint.internal.BaseDetector
import com.zedalpha.shadowgadgets.view.lint.internal.CLIPPED_SHADOWS_CONSTRAINT_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.CLIPPED_SHADOWS_RELATIVE_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.ElementWrapper
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_CONSTRAINT_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_RELATIVE_LAYOUT
import org.w3c.dom.Attr
import org.w3c.dom.Element

class WrongIdDetectorSG : BaseDetector() {

    companion object {

        private val implementation = Implementation(
            WrongIdDetectorSG::class.java,
            Scope.RESOURCE_FILE_SCOPE
        )

        @JvmField
        val UNKNOWN_ID_SG = WrongIdDetector.UNKNOWN_ID.copy(
            Implementation(
                WrongIdDetectorSG::class.java,
                Scope.ALL_RESOURCES_SCOPE,
                Scope.RESOURCE_FILE_SCOPE
            )
        )

        @JvmField
        val NOT_SIBLING_SG = WrongIdDetector.NOT_SIBLING.copy(implementation)

        @JvmField
        val INVALID_SG = WrongIdDetector.INVALID.copy(implementation)

        @JvmField
        val UNKNOWN_ID_LAYOUT_SG =
            WrongIdDetector.UNKNOWN_ID_LAYOUT.copy(implementation)
    }

    override val detector = WrongIdDetector()

    override val issues = mapOf(
        WrongIdDetector.NOT_SIBLING to NOT_SIBLING_SG,
        WrongIdDetector.UNKNOWN_ID to UNKNOWN_ID_SG,
        WrongIdDetector.INVALID to INVALID_SG,
        WrongIdDetector.UNKNOWN_ID_LAYOUT to UNKNOWN_ID_LAYOUT_SG,
    )

    override val elements = listOf(
        SHADOWS_RELATIVE_LAYOUT,
        CLIPPED_SHADOWS_RELATIVE_LAYOUT,
        SHADOWS_CONSTRAINT_LAYOUT,
        CLIPPED_SHADOWS_CONSTRAINT_LAYOUT
    )

    override fun getApplicableAttributes(): Collection<String>? {
        return detector.getApplicableAttributes()
    }

    override fun beforeCheckFile(context: Context) {
        super.beforeCheckFile(context)
        detector.beforeCheckFile(xmlContextWrapper)
    }

    override fun visitAttribute(context: XmlContext, attribute: Attr) {
        detector.visitAttribute(xmlContextWrapper, attribute)
    }

    override fun visitElement(context: XmlContext, element: Element) {
        detector.visitElement(xmlContextWrapper, ElementWrapper(element))
    }

    override fun afterCheckFile(context: Context) {
        detector.afterCheckFile(xmlContextWrapper)
    }

    override fun afterCheckRootProject(context: Context) {
        detector.afterCheckRootProject(contextWrapper)
    }
}