package com.truesight.truesight

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.truesight.truesight.ui.theme.TruesightTheme

class ShareOverlayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedText = extractSharedText(intent)
        setContent {
            TruesightTheme {
                ShareActionSheet(
                    sharedText = sharedText,
                    onFinish = { finish() }
                )
            }
        }
    }

    private fun extractSharedText(intent: Intent?): String? {
        return com.truesight.truesight.extractSharedText(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ShareActionSheet(sharedText: String?, onFinish: () -> Unit) {
    val context = LocalContext.current
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    val factory = remember(context) { ShareOverlayViewModelFactory(context) }
    val shareViewModel: ShareOverlayViewModel = viewModel(factory = factory)
    val uiState = shareViewModel.uiState

    LaunchedEffect(sharedText) {
        shareViewModel.onSharedText(sharedText)
    }

    LaunchedEffect(uiState.isSheetOpen) {
        if (!uiState.isSheetOpen) {
            onFinish()
        }
    }

    if (uiState.isSheetOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = shareViewModel::dismissSheet,
            sheetState = sheetState,
            modifier = Modifier.navigationBarsPadding()
        ) {
            val contentState = when {
                uiState.hasUrlInInput == false -> 0
                uiState.cleanedUrl.isNullOrBlank() -> 1
                else -> 2
            }

            Column(
                modifier = Modifier
                    .heightIn(max = screenHeightDp * 0.85f)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Crossfade(targetState = contentState, label = "share-overlay-state") { state ->
                        when (state) {
                            0 -> {
                                Text(
                                    text = context.getString(R.string.status_no_url),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Button(
                                    onClick = shareViewModel::dismissSheet,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = context.getString(R.string.close))
                                }
                            }
                            1 -> {
                                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = context.getString(R.string.status_cleaning),
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                            else -> {
                                val original = uiState.originalUrl ?: return@Crossfade
                                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = context.getString(R.string.actions_title),
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = context.getString(R.string.original_url),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                        Text(
                                            text = original,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        HorizontalDivider()
                                        Text(
                                            text = context.getString(R.string.cleaned_url),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                        Text(
                                            text = uiState.cleanedUrl ?: "",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = contentState == 2,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    val cleaned = uiState.cleanedUrl ?: return@AnimatedVisibility
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    shareCleanedUrl(context, cleaned)
                                    shareViewModel.dismissSheet()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("share_overlay_action_share")
                            ) {
                                Text(text = context.getString(R.string.share))
                            }
                            OutlinedButton(
                                onClick = {
                                    copyToClipboard(context, cleaned)
                                    shareViewModel.dismissSheet()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("share_overlay_action_copy")
                            ) {
                                Text(text = context.getString(R.string.copy))
                            }
                            OutlinedButton(
                                onClick = {
                                    openCleanedUrl(context, cleaned)
                                    shareViewModel.dismissSheet()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("share_overlay_action_open")
                            ) {
                                Text(text = context.getString(R.string.open))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) = copyCleanedUrl(context, text)
