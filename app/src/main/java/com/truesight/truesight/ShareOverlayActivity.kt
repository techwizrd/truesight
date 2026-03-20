package com.truesight.truesight

import android.content.Context
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
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.truesight.truesight.ui.theme.TruesightTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class ShareSheetContentState {
    NoUrl,
    Cleaning,
    Ready
}

private const val ACTION_FEEDBACK_DURATION_MILLIS = 300L

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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ShareActionSheet(sharedText: String?, onFinish: () -> Unit) {
    val context = LocalContext.current
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

    ShareOverlaySheet(
        uiState = uiState,
        onDismiss = shareViewModel::dismissSheet
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareOverlaySheet(
    uiState: ShareOverlayUiState,
    onDismiss: () -> Unit
) {
    if (!uiState.isSheetOpen) {
        return
    }

    val context = LocalContext.current
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    val contentState = contentStateFor(uiState)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.navigationBarsPadding()
    ) {
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
                Crossfade(
                    targetState = contentState,
                    animationSpec = tween(durationMillis = 220),
                    label = "share-overlay-state"
                ) { state ->
                    when (state) {
                        ShareSheetContentState.NoUrl -> NoUrlContent(onDismiss = onDismiss)
                        ShareSheetContentState.Cleaning -> CleaningContent()
                        ShareSheetContentState.Ready -> {
                            ReadyContent(
                                originalUrl = uiState.originalUrl ?: return@Crossfade,
                                cleanedUrl = uiState.cleanedUrl.orEmpty(),
                                paramsRemoved = uiState.paramsRemoved,
                                redirectsFollowed = uiState.redirectsFollowed
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = contentState == ShareSheetContentState.Ready,
                enter = fadeIn(animationSpec = tween(durationMillis = 180)) +
                    expandVertically(animationSpec = tween(durationMillis = 220)),
                exit = fadeOut(animationSpec = tween(durationMillis = 120)) +
                    shrinkVertically(animationSpec = tween(durationMillis = 180))
            ) {
                val cleaned = uiState.cleanedUrl ?: return@AnimatedVisibility
                ShareReadyActions(cleanedUrl = cleaned, onDismiss = onDismiss)
            }
        }
    }
}

@Composable
private fun ShareReadyActions(cleanedUrl: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    var actionFeedback by remember { mutableStateOf<Int?>(null) }
    val actionsEnabled = actionFeedback == null

    fun triggerAction(feedbackResId: Int, action: () -> Unit) {
        if (!actionsEnabled) {
            return
        }
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        action()
        actionFeedback = feedbackResId
        scope.launch {
            delay(ACTION_FEEDBACK_DURATION_MILLIS)
            onDismiss()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ShareActionButtonsRow(
            enabled = actionsEnabled,
            onShare = {
                triggerAction(R.string.status_action_shared) {
                    shareCleanedUrl(context, cleanedUrl)
                }
            },
            onCopy = {
                triggerAction(R.string.status_action_copied) {
                    copyToClipboard(context, cleanedUrl)
                }
            },
            onOpen = {
                triggerAction(R.string.status_action_opened) {
                    openCleanedUrl(context, cleanedUrl)
                }
            }
        )
        AnimatedVisibility(visible = actionFeedback != null, enter = fadeIn(), exit = fadeOut()) {
            val actionLabelRes = actionFeedback ?: return@AnimatedVisibility
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                tonalElevation = 1.dp,
                modifier = Modifier.testTag("share_overlay_action_feedback")
            ) {
                Text(
                    text = stringResource(actionLabelRes),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

private fun contentStateFor(uiState: ShareOverlayUiState): ShareSheetContentState {
    return when {
        uiState.hasUrlInInput == false -> ShareSheetContentState.NoUrl
        uiState.cleanedUrl.isNullOrBlank() -> ShareSheetContentState.Cleaning
        else -> ShareSheetContentState.Ready
    }
}

@Composable
private fun ShareActionButtonsRow(
    enabled: Boolean,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onOpen: () -> Unit
) {
    HorizontalDivider()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onShare,
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .testTag("share_overlay_action_share")
        ) {
            Text(text = stringResource(R.string.share))
        }
        OutlinedButton(
            onClick = onCopy,
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .testTag("share_overlay_action_copy")
        ) {
            Text(text = stringResource(R.string.copy))
        }
        OutlinedButton(
            onClick = onOpen,
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .testTag("share_overlay_action_open")
        ) {
            Text(text = stringResource(R.string.open))
        }
    }
}

@Composable
private fun NoUrlContent(onDismiss: () -> Unit) {
    Text(
        text = stringResource(R.string.status_no_url),
        style = MaterialTheme.typography.titleMedium
    )
    Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
        Text(text = stringResource(R.string.close))
    }
}

@Composable
private fun CleaningContent() {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.status_cleaning),
                style = MaterialTheme.typography.titleMedium
            )
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun ReadyContent(
    originalUrl: String,
    cleanedUrl: String,
    paramsRemoved: Int,
    redirectsFollowed: Int
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = cleanedSummaryText(
                    context = LocalContext.current,
                    paramsRemoved = paramsRemoved,
                    redirectsFollowed = redirectsFollowed
                ),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.original_url),
                style = MaterialTheme.typography.labelLarge
            )
            ScrollableUrlText(
                text = originalUrl,
                style = MaterialTheme.typography.bodySmall
            )
            HorizontalDivider()
            Text(
                text = stringResource(R.string.cleaned_url),
                style = MaterialTheme.typography.labelLarge
            )
            ScrollableUrlText(
                text = cleanedUrl,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun copyToClipboard(context: Context, text: String) = copyCleanedUrl(context, text)
