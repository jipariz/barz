package dev.parez.barz

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.DrawableResource

/**
 * A prominent action rendered inside the bar on iOS, where the platform idiom puts a primary
 * action in the tab bar rather than floating above the content.
 *
 * It is iOS-only by design. On Android, Desktop and Web this is ignored — use `Scaffold`'s
 * `floatingActionButton` slot, which is the correct idiom there.
 *
 * @param systemIcon SF Symbol or asset-catalog name, used when the chrome is native UIKit.
 * @param icon drawable for the Compose-drawn bar; pass an `fabIcon` slot instead if your icons
 *   are `ImageVector`s.
 * @param containerColor background of the circular button.
 * @param contentColor tint of its icon.
 */
@Immutable
data class IosFabItem(
    val systemIcon: String,
    val icon: DrawableResource? = null,
    val containerColor: Color = Color.Unspecified,
    val contentColor: Color = Color.Unspecified,
    val title: String = "",
    val showLabel: Boolean = false,
    val contentDescription: String? = null,
)
