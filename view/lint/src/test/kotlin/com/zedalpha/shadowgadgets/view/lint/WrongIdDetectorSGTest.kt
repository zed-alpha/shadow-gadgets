package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.xml
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class WrongIdDetectorSGTest {

    @Test
    fun `ShadowsRelativeLayouts error on unknown IDs`() {
        lint()
            .files(
                xml(
                    "res/layout/unknown_ids.xml",
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
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:layout_above="@id/unknown1" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ShadowsRelativeLayout>
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsRelativeLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:layout_above="@id/unknown2" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsRelativeLayout>
                    
                    </LinearLayout>
                    """.trimIndent()
                )
            )
            .issues(WrongIdDetectorSG.UNKNOWN_ID_SG)
            .run()
            .expect(
                """
                res/layout/unknown_ids.xml:14: Error: The id "unknown1" is not defined anywhere. [UnknownIdSG]
                            android:layout_above="@id/unknown1" />
                            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                res/layout/unknown_ids.xml:25: Error: The id "unknown2" is not defined anywhere. [UnknownIdSG]
                            android:layout_above="@id/unknown2" />
                            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                2 errors, 0 warnings"""
            )
    }

    @Test
    fun `ShadowsRelativeLayouts error on non-sibling IDs`() {
        lint()
            .files(
                xml(
                    "res/layout/unknown_ids.xml",
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
                                android:id="@+id/sibling1"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:layout_above="@id/sibling1"
                                android:layout_below="@id/uncle" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ShadowsRelativeLayout>
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsRelativeLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                            
                            <TextView
                                android:id="@+id/sibling2"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:layout_above="@id/sibling2"
                                android:layout_below="@id/uncle" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsRelativeLayout>
                        
                        <TextView
                                android:id="@+id/uncle"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                    </LinearLayout>
                    """.trimIndent()
                )
            )
            .issues(WrongIdDetectorSG.NOT_SIBLING_SG)
            .run()
            .expect(
                """
                res/layout/unknown_ids.xml:20: Error: @id/uncle is not a sibling in the same RelativeLayout [NotSiblingSG]
                            android:layout_below="@id/uncle" />
                            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                res/layout/unknown_ids.xml:37: Error: @id/uncle is not a sibling in the same RelativeLayout [NotSiblingSG]
                            android:layout_below="@id/uncle" />
                            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                2 errors, 0 warnings"""
            )
    }

    @Test
    fun `ShadowsRelativeLayouts error on invalid IDs`() {
        lint()
            .files(
                xml(
                    "res/layout/unknown_ids.xml",
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
                                android:id="@+id/"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                            <TextView
                                android:id="@+id/foo/bar"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                            <TextView
                                android:id="@foo/bar"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                            <TextView
                                android:id="@string/foo"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ShadowsRelativeLayout>
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsRelativeLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent">
                    
                            <TextView
                                android:id="@+id/"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                            <TextView
                                android:id="@+id/foo/bar"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                            <TextView
                                android:id="@foo/bar"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                            <TextView
                                android:id="@string/foo"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content" />
                    
                        </com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsRelativeLayout>
                    
                    </LinearLayout>
                    """.trimIndent()
                )
            )
            .issues(WrongIdDetectorSG.INVALID_SG)
            .run()
            .expect(
                """
                res/layout/unknown_ids.xml:12: Error: Invalid id: missing value [InvalidIdSG]
                            android:id="@+id/"
                            ~~~~~~~~~~~~~~~~~~
                res/layout/unknown_ids.xml:17: Error: ID definitions must be of the form @+id/name; try using @+id/foo_bar [InvalidIdSG]
                            android:id="@+id/foo/bar"
                            ~~~~~~~~~~~~~~~~~~~~~~~~~
                res/layout/unknown_ids.xml:22: Error: Invalid id; ID definitions must be of the form @+id/name [InvalidIdSG]
                            android:id="@foo/bar"
                            ~~~~~~~~~~~~~~~~~~~~~
                res/layout/unknown_ids.xml:27: Error: Invalid id; ID definitions must be of the form @+id/name; did you mean @+id/foo? [InvalidIdSG]
                            android:id="@string/foo"
                            ~~~~~~~~~~~~~~~~~~~~~~~~
                res/layout/unknown_ids.xml:38: Error: Invalid id: missing value [InvalidIdSG]
                            android:id="@+id/"
                            ~~~~~~~~~~~~~~~~~~
                res/layout/unknown_ids.xml:43: Error: ID definitions must be of the form @+id/name; try using @+id/foo_bar [InvalidIdSG]
                            android:id="@+id/foo/bar"
                            ~~~~~~~~~~~~~~~~~~~~~~~~~
                res/layout/unknown_ids.xml:48: Error: Invalid id; ID definitions must be of the form @+id/name [InvalidIdSG]
                            android:id="@foo/bar"
                            ~~~~~~~~~~~~~~~~~~~~~
                res/layout/unknown_ids.xml:53: Error: Invalid id; ID definitions must be of the form @+id/name; did you mean @+id/foo? [InvalidIdSG]
                            android:id="@string/foo"
                            ~~~~~~~~~~~~~~~~~~~~~~~~
                8 errors, 0 warnings"""
            )
    }
}