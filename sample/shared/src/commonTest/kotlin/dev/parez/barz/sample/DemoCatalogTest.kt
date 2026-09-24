package dev.parez.barz.sample

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DemoCatalogTest {

    @Test
    fun `every destination has items`() {
        AppDestination.entries.forEach { destination ->
            assertTrue(
                DemoCatalog.items(destination).isNotEmpty(),
                "No items for $destination",
            )
        }
    }

    @Test
    fun `every item has non-blank content`() {
        allItems().forEach { item ->
            assertTrue(item.id.isNotBlank())
            assertTrue(item.title.isNotBlank())
            assertTrue(item.subtitle.isNotBlank())
            assertTrue(item.body.isNotBlank())
        }
    }

    /**
     * Ids are the scaffold's content key, so a duplicate would silently make two rows resolve to
     * the same detail pane — and would also break `LazyColumn`'s `key`.
     */
    @Test
    fun `item ids are unique across the whole catalog`() {
        val ids = allItems().map { it.id }
        assertEquals(ids.size, ids.toSet().size, "Duplicate ids in catalog: $ids")
    }

    @Test
    fun `every item is resolvable by id`() {
        allItems().forEach { item ->
            assertEquals(item, assertNotNull(DemoCatalog.item(item.id)))
        }
    }

    @Test
    fun `unknown id resolves to null`() {
        assertEquals(null, DemoCatalog.item("no-such-item"))
    }

    private fun allItems(): List<DemoItem> =
        AppDestination.entries.flatMap { DemoCatalog.items(it) }
}
