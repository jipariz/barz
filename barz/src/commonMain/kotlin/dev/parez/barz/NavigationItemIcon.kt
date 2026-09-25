package dev.parez.barz

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgeDefaults
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import org.jetbrains.compose.resources.painterResource

/**
 * Draws an item's icon from whichever source the caller supplied — an explicit slot wins, otherwise
 * [NavigationItem.icon] — and wraps it in a badge when the item asks for one.
 *
 * Badging lives here rather than at the call sites so every container gets it from one place;
 * [AdaptiveNavigationScaffold] used to drop [NavigationItem.badge] on the floor because only
 * [AdaptiveNavigationBar] knew how to draw it.
 *
 * Failing loudly on a missing icon is deliberate. An item with no icon at all would otherwise render
 * as an invisible tap target, which is far harder to diagnose than an exception naming the item.
 */
@Composable
internal fun NavigationItemIcon(
    item: NavigationItem,
    index: Int,
    selected: Boolean,
    slot: (@Composable (index: Int, selected: Boolean) -> Unit)?,
    badgeContainerColor: Color = Color.Unspecified,
    badgeContentColor: Color = Color.Unspecified,
    showBadge: Boolean = true,
) {
    val icon: @Composable () -> Unit = {
        when {
            slot != null -> slot(index, selected)
            item.icon != null -> Icon(
                painter = painterResource(item.iconFor(selected)!!),
                // Null when the item shows a label: NavigationBarItem/RailItem/DrawerItem all merge
                // descendant semantics and already announce the title, so describing the icon too
                // makes every screen reader say "Home, Home".
                contentDescription =
                    if (item.showLabel) null else item.contentDescription ?: item.title,
            )
            else -> error(
                "NavigationItem \"${item.title}\" has no icon. Set NavigationItem.icon to a " +
                    "DrawableResource, or pass an `icon` slot to the composable.",
            )
        }
    }

    // A text badge wins over the dot; `showBadgeDot` is only consulted when `badge` is null.
    val badge: (@Composable () -> Unit)? = when {
        !showBadge -> null
        item.badge != null -> ({ ItemBadge(badgeContainerColor, badgeContentColor) { Text(item.badge) } })
        item.showBadgeDot -> ({ ItemBadge(badgeContainerColor, badgeContentColor, content = null) })
        else -> null
    }

    if (badge == null) Box { icon() } else BadgedBox(badge = { badge() }) { icon() }
}

@Composable
private fun ItemBadge(
    containerColor: Color,
    contentColor: Color,
    content: (@Composable androidx.compose.foundation.layout.RowScope.() -> Unit)? = null,
) {
    val container = containerColor.takeOrElse { BadgeDefaults.containerColor }
    Badge(
        containerColor = container,
        contentColor = contentColor.takeOrElse { contentColorFor(container) },
        content = content,
    )
}
