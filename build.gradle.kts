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
