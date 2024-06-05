package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.xml
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class ShadowAttributesDetectorTest {

    @Test
    fun `ShadowsViewGroups error on children without IDs`() {
        lint()
            .files(
                xml(
                    "res/layout/shadow_missing_id.xml",
                    """
                    <LinearLayout
                        xmlns:android="http://schemas.android.com/apk/res/android"
                        xmlns:app="http://schemas.android.com/apk/res-auto"
                        android:layout_width="match_parent"
                        android:layout_height="match_parent"
                        android:orientation="vertical">
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsFrameLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                app:clipOutlineShadow="true" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ShadowsFrameLayout>
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsLinearLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <TextView
                                android:id="@+id/correct"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                app:clipOutlineShadow="true" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ShadowsLinearLayout>
                    
                    </LinearLayout>
                    """.trimIndent()
                )
            )
            .issues(ShadowAttributesDetector.MISSING_ID)
            .run()
            .expect(
                """
                res/layout/shadow_missing_id.xml:12: Error: This TextView requires an android:id to enable its shadow attributes [MissingIdWithShadowAttributes]
                        <TextView
                         ~~~~~~~~
                1 errors, 0 warnings"""
            )
    }
}