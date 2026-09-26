package com.zedalpha.shadowgadgets.move

import com.android.tools.lint.checks.infrastructure.TestFiles.gradleToml
import com.android.tools.lint.checks.infrastructure.TestFiles.kts
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class MoveAlertDetectorTest {

    @Test
    fun testWarnings() {
        lint()
            .files(
                kts(
                    "build.gradle.kts",
                    """
                    dependencies {
                        implementation("com.zedalpha.shadowgadgets:local:2.5.1")
                        implementation("com.github.zed-alpha.shadow-gadgets:view:2.5.1")
                        implementation("com.other.library:module:0.0.0")
                    }
                    """.trimIndent()
                ),
                gradleToml(
                    """
                    [versions]
                    shadowGadgets="2.5.1"
                    other="0.0.0"
                    [libraries]
                    sg-local = { module = "com.github.zedalpha.shadowgadgets:local", version.ref = "shadowGadgets" }
                    shadow-gadgets = { module = "com.github.zed-alpha.shadow-gadgets:view", version.ref = "shadowGadgets" }
                    other-thing = { module = "com.other.library:module", version.ref = "other" }
                    """.trimIndent()
                )
            )
            .issues(MoveAlertDetector.ISSUE)
            .run()
            .expect(
                """
                build.gradle.kts:3: Hint: Shadow Gadgets moved to Maven Central: https://github.com/zed-alpha/shadow-gadgets#download [ShadowGadgetsMoved]
                    implementation("com.github.zed-alpha.shadow-gadgets:view:2.5.1")
                                   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                ../gradle/libs.versions.toml:6: Hint: Shadow Gadgets moved to Maven Central: https://github.com/zed-alpha/shadow-gadgets#download [ShadowGadgetsMoved]
                shadow-gadgets = { module = "com.github.zed-alpha.shadow-gadgets:view", version.ref = "shadowGadgets" }
                                  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                0 errors, 0 warnings, 2 hints
                """.trimIndent()
            )
    }

    @Test
    fun testNoWarnings() {
        lint()
            .files(
                kts(
                    "build.gradle.kts",
                    """
                    dependencies {
                        implementation("com.zedalpha.shadowgadgets:view:2.5.1")
                        implementation("com.other.library:module:0.0.0")
                    }
                    """.trimIndent()
                ),
                gradleToml(
                    """
                    [versions]
                    shadowGadgets="2.5.1"
                    other="0.0.0"
                    [libraries]
                    sg-local = { module = "com.github.zedalpha.shadowgadgets:view", version.ref = "shadowGadgets" }
                    other-thing = { module = "com.other.library:module", version.ref = "other" }
                    """.trimIndent()
                )
            )
            .issues(MoveAlertDetector.ISSUE)
            .run()
            .expectClean()
    }
}