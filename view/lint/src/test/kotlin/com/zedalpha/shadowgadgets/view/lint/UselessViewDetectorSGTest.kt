package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.xml
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class UselessViewDetectorSGTest {

    @Test
    fun `Useless parent and leaf ShadowsViewGroups warnings`() {
        lint()
            .files(
                xml(
                    "res/layout/useless.xml",
                    """
                    <LinearLayout
                        xmlns:android="http://schemas.android.com/apk/res/android"
                        android:layout_width="match_parent"
                        android:layout_height="match_parent"
                        android:orientation="vertical">
                    
                        <LinearLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsLinearLayout
                                android:layout_width="match_parent"
                                android:layout_height="match_parent">
                    
                                <TextView
                                    android:layout_width="match_parent"
                                    android:layout_height="match_parent" />
                    
                            </com.zedalpha.shadowgadgets.view.viewgroup.ShadowsLinearLayout>
                    
                        </LinearLayout>
                    
                        <LinearLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsLinearLayout
                                android:layout_width="match_parent"
                                android:layout_height="match_parent">
                    
                                <TextView
                                    android:layout_width="match_parent"
                                    android:layout_height="match_parent" />
                    
                            </com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsLinearLayout>
                    
                        </LinearLayout>
                    
                        <LinearLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsLinearLayout
                                android:layout_width="match_parent"
                                android:layout_height="match_parent" />
                    
                        </LinearLayout>
                    
                        <LinearLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsLinearLayout
                                android:layout_width="match_parent"
                                android:layout_height="match_parent" />
                    
                        </LinearLayout>
                    
                    </LinearLayout>
                    """.trimIndent()
                )
            )
            .issues(
                UselessViewDetectorSG.USELESS_PARENT_SG,
                UselessViewDetectorSG.USELESS_LEAF_SG
            )
            .run()
            .expect(
                """
                res/layout/useless.xml:43: Warning: This com.zedalpha.shadowgadgets.view.viewgroup.ShadowsLinearLayout view is unnecessary (no children, no background, no id, no style) [UselessLeafSG]
                        <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsLinearLayout
                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                res/layout/useless.xml:53: Warning: This com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsLinearLayout view is unnecessary (no children, no background, no id, no style) [UselessLeafSG]
                        <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsLinearLayout
                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                res/layout/useless.xml:11: Warning: This com.zedalpha.shadowgadgets.view.viewgroup.ShadowsLinearLayout layout or its LinearLayout parent is unnecessary [UselessParentSG]
                        <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsLinearLayout
                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                res/layout/useless.xml:27: Warning: This com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsLinearLayout layout or its LinearLayout parent is unnecessary [UselessParentSG]
                        <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsLinearLayout
                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                0 errors, 4 warnings"""
            )
    }
}