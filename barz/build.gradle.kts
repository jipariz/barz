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

        withHostTestBuilder {}.configure {}

        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
            }
        }
    }

    jvm()

    // Tests are off on the web targets, deliberately. Karma needs a local Chrome install, and
    // Node cannot host them either because Compose's web runtime loads Skiko's .wasm over XHR.
    // The code under test lives in commonMain and is exercised by the jvm, android and iOS runs,
    // so the web targets are compile-verified rather than untested.
    js(IR) { browser { testTask { enabled = false } } }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { browser { testTask { enabled = false } } }

    // iosX64 (the Intel simulator) is deliberately absent: Apple has wound Intel Macs down, and
    // several Compose Multiplatform artifacts have already stopped publishing a uikitX64 variant.
    // Carrying it would pin this library to older dependencies for a target nobody ships to.
    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            // `api`, not `implementation`: these types appear in this library's public signatures
            // (@Composable, Modifier, Color, DrawableResource), so consumers must see them.
            api(libs.compose.runtime)
            api(libs.compose.ui)
            api(libs.compose.material3)
            api(libs.compose.components.resources)

            // Internal only — Barz wraps NavigationSuiteScaffold behind its own API surface.
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3.adaptive.navigation.suite)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

mavenPublishing {
    // Coordinates, POM and version come from gradle.properties (GROUP / VERSION_NAME / POM_*).
    configure(
        KotlinMultiplatform(
            // Central requires a javadoc jar; an empty one satisfies it. Dokka would pull
            // rendering artifacts that are not in the local cache, so it stays opt-in.
            javadocJar = JavadocJar.Empty(),
            sourcesJar = true,
        ),
    )
    publishToMavenCentral()
    // In-memory signing only. The signing plugin's useGpgCmd() is not configuration-cache
    // compatible, and this build has the configuration cache enabled.
    if (providers.gradleProperty("signingInMemoryKey").isPresent) {
        signAllPublications()
    }
}

@OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
kotlin {
    abiValidation {
        // Commit the dump so a change to the public API shows up as a diff in review.
        enabled.set(true)
    }
}
