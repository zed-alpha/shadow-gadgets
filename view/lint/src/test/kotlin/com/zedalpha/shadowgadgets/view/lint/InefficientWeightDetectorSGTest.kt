package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.xml
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class InefficientWeightDetectorSGTest {

    @Test
    fun `ShadowsLinearLayouts error and warn of inefficient & nested weights, wrong 0dp, orientation`() {
        lint()
            .files(
                xml(
                    "res/layout/bad_weights.xml",
                    """
                    <LinearLayout
                        xmlns:android="http://schemas.android.com/apk/res/android"
                        android:layout_width="match_parent"
                        android:layout_height="match_parent">
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsLinearLayout
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content">
                    
                            <TextView
                                android:layout_width="0dp"
                                android:layout_height="match_parent"
                                android:layout_weight="1" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ShadowsLinearLayout>
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsLinearLayout
                            android:layout_width="match_parent"
                            android:layout_height="0dop"
                            android:layout_weight="1">
                    
                            <TextView
                                android:layout_width="0dp"
                                android:layout_height="match_parent"
                                android:layout_weight="1" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsLinearLayout>
                    
                    </LinearLayout>
                    """.trimIndent()
                )
            )
            .issues(
                InefficientWeightDetectorSG.BASELINE_WEIGHTS_SG,
                InefficientWeightDetectorSG.INEFFICIENT_WEIGHT_SG,
                InefficientWeightDetectorSG.NESTED_WEIGHTS_SG,
                InefficientWeightDetectorSG.ORIENTATION_SG,
                InefficientWeightDetectorSG.WRONG_0DP_SG
            )
            .run()
            .expect(
                """
                res/layout/bad_weights.xml:1: Error: Wrong orientation? No orientation specified, and the default is horizontal, yet this layout has multiple children where at least one has layout_width="match_parent" [OrientationSG]
                <LinearLayout
                 ~~~~~~~~~~~~
                res/layout/bad_weights.xml:1: Warning: Set android:baselineAligned="false" on this element for better performance [DisableBaselineAlignmentSG]
                <LinearLayout
                 ~~~~~~~~~~~~
                res/layout/bad_weights.xml:18: Warning: Use a layout_width of 0dp instead of match_parent for better performance [InefficientWeightSG]
                        android:layout_width="match_parent"
                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                res/layout/bad_weights.xml:25: Warning: Nested weights are bad for performance [NestedWeightsSG]
                            android:layout_weight="1" />
                            ~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 3 warnings"""
            )
    }
}