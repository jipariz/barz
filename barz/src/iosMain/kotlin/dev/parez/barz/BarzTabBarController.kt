package dev.parez.barz

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ComposeUIViewController
import platform.Foundation.NSBundle
import platform.UIKit.UIImage
import platform.UIKit.UITabBarAppearance
import platform.UIKit.UITabBarController
import platform.UIKit.UITabBarControllerDelegateProtocol
import platform.UIKit.UITabBarControllerModeTabSidebar
import platform.UIKit.UITabBarItem
import platform.UIKit.UIViewController
import platform.UIKit.setTabBarItem
import platform.darwin.NSObject

/**
 * Native iOS navigation chrome, built from UIKit in Kotlin.
 *
 * This is a real `UITabBarController`, which matters: Liquid Glass, the iPad sidebar and the
 * placement iPhone Duo introduces are granted by the system to genuine containers and to nothing
 * else. A bar drawn in Compose gets none of them.
 *
 * Doing it in UIKit rather than SwiftUI is what keeps Barz to a single Gradle dependency — SwiftUI
 * cannot be shipped inside a Kotlin framework, but UIKit is fully bound by Kotlin/Native, so the
 * same container is reachable without asking consumers to add a Swift package too.
 *
 * Call it from your `ComposeUIViewController` entry point and hand the result to SwiftUI via
 * `UIViewControllerRepresentable`, or set it as the window's `rootViewController`.
 *
 * @param items the destinations; [NavigationItem.systemIcon] is used as an SF Symbol name.
 * @param options [IosOptions.sidebarAdaptable] promotes tabs to a sidebar on iPad;
 *   [IosOptions.liquidGlass] false forces an opaque bar instead of the system material.
 * @param onSelect invoked with the newly selected index.
 * @param content the Compose content for a given tab index.
 */
fun barzTabBarController(
    items: List<NavigationItem>,
    options: IosOptions = IosOptions(),
    onSelect: (Int) -> Unit = {},
    content: @Composable (index: Int) -> Unit,
): UIViewController = BarzTabBarController(items, options, onSelect, content)

private class BarzTabBarController(
    items: List<NavigationItem>,
    options: IosOptions,
    private val onSelect: (Int) -> Unit,
    content: @Composable (index: Int) -> Unit,
) : UITabBarController(nibName = null, bundle = null as NSBundle?) {

    // UIKit holds `delegate` weakly, so the object has to be owned by something that outlives the
    // call. Keeping it as a field of the controller is the simplest thing that does that.
    private val tabDelegate = object : NSObject(), UITabBarControllerDelegateProtocol {
        override fun tabBarController(
            tabBarController: UITabBarController,
            didSelectViewController: UIViewController,
        ) {
            onSelect(tabBarController.selectedIndex.toInt())
        }
    }

    init {
        setViewControllers(
            items.mapIndexed { index, item ->
                ComposeUIViewController { content(index) }.also { vc ->
                    vc.setTabBarItem(
                        UITabBarItem(
                        title = item.title.takeIf { item.showLabel },
                        image = UIImage.systemImageNamed(item.systemIcon),
                        selectedImage = item.selectedSystemIcon?.let(UIImage::systemImageNamed),
                        ).apply {
                            badgeValue = item.badge ?: if (item.showBadgeDot) "" else null
                            enabled = item.enabled
                        },
                    )
                }
            },
        )
        delegate = tabDelegate

        if (options.sidebarAdaptable) {
            mode = UITabBarControllerModeTabSidebar
        }
        if (!options.liquidGlass) {
            // There is no per-view opt-out of the system material — the only real switch is the
            // app-level UIDesignRequiresCompatibility Info.plist key, which a library cannot set.
            // Forcing an opaque appearance is the closest a container can get on its own.
            val opaque = UITabBarAppearance().apply { configureWithOpaqueBackground() }
            tabBar.standardAppearance = opaque
            tabBar.scrollEdgeAppearance = opaque
        }
    }

    /** Selects a tab programmatically, e.g. to restore state. */
    fun select(index: Int) {
        selectedIndex = index.toULong()
    }
}
