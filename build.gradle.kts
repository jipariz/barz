plugins {
    // this is necessary to avoid the plugins being loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.ktfmt)
}

// Formatting is the whole build's concern, so configure it once here rather than per module.
// kotlinLangStyle() is 4-space / 100-column, which is what this code already mostly follows —
// adopting it was a small diff. `./gradlew ktfmtFormat` to fix, `ktfmtCheck` to verify; CI runs
// the latter. `allprojects`, not `subprojects`: the root has no Kotlin source set but does own
// the *Scripts tasks that format settings.gradle.kts, and without the style they default to
// Google's 2-space.
allprojects {
    apply(plugin = rootProject.libs.plugins.ktfmt.get().pluginId)
    extensions.configure<com.ncorti.ktfmt.gradle.KtfmtExtension> { kotlinLangStyle() }
}

// What CI checks, named in one place.
//
// `:navbarz` only. It is the published artifact; the sample is a demo that ships to nobody, and
// building it was costing more than it proved — `build` also produces the sample's *distributable*
// artifacts (linked iOS frameworks, optimized web bundles), which exhausted the runner's heap
// twice: first `linkReleaseFrameworkIosArm64`, then `compileProductionExecutableKotlinWasmJs`.
//
// Excluding those with `-x` was the first attempt and does not hold: `:sample:shared` and
// `:sample:webApp` each have their own set, the exclusions do not cascade (`-x` on
// `compileProductionExecutableKotlinWasmJs` leaves `...WasmJsOptimize` behind), and the names move
// with the Kotlin version. Naming what must pass is stable; naming what must not run is
// whack-a-mole.
//
// The sample is not entirely unbuilt on CI: the xcodebuild step compiles `:sample:shared` for iOS
// through the Xcode project's "Compile Kotlin Framework" phase, which is the one path that also
// proves a consumer's own wiring works. What is no longer covered is the Android, desktop and web
// shells — a change that breaks those now surfaces locally rather than in CI.
tasks.register("ciVerify") {
    group = "verification"
    description =
        "Everything CI checks: the published library, built, tested, ABI-checked and published locally."

    // `build` covers every target and the tests, and pulls in checkKotlinAbi because KGP wires
    // `check` to it.
    dependsOn(":navbarz:build")
    dependsOn(":navbarz:publishToMavenLocal")
}
