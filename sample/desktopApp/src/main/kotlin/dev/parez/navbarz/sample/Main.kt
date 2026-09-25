package dev.parez.navbarz.sample

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "NavBarz — Pokédex",
        // Deliberately phone-shaped on launch: the point of the demo is that dragging the window
        // wider walks the bar → rail → drawer, so it should start at the narrow end.
        state = rememberWindowState(width = 480.dp, height = 820.dp),
    ) {
        DemoApp()
    }
}
