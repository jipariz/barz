package dev.parez.barz

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.painterResource

/**
 * Draws an item's icon from whichever source the caller supplied: an explicit slot wins, otherwise
 * [NavigationItem.icon].
 *
 * Failing loudly here is deliberate. An item with no icon at all would otherwise render as an
 * invisible tap target, which is far harder to diagnose than an exception naming the item.
 */
@Composable
internal fun NavigationItemIcon(
    item: NavigationItem,
    index: Int,
    selected: Boolean,
    slot: (@Composable (index: Int, selected: Boolean) -> Unit)?,
) {
    when {
        slot != null -> slot(index, selected)
        item.icon != null -> Icon(
            painter = painterResource(item.iconFor(selected)!!),
            contentDescription = item.contentDescription ?: item.title,
        )
        else -> error(
            "NavigationItem \"${item.title}\" has no icon. Set NavigationItem.icon to a " +
                "DrawableResource, or pass an `icon` slot to the composable.",
        )
    }
}
