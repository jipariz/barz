package dev.parez.barz.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The demo insets its own content, so let it draw behind the system bars.
        enableEdgeToEdge()
        setContent { DemoApp() }
    }
}
