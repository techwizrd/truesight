package com.truesight.truesight

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.truesight.truesight.ui.theme.TruesightTheme

class MainActivity : ComponentActivity() {
    private var sharePayload by mutableStateOf<SharePayload?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateSharePayload(intent)
        enableEdgeToEdge()
        setContent {
            TruesightTheme {
                LinkStripperApp(sharePayload = sharePayload)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        updateSharePayload(intent)
    }

    private fun updateSharePayload(intent: Intent?) {
        val sharedText = extractSharedText(intent)
        sharePayload = sharedText?.let { SharePayload(id = System.nanoTime(), text = it) }
    }

    private fun extractSharedText(intent: Intent?): String? {
        return com.truesight.truesight.extractSharedText(intent)
    }
}

internal data class SharePayload(
    val id: Long,
    val text: String
)
