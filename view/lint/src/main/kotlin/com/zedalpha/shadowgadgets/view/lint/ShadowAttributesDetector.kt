package com.zedalpha.shadowgadgets.view.lint

import com.android.SdkConstants.ANDROID_URI
import com.android.SdkConstants.ATTR_ID
import com.android.SdkConstants.AUTO_URI
import com.android.SdkConstants.CLASS_VIEW
import com.android.SdkConstants.NEW_ID_PREFIX
import com.android.SdkConstants.TAG_INCLUDE
import com.android.SdkConstants.VIEW
import com.android.SdkConstants.VIEW_MERGE
import com.android.tools.lint.checks.ViewTypeDetector.Companion.findViewForTag
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.LayoutDetector
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.XmlContext
import com.android.tools.lint.detector.api.isLayoutMarkerTag
import com.android.utils.iterator
import com.zedalpha.shadowgadgets.view.lint.internal.ALL_CHILD_SHADOW_ATTRIBUTES
import com.zedalpha.shadowgadgets.view.lint.internal.ALL_LIBRARY_VIEW_GROUPS
import org.w3c.dom.Element

class ShadowAttributesDetector : LayoutDetector() {

    companion object {

        @JvmField
        val MISSING_ID = Issue.create(
            id = "MissingIdWithShadowAttributes",
            briefDescription = "Missing ID on child with shadow attributes in a ShadowsViewGroup",
            explanation = """
                An android:id is necessary on children of ShadowsViewGroups in order to enable
                their individual shadow attributes. Views without IDs can still have shadow
                properties set through their parents' tags, but their own attributes will be
                ignored by the ShadowsViewGroup.""",
            category = Category.CORRECTNESS,
            priority = 7,
            severity = Severity.ERROR,
            implementation = Implementation(
                ShadowAttributesDetector::class.java,
                Scope.RESOURCE_FILE_SCOPE
            )
        )
    }

    override fun getApplicableElements() = ALL_LIBRARY_VIEW_GROUPS

    override fun visitElement(context: XmlContext, element: Element) {
        for (child in element) {
            if (child.isViewTag(context) &&
                child.hasChildShadowAttributes() &&
                !child.hasAttributeNS(ANDROID_URI, ATTR_ID)
            ) {
                context.report(
                    MISSING_ID,
                    context.getNameLocation(child),
                    "This ${child.tagName} requires an android:id to enable its shadow attributes",
                    fix().set().todo(ANDROID_URI, ATTR_ID, NEW_ID_PREFIX)
                        .build()
                )
            }
        }
    }
}

private fun Element.isViewTag(context: XmlContext): Boolean {
    val tagName = tagName
    return when {
        tagName == VIEW -> true
        tagName == TAG_INCLUDE || tagName == VIEW_MERGE -> false
        isLayoutMarkerTag(this) -> false
        context.sdkInfo.getParentViewName(tagName) != null -> true
        tagName.indexOf('.') <= 0 -> false
        else -> {
            val evaluator = context.client
                .getUastParser(context.project).evaluator
            val view = findViewForTag(tagName, evaluator)
            return view != null && evaluator.extendsClass(view, CLASS_VIEW)
        }
    }
}

private fun Element.hasChildShadowAttributes() =
    ALL_CHILD_SHADOW_ATTRIBUTES.any { hasAttributeNS(AUTO_URI, it) }