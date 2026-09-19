plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kover)
}

kotlin {
    androidLibrary {
        namespace = "com.neteinstein.whois.app"
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = libs.versions.androidMinSdk.get().toInt()
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:ui"))
            implementation(project(":feature:splash"))
            implementation(project(":feature:search"))
            implementation(project(":feature:settings"))
            // `api`, not `implementation`: :androidApp calls App() directly (see AGENTS.md) and
            // needs these on its own compile classpath at the exact version Compose Multiplatform
            // resolved, rather than redeclaring a separate androidx Compose BOM that could drift.
            api(compose.runtime)
            api(compose.foundation)
            api(compose.material3)
            api(compose.animation)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.androidx.core.ktx)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
