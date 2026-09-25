package dev.parez.barz

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

internal actual val supportsNativeBar: Boolean = false

/** Never called: [supportsNativeBar] is false here, so the Compose bar is always used. */
@Composable
internal actual fun NativeIosBar(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    colors: AdaptiveNavigationBarColors,
    options: IosOptions,
    modifier: Modifier,
): Unit = Unit
