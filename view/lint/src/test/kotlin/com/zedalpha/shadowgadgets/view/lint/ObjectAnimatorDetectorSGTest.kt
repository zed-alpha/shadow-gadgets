package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.gradle
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestFiles.xml
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class ObjectAnimatorDetectorSGTest {

    @Test
    fun `ShadowsMotionLayout warns on missing @Keep for motion scene attributes`() {
        lint()
            .files(
                xml(
                    "src/main/res/layout/missing_keep.xml",
                    """
                    <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsMotionLayout
                        xmlns:android="http://schemas.android.com/apk/res/android"
                        xmlns:app="http://schemas.android.com/apk/res-auto"
                        android:layout_width="match_parent"
                        android:layout_height="match_parent"
                        app:layoutDescription="@xml/missing_keep_scene">
                    
                        <com.zedalpha.test.TestTextView
                            android:id="@+id/test_text_view"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content" />
                    
                    </com.zedalpha.shadowgadgets.view.viewgroup.ShadowsMotionLayout>
                    """.trimIndent()
                ),
                xml(
                    "src/main/res/xml/missing_keep_scene.xml",
                    """
                    <MotionScene xmlns:android="http://schemas.android.com/apk/res/android"
                        xmlns:app="http://schemas.android.com/apk/res-auto">
                    
                        <Constraint
                            android:id="@id/test_text_view"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content">
                    
                            <CustomAttribute
                                app:attributeName="firstColor"
                                app:customColorValue="#FFFFFF" />
                    
                            <CustomAttribute
                                app:attributeName="secondColor"
                                app:customColorValue="#000000" />
                                
                        </Constraint>
                    
                    </MotionScene>
                    """.trimIndent()
                ),
                kotlin(
                    """
                    package androidx.annotation
                    
                    annotation class Keep
                    """.trimIndent()
                ),
                kotlin(
                    """
                    package com.zedalpha.test
                    
                    import androidx.annotation.Keep
                    
                    class TestTextView @JvmOverloads constructor(
                        context: Context,
                        attrs: AttributeSet? = null
                    ) : TextView(context, attrs) {
                    
                        @get:Keep
                        @set:Keep
                        var firstColor: int = 0
                    
                        var secondColor: Int = 0
                    }
                    """.trimIndent()
                ),
                gradle(
                    """
                    android {
                        buildTypes {
                            release {
                                minifyEnabled true
                            }
                        }
                    }
                    """.trimIndent()
                )
            )
            .issues(ObjectAnimatorDetectorSG.MISSING_KEEP_SG)
            .showSecondaryLintContent(true)
            .run()
            .expect(
                """
                src/main/res/xml/missing_keep_scene.xml:14: Warning: This attribute references a method or property in custom view com.zedalpha.test.TestTextView which is not annotated with @Keep; it should be annotated with @Keep to ensure that it is not discarded or renamed in release builds [AnimatorKeepSG]
                            app:attributeName="secondColor"
                                               ~~~~~~~~~~~
                    src/main/kotlin/com/zedalpha/test/TestTextView.kt:14: This method is accessed via reflection from a MotionScene (missing_keep_scene) so it should be annotated with @Keep to ensure that it is not discarded or renamed in release builds
                    var secondColor: Int = 0
                    ~~~~~~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings"""
            )
    }
}