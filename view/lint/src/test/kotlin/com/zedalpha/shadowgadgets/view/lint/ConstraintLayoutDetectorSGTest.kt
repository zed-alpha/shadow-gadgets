package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.xml
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class ConstraintLayoutDetectorSGTest {

    @Test
    fun `ShadowsConstraintLayouts and MotionLayouts error on children without constraints`() {
        lint()
            .files(
                xml(
                    "res/layout/missing_constraints.xml",
                    """
                    <LinearLayout
                        xmlns:android="http://schemas.android.com/apk/res/android"
                        android:layout_width="match_parent"
                        android:layout_height="match_parent"
                        android:orientation="vertical">
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsConstraintLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ShadowsConstraintLayout>
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsMotionLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ShadowsMotionLayout>
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsConstraintLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsConstraintLayout>
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsMotionLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsMotionLayout>
                    
                    </LinearLayout>
                    """.trimIndent()
                )
            )
            .issues(ConstraintLayoutDetectorSG.ISSUE_SG)
            .run()
            .expect(
                """
                res/layout/missing_constraints.xml:11: Error: This view is not constrained. It only has designtime positions, so it will jump to (0,0) at runtime unless you add the constraints [MissingConstraintsSG]
                        <TextView
                         ~~~~~~~~
                res/layout/missing_constraints.xml:21: Error: This view is not constrained. It only has designtime positions, so it will jump to (0,0) at runtime unless you add the constraints [MissingConstraintsSG]
                        <TextView
                         ~~~~~~~~
                res/layout/missing_constraints.xml:31: Error: This view is not constrained. It only has designtime positions, so it will jump to (0,0) at runtime unless you add the constraints [MissingConstraintsSG]
                        <TextView
                         ~~~~~~~~
                res/layout/missing_constraints.xml:41: Error: This view is not constrained. It only has designtime positions, so it will jump to (0,0) at runtime unless you add the constraints [MissingConstraintsSG]
                        <TextView
                         ~~~~~~~~
                4 errors, 0 warnings"""
            )
    }
}