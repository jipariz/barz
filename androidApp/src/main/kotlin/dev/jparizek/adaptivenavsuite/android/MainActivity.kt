package dev.jparizek.adaptivenavsuite.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.jparizek.adaptivenavsuite.android.ui.AdaptiveNavApp
import dev.jparizek.adaptivenavsuite.android.ui.theme.AdaptiveNavSuiteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdaptiveNavSuiteTheme {
                AdaptiveNavApp()
            }
        }
    }
}
