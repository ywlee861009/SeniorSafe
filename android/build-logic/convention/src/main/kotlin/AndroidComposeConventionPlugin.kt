import com.android.build.gradle.LibraryExtension
import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            // library 또는 application 중 적용된 쪽에 compose 활성화
            extensions.findByType<LibraryExtension>()?.buildFeatures?.compose = true
            extensions.findByType<ApplicationExtension>()?.buildFeatures?.compose = true

            // android 플러그인이 나중에 적용될 경우를 대비해 afterEvaluate 없이 pluginManager 콜백 사용
            pluginManager.withPlugin("com.android.library") {
                extensions.findByType<LibraryExtension>()?.buildFeatures?.compose = true
            }
            pluginManager.withPlugin("com.android.application") {
                extensions.findByType<ApplicationExtension>()?.buildFeatures?.compose = true
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            dependencies {
                val bom = libs.findLibrary("compose-bom").get()
                add("implementation", platform(bom))
                add("implementation", libs.findLibrary("compose-ui").get())
                add("implementation", libs.findLibrary("compose-ui-tooling-preview").get())
                add("implementation", libs.findLibrary("compose-material3").get())
                add("implementation", libs.findLibrary("compose-material-icons").get())
                add("debugImplementation", libs.findLibrary("compose-ui-tooling").get())
            }
        }
    }
}
