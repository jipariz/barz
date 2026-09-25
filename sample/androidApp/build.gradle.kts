plugins {
    alias(libs.plugins.androidApplication)
    // Without this, MainActivity's `setContent { }` compiles as a plain lambda and the app dies at
    // launch with NoSuchMethodError: setContent$default(… Function0 …). AGP brings Kotlin itself,
    // but not the Compose compiler.
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "dev.parez.navbarz.sample.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.parez.navbarz.sample"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildTypes { getByName("release") { isMinifyEnabled = false } }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    implementation(projects.sample.shared)
    // AGP applications don't inherit a KMP library's transitive Compose/activity deps on the
    // compile classpath, so MainActivity's own imports are declared here.
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
}
