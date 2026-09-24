package dev.parez.barz.sample

import dev.parez.barz.IosOptions
import dev.parez.barz.barzTabBarController
import dev.parez.barz.sample.ui.DestinationScreen
import dev.parez.barz.sample.ui.theme.AppTheme
import platform.UIKit.UIViewController

/**
 * The whole iOS app, from Kotlin.
 *
 * [barzTabBarController] returns a real `UITabBarController`, so the tab bar is a genuine system
 * container — Liquid Glass, the iPad sidebar and iPhone Duo placement all come for free — while
 * each tab's content is the shared Compose screen. Swift's only job is to host this one view
 * controller.
 *
 * [AppTheme] is not optional: `ComposeUIViewController` installs no Material composition locals, so
 * without it `MaterialTheme.colorScheme` resolves to `lightColorScheme()` whatever the system
 * appearance is. It is also what gives the iOS app dark mode.
 */
fun barzSampleRootViewController(): UIViewController = barzTabBarController(
    items = sampleDestinations,
    options = IosOptions(sidebarAdaptable = true, liquidGlass = true),
) { index ->
    AppTheme {
        DestinationScreen(destination = destinationAt(index))
    }
}
