package dev.parez.navbarz.sample.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Adds two [PaddingValues] edge-wise.
 *
 * Scrolling screens need the window insets handed down by the navigation scaffold *plus* their own
 * gutter, and both have to reach `contentPadding` — `Modifier.padding` on a lazy container clips
 * the scroll instead of insetting its content, so the bar would slice through the list as it moves.
 */
@Composable
internal operator fun PaddingValues.plus(other: PaddingValues): PaddingValues {
    // calculateStartPadding(Ltr) returns the *left* edge. Feeding that back in as `start` sends it
    // to the right in an RTL layout — and the values being folded here are the navigation rail's
    // width and the iPhone Duo's side strip, which is the whole reason this helper exists.
    val direction = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(direction) + other.calculateStartPadding(direction),
        top = calculateTopPadding() + other.calculateTopPadding(),
        end = calculateEndPadding(direction) + other.calculateEndPadding(direction),
        bottom = calculateBottomPadding() + other.calculateBottomPadding(),
    )
}

/**
 * The same insets with the top edge zeroed. Screens that sit under a page header have already
 * consumed the top inset there; passing it on again would leave a second status-bar-sized gap. The
 * horizontal edges are kept — those carry the navigation rail's width on wide windows.
 */
@Composable
internal fun PaddingValues.withoutTop(): PaddingValues {
    val direction = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(direction),
        top = 0.dp,
        end = calculateEndPadding(direction),
        bottom = calculateBottomPadding(),
    )
}

/**
 * The same insets with the start (or end) edge zeroed, for a pane that does not touch that window
 * edge.
 *
 * `ListDetailPaneScaffold` hands both panes the window's insets, but in two-pane mode only the
 * outer edges are real: the list's end and the detail's start abut each other, not the window. On
 * an opened iPhone Duo the native tab bar contributes an 84dp *end* inset, so an unfiltered list
 * pane leaves 84dp of dead space against the divider.
 */
@Composable
internal fun PaddingValues.withoutStart(): PaddingValues {
    val direction = LocalLayoutDirection.current
    return PaddingValues(
        start = 0.dp,
        top = calculateTopPadding(),
        end = calculateEndPadding(direction),
        bottom = calculateBottomPadding(),
    )
}

/** @see withoutStart */
@Composable
internal fun PaddingValues.withoutEnd(): PaddingValues {
    val direction = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(direction),
        top = calculateTopPadding(),
        end = 0.dp,
        bottom = calculateBottomPadding(),
    )
}
