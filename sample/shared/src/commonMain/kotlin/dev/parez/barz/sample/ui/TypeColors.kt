package dev.parez.barz.sample.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * The standard community type palette. The design applies each type three ways — as the detail
 * screen's full-bleed header, as a chip background, and as that chip's text — so the two derived
 * shades live next to the base rather than being hardcoded per type.
 */
fun typeColor(type: String): Color =
    when (type.lowercase()) {
        "normal" -> Color(0xFFA8A878)
        "fire" -> Color(0xFFF08030)
        "water" -> Color(0xFF6890F0)
        "electric" -> Color(0xFFF2C21D)
        "grass" -> Color(0xFF76CC4A)
        "ice" -> Color(0xFF7FCFCF)
        "fighting" -> Color(0xFFC03028)
        "poison" -> Color(0xFFA040A0)
        "ground" -> Color(0xFFE0C068)
        "flying" -> Color(0xFFA890F0)
        "psychic" -> Color(0xFFF85888)
        "bug" -> Color(0xFFA8B820)
        "rock" -> Color(0xFFB8A038)
        "ghost" -> Color(0xFF705898)
        "dragon" -> Color(0xFF7038F8)
        "dark" -> Color(0xFF705848)
        "steel" -> Color(0xFF8E8EB0)
        "fairy" -> Color(0xFFEE99AC)
        else -> Color(0xFF68A090)
    }

/** Pale tint behind a type tag. */
fun typeChipBackground(type: String, dark: Boolean): Color =
    lerp(typeColor(type), if (dark) Color.Black else Color.White, if (dark) 0.62f else 0.66f)

/** The tag's label — the base hue pushed darker so it reads against [typeChipBackground]. */
fun typeChipContent(type: String, dark: Boolean): Color =
    lerp(typeColor(type), if (dark) Color.White else Color.Black, if (dark) 0.15f else 0.3f)
