package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.xml
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class RelativeOverlapDetectorSGTest {

    @Test
    fun `ShadowsRelativeLayouts warn of overlapping children`() {
        lint()
            .files(
                xml(
                    "res/layout/overlap.xml",
                    """
                    <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsRelativeLayout
                        xmlns:android="http://schemas.android.com/apk/res/android"
                        android:layout_width="match_parent"
                        android:layout_height="match_parent">
                    
                        <TextView
                            android:id="@+id/text1"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content" />
                    
                        <Button
                            android:id="@+id/button1"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_alignParentEnd="true" />
                    
                    </com.zedalpha.shadowgadgets.view.viewgroup.ShadowsRelativeLayout>
                    """.trimIndent()
                )
            )
            .issues(RelativeOverlapDetectorSG.ISSUE_SG)
            .run()
            .expect(
                """
                res/layout/overlap.xml:11: Warning: @id/button1 can overlap @id/text1 if @id/text1, @id/button1 grow due to localized text expansion [RelativeOverlapSG]
                    <Button
                     ~~~~~~
                0 errors, 1 warnings"""
            )
    }
}