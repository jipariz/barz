package dev.parez.barz.sample.ui

import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Renders a team member's added-at stamp the way the design does — "Dec 5, 11:30 AM", or "Dec 5,
 * 11:30" when the 24-hour setting is on.
 *
 * Hand-rolled rather than delegating to a platform formatter: `kotlinx-datetime` has no common
 * formatter for localised month names, and an `expect`/`actual` per target for one short string
 * would be more surface than the demo warrants.
 */
fun formatAddedAt(instant: Instant, twentyFourHourTime: Boolean): String {
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val month = MONTHS[local.month.ordinal]
    val minute = local.minute.toString().padStart(2, '0')
    return if (twentyFourHourTime) {
        "$month ${local.day}, ${local.hour.toString().padStart(2, '0')}:$minute"
    } else {
        val suffix = if (local.hour < 12) "AM" else "PM"
        val hour12 = if (local.hour % 12 == 0) 12 else local.hour % 12
        "$month ${local.day}, $hour12:$minute $suffix"
    }
}

private val MONTHS =
    listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
