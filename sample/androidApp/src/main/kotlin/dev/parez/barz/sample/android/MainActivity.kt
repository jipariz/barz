package dev.parez.barz.sample.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.parez.barz.sample.android.ui.AdaptiveNavApp
import dev.parez.barz.sample.android.ui.theme.AdaptiveNavSuiteTheme

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
