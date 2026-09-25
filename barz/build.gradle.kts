import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.mavenPublish)
}

kotlin {
    android {
        namespace = "dev.parez.barz"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
            }
        }
    }

    jvm()

    // Calling this is what enables ABI validation as of Kotlin 2.4 — the `enabled` property it
    // used to take was removed. The dump under barz/api/ is committed so a change to the public
    // API shows up as a diff in review.
    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation {}

    // Tests are off on the web targets, deliberately. Karma needs a local Chrome install, and
    // Node cannot host them either because Compose's web runtime loads Skiko's .wasm over XHR.
    // The code under test lives in commonMain and is exercised by the jvm, android and iOS runs,
    // so the web targets are compile-verified rather than untested.
    js { browser { testTask { enabled = false } } }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { browser { testTask { enabled = false } } }

    // iosX64 (the Intel simulator) is deliberately absent: Apple has wound Intel Macs down, and
    // several Compose Multiplatform artifacts have already stopped publishing a uikitX64 variant.
    // Carrying it would pin this library to older dependencies for a target nobody ships to.
    iosArm64()
    iosSimulatorArm64()

    // Required now that nonIosMain declares manual dependsOn edges: those switch off the
    // automatic application of the default hierarchy, and without it iosMain is never wired.
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate()

    sourceSets {
        // One no-op actual instead of four byte-identical copies. Only iOS has a native bar; every
        // other target's NativeIosBar is unreachable behind `supportsNativeBar`.
        val nonIosMain by creating { dependsOn(commonMain.get()) }
        androidMain.get().dependsOn(nonIosMain)
        jvmMain.get().dependsOn(nonIosMain)
        jsMain.get().dependsOn(nonIosMain)
        wasmJsMain.get().dependsOn(nonIosMain)

        commonMain.dependencies {
            // `api`, not `implementation`: these types appear in this library's public signatures
            // (@Composable, Modifier, Color, DrawableResource), so consumers must see them.
            api(libs.compose.runtime)
            api(libs.compose.ui)
            api(libs.compose.material3)
            api(libs.compose.components.resources)

            // api, not implementation: ColumnScope is androidx.compose.foundation.layout and it
            // appears in AdaptiveNavigationScaffold's public `header` signature. It only resolves
            // for consumers today because material3 happens to api foundation transitively.
            api(libs.compose.foundation)
            // Internal only — Barz wraps NavigationSuiteScaffoldLayout behind its own API surface.
            implementation(libs.compose.material3.adaptive.navigation.suite)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// Compose 1.12 added a check that fails the build when a web target has Compose UI tests but no
// webpack bundle to load Skiko from. Barz's web test tasks are disabled (see the note on the js
// target above), so there is nothing for it to guard — and satisfying it would mean declaring an
// executable binary on a library.
tasks.matching { it.name.startsWith("checkComposeUiTestConfigurationFor") }.configureEach {
    enabled = false
}

mavenPublishing {
    // Coordinates, POM and version come from gradle.properties (GROUP / VERSION_NAME / POM_*).
    configure(
        KotlinMultiplatform(
            // Central requires a javadoc jar; an empty one satisfies it. Dokka would pull
            // rendering artifacts that are not in the local cache, so it stays opt-in.
            javadocJar = JavadocJar.Empty(),
        ),
    )
    publishToMavenCentral()
    // In-memory signing only. The signing plugin's useGpgCmd() is not configuration-cache
    // compatible, and this build has the configuration cache enabled.
    if (providers.gradleProperty("signingInMemoryKey").isPresent) {
        signAllPublications()
    }
}

