package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.xml
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class ChildCountDetectorSGTest {

    @Test
    fun `ShadowsListViews and GridViews warn of layout children`() {
        lint()
            .files(
                xml(
                    "res/layout/list_view_child.xml",
                    """
                    <LinearLayout
                        xmlns:android="http://schemas.android.com/apk/res/android"
                        android:layout_width="match_parent"
                        android:layout_height="match_parent"
                        android:orientation="vertical">
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsListView
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ShadowsListView>
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsGridView
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ShadowsGridView>
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsListView
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsListView>
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsGridView
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsGridView>
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsListView
                            android:layout_width="match_parent"
                            android:layout_height="match_parent" />
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsGridView
                            android:layout_width="match_parent"
                            android:layout_height="match_parent" />
                    
                    </LinearLayout>
                    """.trimIndent()
                )
            )
            .issues(ChildCountDetectorSG.ADAPTER_VIEW_ISSUE_SG)
            .run()
            .expect(
                """
                res/layout/list_view_child.xml:7: Warning: A list/grid should have no children declared in XML [AdapterViewChildrenSG]
                    <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsListView
                     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                res/layout/list_view_child.xml:17: Warning: A list/grid should have no children declared in XML [AdapterViewChildrenSG]
                    <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsGridView
                     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                res/layout/list_view_child.xml:27: Warning: A list/grid should have no children declared in XML [AdapterViewChildrenSG]
                    <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsListView
                     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                res/layout/list_view_child.xml:37: Warning: A list/grid should have no children declared in XML [AdapterViewChildrenSG]
                    <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsGridView
                     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                0 errors, 4 warnings"""
            )
    }
}