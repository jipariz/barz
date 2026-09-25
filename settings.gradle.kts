rootProject.name = "barz-sdk"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":barz")

// Demo app, ported from the sidekick Pokédex: one shared KMP library plus a thin shell per
// platform. iosApp is a sibling Xcode project, not a Gradle module.
include(":sample:shared")

include(":sample:androidApp")

include(":sample:desktopApp")

include(":sample:webApp")
