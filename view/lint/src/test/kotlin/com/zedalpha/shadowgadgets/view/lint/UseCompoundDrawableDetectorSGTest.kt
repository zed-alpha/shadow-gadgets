package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.xml
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class UseCompoundDrawableDetectorSGTest {

    @Test
    fun `ShadowsLinearLayouts can be a TextView with a Drawable`() {
        lint()
            .files(
                xml(
                    "res/layout/compound.xml",
                    """
                    <LinearLayout
                        xmlns:android="http://schemas.android.com/apk/res/android"
                        android:layout_width="match_parent"
                        android:layout_height="match_parent"
                        android:orientation="vertical">
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsLinearLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <TextView
                                android:layout_width="match_parent"
                                android:layout_height="match_parent" />
                    
                            <ImageView
                                android:layout_width="match_parent"
                                android:layout_height="match_parent"
                                android:contentDescription="@string/app_name" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ShadowsLinearLayout>
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsLinearLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <TextView
                                android:layout_width="match_parent"
                                android:layout_height="match_parent" />
                    
                            <ImageView
                                android:layout_width="match_parent"
                                android:layout_height="match_parent"
                                android:contentDescription="@string/app_name" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsLinearLayout>
                    
                    </LinearLayout>
                    """.trimIndent()
                )
            )
            .issues(UseCompoundDrawableDetectorSG.ISSUE_SG)
            .run()
            .expect(
                """
                res/layout/compound.xml:7: Warning: This tag and its children can be replaced by one <TextView/> and a compound drawable [UseCompoundDrawablesSG]
                    <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsLinearLayout
                     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                res/layout/compound.xml:22: Warning: This tag and its children can be replaced by one <TextView/> and a compound drawable [UseCompoundDrawablesSG]
                    <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsLinearLayout
                     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                0 errors, 2 warnings"""
            )
    }
}