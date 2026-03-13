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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.truesight.truesight.ui.theme.TruesightTheme

private enum class ShareSheetContentState {
    NoUrl,
    Cleaning,
    Ready
}

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
                Crossfade(targetState = contentState, label = "share-overlay-state") { state ->
                    when (state) {
                        ShareSheetContentState.NoUrl -> NoUrlContent(onDismiss = onDismiss)
                        ShareSheetContentState.Cleaning -> CleaningContent()
                        ShareSheetContentState.Ready -> {
                            ReadyContent(
                                originalUrl = uiState.originalUrl ?: return@Crossfade,
                                cleanedUrl = uiState.cleanedUrl.orEmpty()
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = contentState == ShareSheetContentState.Ready,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val cleaned = uiState.cleanedUrl ?: return@AnimatedVisibility
                ShareActionButtonsRow(
                    onShare = {
                        shareCleanedUrl(context, cleaned)
                        onDismiss()
                    },
                    onCopy = {
                        copyToClipboard(context, cleaned)
                        onDismiss()
                    },
                    onOpen = {
                        openCleanedUrl(context, cleaned)
                        onDismiss()
                    }
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
            modifier = Modifier
                .weight(1f)
                .testTag("share_overlay_action_share")
        ) {
            Text(text = stringResource(R.string.share))
        }
        OutlinedButton(
            onClick = onCopy,
            modifier = Modifier
                .weight(1f)
                .testTag("share_overlay_action_copy")
        ) {
            Text(text = stringResource(R.string.copy))
        }
        OutlinedButton(
            onClick = onOpen,
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
private fun ReadyContent(originalUrl: String, cleanedUrl: String) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.actions_title),
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
            Text(text = cleanedUrl, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun copyToClipboard(context: Context, text: String) = copyCleanedUrl(context, text)
