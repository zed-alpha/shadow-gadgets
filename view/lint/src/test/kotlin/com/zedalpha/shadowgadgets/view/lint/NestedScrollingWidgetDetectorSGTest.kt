package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.xml
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class NestedScrollingWidgetDetectorSGTest {

    @Test
    fun `ShadowsListViews and GridViews cause warnings in ScrollViews`() {
        lint()
            .files(
                xml(
                    "res/layout/nested_scrolls.xml",
                    """
                    <LinearLayout
                        xmlns:android="http://schemas.android.com/apk/res/android"
                        android:layout_width="match_parent"
                        android:layout_height="match_parent"
                        android:orientation="vertical">
                    
                        <ScrollView
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsListView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                        </ScrollView>
                    
                        <ScrollView
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsListView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                        </ScrollView>
                    
                        <ScrollView
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsGridView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                        </ScrollView>
                    
                        <ScrollView
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsGridView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                        </ScrollView>
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsListView
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsListView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ShadowsListView>
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsListView
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsListView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsListView>
                    
                    </LinearLayout>
                    """.trimIndent()
                )
            )
            .issues(NestedScrollingWidgetDetectorSG.ISSUE_SG)
            .run()
            .expect(
                """
                res/layout/nested_scrolls.xml:11: Warning: The vertically scrolling ScrollView should not contain another vertically scrolling widget (ListView) [NestedScrollingSG]
                        <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsListView
                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                res/layout/nested_scrolls.xml:21: Warning: The vertically scrolling ScrollView should not contain another vertically scrolling widget (ListView) [NestedScrollingSG]
                        <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsListView
                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                res/layout/nested_scrolls.xml:31: Warning: The vertically scrolling ScrollView should not contain another vertically scrolling widget (GridView) [NestedScrollingSG]
                        <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsGridView
                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                res/layout/nested_scrolls.xml:41: Warning: The vertically scrolling ScrollView should not contain another vertically scrolling widget (GridView) [NestedScrollingSG]
                        <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsGridView
                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                res/layout/nested_scrolls.xml:51: Warning: The vertically scrolling ListView should not contain another vertically scrolling widget (ListView) [NestedScrollingSG]
                        <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsListView
                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                res/layout/nested_scrolls.xml:61: Warning: The vertically scrolling ListView should not contain another vertically scrolling widget (ListView) [NestedScrollingSG]
                        <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsListView
                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                0 errors, 6 warnings"""
            )
    }
}