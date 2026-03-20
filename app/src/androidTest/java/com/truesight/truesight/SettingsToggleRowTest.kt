package com.truesight.truesight

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.platform.testTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.truesight.truesight.ui.theme.TruesightTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsToggleRowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun tappingLabelRowTogglesSwitchState() {
        var checked by mutableStateOf(false)

        composeRule.setContent {
            TruesightTheme {
                SettingsToggleRow(
                    labelResId = R.string.setting_strip_utm,
                    checked = checked,
                    modifier = Modifier.testTag("settings_toggle_row"),
                    onCheckedChange = { checked = it }
                )
            }
        }

        composeRule.onNodeWithTag("settings_toggle_row").performClick()
        composeRule.runOnIdle { assertTrue(checked) }

        composeRule.onNodeWithTag("settings_toggle_row").performClick()
        composeRule.runOnIdle { assertFalse(checked) }
    }
}
