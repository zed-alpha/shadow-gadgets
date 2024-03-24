package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.xml
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class MotionLayoutDetectorSGTest {

    @Test
    fun `ShadowsMotionLayouts with valid MotionScene and no warnings`() {
        lint()
            .files(
                xml("res/xml/motion_scene.xml", "<MotionScene/>"),
                xml(
                    "res/layout/valid_motion_scene.xml",
                    """
                    <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsMotionLayout
                        xmlns:android="http://schemas.android.com/apk/res/android"
                        xmlns:app="http://schemas.android.com/apk/res-auto"
                        android:id="@+id/motionLayout"
                        app:layoutDescription="@xml/motion_scene"
                        android:layout_width="match_parent"
                        android:layout_height="match_parent" />
                    """.trimIndent()
                ),
            )
            .issues(MotionLayoutDetectorSG.INVALID_SCENE_FILE_REFERENCE_SG)
            .run()
            .expectClean()
    }

    @Test
    fun `ShadowsMotionLayouts error on missing & invalid layoutDescriptions, nonexistent motion scenes`() {
        lint()
            .files(
                xml(
                    "res/values/strings.xml",
                    """
                    <resources>
                        <string name="not_a_motion_scene">Duh.</string>
                    </resources>
                    """.trimIndent()
                ),
                xml(
                    "res/layout/invalid_motion_scene.xml",
                    """
                    <LinearLayout
                        xmlns:android="http://schemas.android.com/apk/res/android"
                        xmlns:app="http://schemas.android.com/apk/res-auto"
                        android:layout_width="match_parent"
                        android:layout_height="match_parent">
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsMotionLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent" />
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsMotionLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent" />
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsMotionLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent"
                            app:layoutDescription="@string/not_a_motion_scene" />
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsMotionLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent"
                            app:layoutDescription="@string/not_a_motion_scene" />
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsMotionLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent"
                            app:layoutDescription="@xml/missing_scene" />
                    
                        <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsMotionLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent"
                            app:layoutDescription="@xml/missing_scene2" />
                    
                    </LinearLayout>
                    """.trimIndent()
                )
            )
            .issues(MotionLayoutDetectorSG.INVALID_SCENE_FILE_REFERENCE_SG)
            .run()
            .expect(
                """
                res/layout/invalid_motion_scene.xml:7: Error: The attribute: layoutDescription is missing [MotionLayoutInvalidSceneFileReferenceSG]
                    <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsMotionLayout
                     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                res/layout/invalid_motion_scene.xml:11: Error: The attribute: layoutDescription is missing [MotionLayoutInvalidSceneFileReferenceSG]
                    <com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsMotionLayout
                     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                res/layout/invalid_motion_scene.xml:18: Error: @string/not_a_motion_scene is an invalid value for layoutDescription [MotionLayoutInvalidSceneFileReferenceSG]
                        app:layoutDescription="@string/not_a_motion_scene" />
                                               ~~~~~~~~~~~~~~~~~~~~~~~~~~
                res/layout/invalid_motion_scene.xml:23: Error: @string/not_a_motion_scene is an invalid value for layoutDescription [MotionLayoutInvalidSceneFileReferenceSG]
                        app:layoutDescription="@string/not_a_motion_scene" />
                                               ~~~~~~~~~~~~~~~~~~~~~~~~~~
                res/layout/invalid_motion_scene.xml:28: Error: The motion scene file: @xml/missing_scene doesn't exist [MotionLayoutInvalidSceneFileReferenceSG]
                        app:layoutDescription="@xml/missing_scene" />
                                               ~~~~~~~~~~~~~~~~~~
                res/layout/invalid_motion_scene.xml:33: Error: The motion scene file: @xml/missing_scene2 doesn't exist [MotionLayoutInvalidSceneFileReferenceSG]
                        app:layoutDescription="@xml/missing_scene2" />
                                               ~~~~~~~~~~~~~~~~~~~
                6 errors, 0 warnings"""
            )
    }
}