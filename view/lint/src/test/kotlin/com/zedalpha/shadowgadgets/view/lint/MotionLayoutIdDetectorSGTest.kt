package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.xml
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class MotionLayoutIdDetectorSGTest {

    @Test
    fun `ShadowsMotionLayouts error on missing IDs`() {
        lint()
            .files(
                xml(
                    "res/layout/missing_ids.xml",
                    """
                    <LinearLayout
                        xmlns:android="http://schemas.android.com/apk/res/android"
                        android:layout_width="match_parent"
                        android:layout_height="match_parent"
                        android:orientation="vertical">
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsMotionLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ShadowsMotionLayout>
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsMotionLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <TextView
                                android:id="@+id/text1"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ShadowsMotionLayout>
                    
                    </LinearLayout>
                    """.trimIndent(),
                )
            )
            .issues(MotionLayoutIdDetectorSG.MISSING_ID_SG)
            .run()
            .expect(
                """
                res/layout/missing_ids.xml:11: Error: Views inside MotionLayout require an android:id attribute [MotionLayoutMissingIdSG]
                        <TextView
                         ~~~~~~~~
                1 errors, 0 warnings"""
            )
    }
}