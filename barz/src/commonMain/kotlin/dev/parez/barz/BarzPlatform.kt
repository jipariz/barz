package dev.parez.barz

/** The platform the app is running on, used to resolve per-platform configuration. */
enum class BarzPlatform { Android, Desktop, Web, Ios }

/** The current platform. */
expect val currentPlatform: BarzPlatform
