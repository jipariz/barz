package dev.jparizek.adaptivenavsuite.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * Spacing scale for the shared UI. Exists so shared composables never hardcode a `.dp` literal,
 * and so both platforms resolve the same values — Android provides this from its own theme
 * (which keeps Material You dynamic color), iOS gets it from [AppTheme].
 */
object AppSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
}

val LocalAppSpacing = staticCompositionLocalOf { AppSpacing }

/**
 * Theme for the shared Compose content on platforms that don't bring their own.
 *
 * This matters more than it looks: `ComposeUIViewController` installs no Material composition
 * locals, so before this existed the iOS side resolved `MaterialTheme.colorScheme` to
 * [lightColorScheme] unconditionally and `LocalContentColor` to black — invisible while the
 * screen was plain text on no background, wrong the moment there is color on screen.
 *
 * [isSystemInDarkTheme] is multiplatform, so this is also what gives the iOS app dark mode.
 *
 * Android deliberately does *not* use this: `AdaptiveNavSuiteTheme` keeps dynamic color, which is
 * the platform-correct behavior there and a nice visible contrast with iOS in the demo.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAppSpacing provides AppSpacing) {
        MaterialTheme(
            colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
            content = content,
        )
    }
}
