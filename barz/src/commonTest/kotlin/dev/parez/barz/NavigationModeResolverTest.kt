package dev.parez.barz

import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationModeResolverTest {

    private fun mode(
        width: Float,
        height: Float,
        config: AdaptiveNavigationConfig = AdaptiveNavigationConfig(),
    ) = resolveNavigationMode(width, height, config)

    @Test
    fun `compact width gets a bottom bar`() {
        assertEquals(NavigationMode.BottomBar, mode(411f, 891f))
    }

    @Test
    fun `medium width gets a rail`() {
        assertEquals(NavigationMode.Rail, mode(673f, 841f))
    }

    @Test
    fun `wide window gets a drawer only past the drawer breakpoint`() {
        assertEquals(NavigationMode.Rail, mode(851f, 883f), "unfolded foldable should stay a rail")
        assertEquals(NavigationMode.Drawer, mode(1280f, 800f))
    }

    /** The regression this library exists to get right: wide but short is still a phone. */
    @Test
    fun `landscape phone stays a bottom bar despite being wide`() {
        assertEquals(NavigationMode.BottomBar, mode(891f, 411f))
    }

    @Test
    fun `breakpoints are configurable`() {
        val eager = AdaptiveNavigationConfig(
            breakpoints = NavigationBreakpoints(railFromWidthDp = 400, drawerFromWidthDp = 700),
        )
        assertEquals(NavigationMode.Rail, mode(411f, 891f, eager))
        assertEquals(NavigationMode.Drawer, mode(851f, 883f, eager))
    }

    @Test
    fun `allowedModes caps the mode instead of failing`() {
        val noDrawer = AdaptiveNavigationConfig(
            allowedModes = setOf(NavigationMode.BottomBar, NavigationMode.Rail),
        )
        assertEquals(NavigationMode.Rail, mode(1600f, 1200f, noDrawer))

        val barOnly = AdaptiveNavigationConfig(allowedModes = setOf(NavigationMode.BottomBar))
        assertEquals(NavigationMode.BottomBar, mode(1600f, 1200f, barOnly))
    }

    /** Nothing below Drawer is permitted, so a phone still gets one rather than a crash. */
    @Test
    fun `a config allowing only drawer never falls through`() {
        val drawerOnly = AdaptiveNavigationConfig(allowedModes = setOf(NavigationMode.Drawer))
        assertEquals(NavigationMode.Drawer, mode(360f, 640f, drawerOnly))
    }

    // ── Breakpoint boundaries ─────────────────────────────────────────────────────────────────
    // Every comparison in resolveNavigationMode is `>=`. Before these, flipping any one of them to
    // `>` passed the entire suite — the thresholds are the one thing this library must get exactly
    // right, and nothing pinned them.

    @Test
    fun `rail starts exactly at the rail breakpoint`() {
        assertEquals(NavigationMode.BottomBar, mode(599f, 900f))
        assertEquals(NavigationMode.Rail, mode(600f, 900f))
    }

    @Test
    fun `drawer starts exactly at the drawer breakpoint`() {
        assertEquals(NavigationMode.Rail, mode(1199f, 900f))
        assertEquals(NavigationMode.Drawer, mode(1200f, 900f))
    }

    @Test
    fun `the height guard releases exactly at the minimum height`() {
        assertEquals(NavigationMode.BottomBar, mode(1600f, 479f), "too short to promote")
        assertEquals(NavigationMode.Drawer, mode(1600f, 480f))
    }

    @Test
    fun `a zero size resolves rather than throwing`() {
        // containerSize is IntSize.Zero on the first frame, before the window reports a size.
        assertEquals(NavigationMode.BottomBar, mode(0f, 0f))
    }

    // ── Clamping ──────────────────────────────────────────────────────────────────────────────
    // clampTo walks `entries` by ordinal. Previously only the Drawer-downwards and
    // ordinal-zero paths were exercised; these cover clamping from the middle, in both directions.

    @Test
    fun `rail clamps down to bottom bar when rail is disallowed`() {
        val noRail = AdaptiveNavigationConfig(
            allowedModes = setOf(NavigationMode.BottomBar, NavigationMode.Drawer),
        )
        assertEquals(NavigationMode.BottomBar, mode(800f, 900f, noRail))
    }

    @Test
    fun `rail clamps up to drawer when nothing narrower is allowed`() {
        // The surprising direction: a rail-width window is promoted, because there is no narrower
        // allowed mode to fall back to.
        val drawerOnly = AdaptiveNavigationConfig(allowedModes = setOf(NavigationMode.Drawer))
        assertEquals(NavigationMode.Drawer, mode(800f, 900f, drawerOnly))
    }

    @Test
    fun `a phone is promoted when only wider modes are allowed`() {
        val railOnly = AdaptiveNavigationConfig(allowedModes = setOf(NavigationMode.Rail))
        assertEquals(NavigationMode.Rail, mode(360f, 640f, railOnly))
    }

    @Test
    fun `breakpoints that would make rail unreachable are rejected`() {
        try {
            NavigationBreakpoints(railFromWidthDp = 1200, drawerFromWidthDp = 600)
            throw AssertionError("expected IllegalArgumentException")
        } catch (expected: IllegalArgumentException) {
            // exactly what we want
        }
    }

    @Test
    fun `empty allowedModes is rejected at construction`() {
        try {
            AdaptiveNavigationConfig(allowedModes = emptySet())
            throw AssertionError("expected IllegalArgumentException")
        } catch (expected: IllegalArgumentException) {
            // exactly what we want
        }
    }
}
