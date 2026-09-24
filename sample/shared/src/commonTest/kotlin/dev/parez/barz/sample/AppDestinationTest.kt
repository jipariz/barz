package dev.parez.barz.sample

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppDestinationTest {
    @Test
    fun `every destination has non-blank titles and icon keys`() {
        AppDestination.entries.forEach { destination ->
            assertTrue(destination.title.isNotBlank())
            assertTrue(destination.materialIconName.isNotBlank())
            assertTrue(destination.materialSelectedIconName.isNotBlank())
            assertTrue(destination.systemImageName.isNotBlank())
        }
    }

    @Test
    fun `start destination is home`() {
        assertEquals(AppDestination.HOME, AppDestination.startDestination)
    }

    @Test
    fun `destination content is provided for every destination`() {
        AppDestination.entries.forEach { destination ->
            assertTrue(DestinationContent.headline(destination).isNotBlank())
            assertTrue(DestinationContent.body(destination).isNotBlank())
        }
    }
}
