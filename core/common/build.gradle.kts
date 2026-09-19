plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kover)
}

kotlin {
    androidLibrary {
        namespace = "com.neteinstein.whois.core.common"
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = libs.versions.androidMinSdk.get().toInt()

        // Both host-side (JVM) and instrumented unit tests are disabled by default under
        // com.android.kotlin.multiplatform.library - without this, commonTest only ever gets
        // compiled for the iosSimulatorArm64 target (which a Linux CI runner can't execute), so
        // `allTests` silently runs zero JVM tests. This opts into the JVM-executed "host test"
        // compilation, whose dependencies live in an androidHostTest source set if ever needed.
        withHostTestBuilder {}.configure {}
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
