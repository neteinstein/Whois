plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kover)
}

kotlin {
    androidLibrary {
        namespace = "com.neteinstein.whois.core.common"
        compileSdk =
            libs.versions.androidCompileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.androidMinSdk
                .get()
                .toInt()
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
