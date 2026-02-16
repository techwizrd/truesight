package com.truesight.truesight

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri

internal fun extractSharedText(intent: Intent?): String? {
    if (intent?.action != Intent.ACTION_SEND) {
        return null
    }

    val type = intent.type ?: return null
    if (!type.startsWith("text/")) {
        return null
    }

    return intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()?.takeIf { it.isNotEmpty() }
}

internal fun copyCleanedUrl(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("cleaned_url", text))
    Toast.makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
}

internal fun shareCleanedUrl(context: Context, url: String, launchAsNewTask: Boolean = false) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, url)
    }

    val chooser = Intent.createChooser(sendIntent, context.getString(R.string.share_clean_link))
    if (launchAsNewTask) {
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    context.startActivity(chooser)
}

internal fun openCleanedUrl(context: Context, url: String, launchAsNewTask: Boolean = false) {
    try {
        val viewIntent = Intent(Intent.ACTION_VIEW, url.toUri())
        if (launchAsNewTask) {
            viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(viewIntent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, context.getString(R.string.unable_to_open_link), Toast.LENGTH_SHORT).show()
    }
}
