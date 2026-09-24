package dev.parez.barz.sample.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Adds two [PaddingValues] edge-wise.
 *
 * Scrolling screens need the window insets handed down by the navigation scaffold *plus* their own
 * gutter, and both have to reach `contentPadding` — `Modifier.padding` on a lazy container clips the
 * scroll instead of insetting its content, so the bar would slice through the list as it moves.
 */
internal operator fun PaddingValues.plus(other: PaddingValues): PaddingValues =
    PaddingValues(
        start =
            calculateStartPadding(LayoutDirection.Ltr) +
                other.calculateStartPadding(LayoutDirection.Ltr),
        top = calculateTopPadding() + other.calculateTopPadding(),
        end =
            calculateEndPadding(LayoutDirection.Ltr) +
                other.calculateEndPadding(LayoutDirection.Ltr),
        bottom = calculateBottomPadding() + other.calculateBottomPadding(),
    )

/**
 * The same insets with the top edge zeroed. Screens that sit under a page header have already
 * consumed the top inset there; passing it on again would leave a second status-bar-sized gap. The
 * horizontal edges are kept — those carry the navigation rail's width on wide windows.
 */
internal fun PaddingValues.withoutTop(): PaddingValues =
    PaddingValues(
        start = calculateStartPadding(LayoutDirection.Ltr),
        top = 0.dp,
        end = calculateEndPadding(LayoutDirection.Ltr),
        bottom = calculateBottomPadding(),
    )
