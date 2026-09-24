package dev.parez.barz.sample

/**
 * Small piece of genuinely shared business logic so both native front ends demonstrably call into
 * the same Kotlin code, rather than [AppDestination] being the only thing shared.
 */
object DestinationContent {
    fun headline(destination: AppDestination): String = when (destination) {
        AppDestination.HOME -> "Welcome back"
        AppDestination.FAVORITES -> "Saved for later"
        AppDestination.SHOPPING -> "Your cart"
        AppDestination.PROFILE -> "Your account"
    }

    fun body(destination: AppDestination): String = when (destination) {
        AppDestination.HOME ->
            "This screen's copy is produced by the shared Kotlin Multiplatform module " +
                "(dev.parez.barz.sample.DestinationContent), running on ${platformName()}."
        AppDestination.FAVORITES ->
            "Favorites content, also served from the shared module. Running on ${platformName()}."
        AppDestination.SHOPPING ->
            "Shopping content from the shared module. Running on ${platformName()}."
        AppDestination.PROFILE ->
            "Profile content from the shared module. Running on ${platformName()}."
    }
}

/** Simple expect/actual so the demo screens can prove which native runtime is executing. */
expect fun platformName(): String
