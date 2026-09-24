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
