package dev.parez.barz

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIColor
import platform.UIKit.UIImage
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarAppearance
import platform.UIKit.UITabBarDelegateProtocol
import platform.UIKit.UITabBarItem
import platform.darwin.NSObject

internal actual val supportsNativeBar: Boolean = true

/**
 * A real `UITabBar`, embedded in the Compose tree.
 *
 * This is what makes Liquid Glass genuine rather than imitated: the material comes from the system
 * view, not from anything Barz draws. It is the middle of three options — a Compose-drawn bar gets
 * neither the material nor system placement, this gets the material only, and
 * [barzTabBarController] gets both.
 *
 * The bar is positioned by Compose, so a foldable cannot relocate it into its side strip. That is
 * not a limitation of this code but of what an embedded view can be: the system reflows the bar it
 * owns, and it does not own this one.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun NativeIosBar(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    colors: AdaptiveNavigationBarColors,
    options: IosOptions,
    modifier: Modifier,
) {
    // UITabBar holds its delegate weakly, so it has to be remembered here rather than created
    // inline in the factory — otherwise selection stops firing as soon as it is collected.
    val delegate = remember { NativeBarDelegate() }
    // SideEffect, not a bare write: composition can be cancelled or re-run speculatively, and these
    // are plain vars on a long-lived object — updating them mid-composition applies changes that
    // the frame may never commit.
    SideEffect {
        delegate.onItemSelected = onItemSelected
        delegate.items = items
    }

    UIKitView(
        factory = {
            UITabBar().apply {
                this.delegate = delegate
                if (!options.liquidGlass) {
                    val opaque = UITabBarAppearance().apply { configureWithOpaqueBackground() }
                    standardAppearance = opaque
                    scrollEdgeAppearance = opaque
                }
            }
        },
        modifier = modifier,
        update = { bar ->
            colors.selectedIconColor.toUIColor()?.let { bar.tintColor = it }
            bar.unselectedItemTintColor = colors.unselectedIconColor.toUIColor()
            bar.setItems(
                items.mapIndexed { index, item ->
                    UITabBarItem(
                        title = item.title.takeIf { item.showLabel },
                        image = UIImage.systemImageNamed(item.systemIcon)
                            ?: UIImage.imageNamed(item.systemIcon),
                        tag = index.toLong(),
                    ).apply {
                        // Asset-catalog images are a legitimate fallback: consumers often ship
                        // their own glyphs rather than SF Symbols.
                        item.selectedSystemIcon?.let {
                            selectedImage = UIImage.systemImageNamed(it) ?: UIImage.imageNamed(it)
                        }
                        badgeValue = item.badge ?: if (item.showBadgeDot) "" else null
                        enabled = item.enabled
                    }
                },
                animated = false,
            )
            // -1 is the documented "nothing selected" value; UIKit expects a null item for that.
            bar.selectedItem = bar.items?.getOrNull(selectedIndex) as? UITabBarItem
        },
    )
}

private class NativeBarDelegate : NSObject(), UITabBarDelegateProtocol {
    var onItemSelected: (Int) -> Unit = {}
    var items: List<NavigationItem> = emptyList()

    override fun tabBar(tabBar: UITabBar, didSelectItem: UITabBarItem) {
        val index = didSelectItem.tag.toInt()
        if (index in items.indices) onItemSelected(index)
    }
}

private fun Color.toUIColor(): UIColor? = takeIf { it != Color.Unspecified }?.let {
    UIColor.colorWithRed(it.red.toDouble(), it.green.toDouble(), it.blue.toDouble(), it.alpha.toDouble())
}
