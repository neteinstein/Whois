import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.ktlint) apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    plugins.withId("org.jlleitschuh.gradle.ktlint") {
        extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
            version.set("1.5.0")
            android.set(true)
            ignoreFailures.set(false)
            reporters {
                reporter(ReporterType.CHECKSTYLE)
                reporter(ReporterType.PLAIN)
            }
            filter {
                exclude("**/build/**")
                exclude("**/generated/**")
            }
        }
    }
}

dependencies {
    kover(project(":core:common"))
    kover(project(":core:ui"))
    kover(project(":feature:splash"))
    kover(project(":feature:search"))
    kover(project(":feature:settings"))
    kover(project(":composeApp"))
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*.BuildConfig",
                    "*.*\$default*",
                    "*ComposableSingletons*",
                    "*.di.*",
                    // Same rationale as `*.di.*`: declarative color/shape/typography/motion
                    // tokens, not logic - see StringsTest for why the sibling `strings` package
                    // needed a test instead of an exclude (referencing it exercises the same
                    // file's data via the Kotlin top-level-property facade class anyway).
                    "*.theme.*"
                )
                annotatedBy("androidx.compose.runtime.Composable")
            }
        }
        verify {
            rule {
                minBound(40)
            }
        }
    }
}
