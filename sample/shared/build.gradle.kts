// The demo's shared KMP library — every app shell in this directory (androidApp, desktopApp,
// webApp, iosApp) is a thin wrapper around `DemoApp()` from here.
//
// Ported from the sidekick repo's Pokédex demo. The debug-overlay SDK, its Gradle plugin, the
// KSP-generated preferences store and the Room cache have all been stripped: this sample exists
// to show off Barz's navigation, and none of that machinery served that.
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    androidLibrary {
        namespace = "dev.parez.barz.sample"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    jvm()
    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class) applyDefaultHierarchyTemplate()

    sourceSets {
        // `BrowserHistoryEffect` has a real implementation on web and a no-op everywhere else.
        val nonWebMain by creating { dependsOn(commonMain.get()) }
        androidMain.get().dependsOn(nonWebMain)
        jvmMain.get().dependsOn(nonWebMain)
        iosMain.get().dependsOn(nonWebMain)

        commonMain.dependencies {
            implementation(projects.barz)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.material.iconsExtended)
            implementation(libs.compose.ui.tooling.preview)

            implementation(libs.kermit)
            implementation(libs.haze)
            implementation(libs.haze.blur)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinxJson)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodelCompose)

            implementation(libs.compose.adaptive)
            implementation(libs.compose.adaptive.layout)
            implementation(libs.compose.adaptive.navigation3)
            implementation(libs.navigation3.runtime)
            // Force over the older abi the adaptive-navigation3 graph pulls transitively.
            implementation(libs.navigationevent.compose)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
        }
        jvmMain.dependencies { implementation(libs.ktor.client.cio) }
        iosMain.dependencies { implementation(libs.ktor.client.darwin) }
        val webMain by getting {
            dependencies {
                implementation(libs.navigation3.browser)
                // See the comment on androidxLifecycleAosp in libs.versions.toml — without these
                // the Wasm bundle fails to instantiate.
                implementation(libs.androidx.lifecycle.runtime.aosp)
                implementation(libs.androidx.lifecycle.common.aosp)
                implementation(libs.androidx.lifecycle.runtimeCompose.aosp)
                implementation(libs.androidx.savedstate.aosp)
                implementation(libs.androidx.savedstate.compose.aosp)
                implementation(libs.androidx.navigationevent.aosp)
                implementation(libs.androidx.navigationevent.compose.aosp)
            }
        }
        jsMain.dependencies { implementation(libs.ktor.client.js) }
        wasmJsMain.dependencies { implementation(libs.ktor.client.js) }
    }
}
