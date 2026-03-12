package com.truesight.truesight

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.truesight.truesight.ui.theme.TruesightTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalTestApi::class)
class ShareOverlayActivityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun actionButtonsRemainVisibleForLongUrls() {
        val longUrl = buildString {
            append("https://example.com/path?")
            repeat(1200) { index ->
                append("utm_param")
                append(index)
                append("=value")
                append(index)
                append('&')
            }
            append("id=42")
        }

        composeRule.setContent {
            TruesightTheme {
                ShareActionSheet(sharedText = longUrl, onFinish = {})
            }
        }

        composeRule.waitUntilAtLeastOneExists(
            hasTestTag("share_overlay_action_share"),
            timeoutMillis = 10_000
        )

        composeRule.waitUntil(timeoutMillis = 10_000) {
            runCatching {
                composeRule.onNodeWithTag("share_overlay_action_share").assertIsDisplayed()
            }.isSuccess
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            runCatching {
                composeRule.onNodeWithTag("share_overlay_action_copy").assertIsDisplayed()
            }.isSuccess
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            runCatching {
                composeRule.onNodeWithTag("share_overlay_action_open").assertIsDisplayed()
            }.isSuccess
        }

        composeRule.onNodeWithTag("share_overlay_action_share").assertIsDisplayed()
        composeRule.onNodeWithTag("share_overlay_action_copy").assertIsDisplayed()
        composeRule.onNodeWithTag("share_overlay_action_open").assertIsDisplayed()
    }
}
