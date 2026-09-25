package dev.parez.barz

import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.DrawableResource

/**
 * One navigation destination.
 *
 * A single item carries both platforms' iconography because the same list drives a Compose
 * container on Android/Desktop/Web and a native `UITabBarController` on iOS — the two need different
 * icon types and there is no sensible common denominator.
 *
 * @param title label shown next to or under the icon.
 * @param icon drawable used by the Compose containers. Optional: pass an `icon` slot to
 *   [AdaptiveNavigationBar] or [AdaptiveNavigationScaffold] instead if your icons are
 *   `ImageVector`s, or if your resources live somewhere this item cannot reference. Exactly one of
 *   the two must be supplied.
 * @param selectedIcon optional filled variant; [icon] is reused when null.
 * @param systemIcon SF Symbol name, used by the native iOS chrome. Ignored on other platforms.
 * @param selectedSystemIcon optional filled SF Symbol; [systemIcon] is reused when null.
 * @param showLabel whether the label is drawn. Icon-only items still need [contentDescription].
 * @param badge text badge, e.g. an unread count. Null for none.
 * @param showBadgeDot draws a dot instead of text. Ignored when [badge] is set.
 * @param enabled whether the item can be selected.
 * @param contentDescription accessibility label; falls back to [title] when null.
 */
@Immutable
data class NavigationItem(
    val title: String,
    val icon: DrawableResource? = null,
    val selectedIcon: DrawableResource? = null,
    val systemIcon: String,
    val selectedSystemIcon: String? = null,
    val showLabel: Boolean = true,
    val badge: String? = null,
    val showBadgeDot: Boolean = false,
    val enabled: Boolean = true,
    val contentDescription: String? = null,
)
