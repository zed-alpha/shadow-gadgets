import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.the

class PublishConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        val libraryGroupId = findProperty("group.id")!!.toString()
        val libraryVersion = findProperty("library.version")!!.toString()

        apply(plugin = "com.android.library")
        the<LibraryExtension>().publishing {
            singleVariant("release") { withSourcesJar() }
        }

        apply(plugin = "maven-publish")
        afterEvaluate {
            the<PublishingExtension>().publications {
                create<MavenPublication>("release") {
                    from(components["release"])
                    groupId = libraryGroupId
                    artifactId = this@with.name
                    version = libraryVersion
                }
            }
        }
    }
}