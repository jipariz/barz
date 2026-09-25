package dev.parez.navbarz.sample

import dev.parez.navbarz.IosOptions
import dev.parez.navbarz.navBarzTabBarController
import platform.UIKit.UIViewController

/**
 * UIViewController entry point consumed by `iosApp/iosApp/ContentView.swift`.
 *
 * This roots the app in NavBarz's real `UITabBarController` rather than in a Compose-drawn bar.
 * That is what earns the system treatment: Liquid Glass on the bar, the sidebar on iPad, and — on
 * iPhone Duo — the bar moving to the side strip on its own. A Compose bar would get none of those,
 * however closely it imitated them.
 */
@Suppress("unused")
fun MainViewController(): UIViewController =
    navBarzTabBarController(
        items = DemoNavItems,
        options = IosOptions(sidebarAdaptable = true),
        content = { index -> DemoTab(index) },
    )
