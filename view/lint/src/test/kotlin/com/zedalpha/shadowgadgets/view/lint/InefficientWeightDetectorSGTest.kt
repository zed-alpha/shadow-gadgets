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
                        android:layout_height="match_parent"
                        android:baselineAligned="false"
                        android:orientation="horizontal">
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsLinearLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent"
                            android:layout_weight="1">
                    
                            <TextView
                                android:layout_width="0dp"
                                android:layout_height="match_parent"
                                android:layout_weight="1" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ShadowsLinearLayout>
                    
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
                res/layout/bad_weights.xml:9: Warning: Use a layout_width of 0dp instead of match_parent for better performance [InefficientWeightSG]
                        android:layout_width="match_parent"
                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                res/layout/bad_weights.xml:16: Warning: Nested weights are bad for performance [NestedWeightsSG]
                            android:layout_weight="1" />
                            ~~~~~~~~~~~~~~~~~~~~~~~~~
                0 errors, 2 warnings"""
            )
    }
}