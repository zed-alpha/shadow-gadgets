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
                    <LinearLayout
                        xmlns:android="http://schemas.android.com/apk/res/android"
                        android:layout_width="match_parent"
                        android:layout_height="match_parent"
                        android:orientation="vertical">
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsRelativeLayout
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
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsRelativeLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <TextView
                                android:id="@+id/text2"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                            <Button
                                android:id="@+id/button2"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:layout_alignParentEnd="true" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsRelativeLayout>
                    
                    </LinearLayout>
                    """.trimIndent()
                )
            )
            .issues(RelativeOverlapDetectorSG.ISSUE_SG)
            .run()
            .expect(
                """
                res/layout/overlap.xml:16: Warning: @id/button1 can overlap @id/text1 if @id/text1, @id/button1 grow due to localized text expansion [RelativeOverlapSG]
                        <Button
                         ~~~~~~
                res/layout/overlap.xml:33: Warning: @id/button2 can overlap @id/text2 if @id/text2, @id/button2 grow due to localized text expansion [RelativeOverlapSG]
                        <Button
                         ~~~~~~
                0 errors, 2 warnings"""
            )
    }
}