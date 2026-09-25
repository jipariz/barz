package dev.parez.navbarz

/** The platform the app is running on, used to resolve per-platform configuration. */
enum class NavBarzPlatform {
    Android,
    Desktop,
    Web,
    Ios,
}

/** The current platform. */
expect val currentPlatform: NavBarzPlatform
