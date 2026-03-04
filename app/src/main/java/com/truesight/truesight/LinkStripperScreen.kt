package com.truesight.truesight

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.truesight.truesight.ui.theme.TruesightTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LinkStripperApp(
    sharePayload: SharePayload?
) {
    val context = LocalContext.current
    val factory = remember(context) { CleanerViewModelFactory(context) }
    val cleanerViewModel: CleanerViewModel = viewModel(factory = factory)
    val uiState = cleanerViewModel.uiState

    BackHandler(enabled = uiState.currentScreen == AppScreen.Settings) {
        cleanerViewModel.backToCleaner()
    }

    LaunchedEffect(sharePayload?.id) {
        val text = sharePayload?.text ?: return@LaunchedEffect
        cleanerViewModel.onIncomingShareText(text)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (uiState.currentScreen == AppScreen.Cleaner) {
                            stringResourceSafe(context, R.string.app_name)
                        } else {
                            stringResourceSafe(context, R.string.cleaning_preferences_title)
                        }
                    )
                },
                navigationIcon = {
                    if (uiState.currentScreen == AppScreen.Settings) {
                        IconButton(onClick = cleanerViewModel::backToCleaner) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResourceSafe(context, R.string.cd_back_to_cleaner)
                            )
                        }
                    }
                },
                actions = {
                    if (uiState.currentScreen == AppScreen.Cleaner) {
                        IconButton(onClick = cleanerViewModel::openSettings) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResourceSafe(context, R.string.cd_open_settings)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Crossfade(targetState = uiState.currentScreen, label = "app-screen") { screen ->
                when (screen) {
                    AppScreen.Cleaner -> {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .animateContentSize(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = stringResourceSafe(context, R.string.manual_mode_hint),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    AssistChip(
                                        onClick = {},
                                        enabled = false,
                                        label = {
                                            Crossfade(targetState = uiState.status, label = "status-chip") { status ->
                                                Text(
                                                    text = statusTextFor(context, status),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    )
                                    if (uiState.isCleaning) {
                                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                    }
                                }
                            }

                            Text(
                                text = stringResourceSafe(context, R.string.input_label),
                                style = MaterialTheme.typography.titleSmall
                            )

                            OutlinedTextField(
                                value = uiState.inputText,
                                onValueChange = cleanerViewModel::onInputTextChanged,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(text = stringResourceSafe(context, R.string.input_label)) },
                                placeholder = { Text(text = stringResourceSafe(context, R.string.paste_hint)) },
                                minLines = 3
                            )

                            Button(
                                onClick = cleanerViewModel::cleanFromCurrentInput,
                                enabled = uiState.inputText.isNotBlank() && !uiState.isCleaning,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = stringResourceSafe(context, R.string.clean_link))
                            }

                            AnimatedVisibility(
                                visible = uiState.cleanedUrl.isNotBlank(),
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    ResultCard(
                                        originalUrl = uiState.originalUrl,
                                        cleanedUrl = uiState.cleanedUrl
                                    )
                                    ResultActionsRow(
                                        context = context,
                                        cleanedUrl = uiState.cleanedUrl,
                                        onClose = cleanerViewModel::dismissActionSheet
                                    )
                                }
                            }
                        }
                    }

                    AppScreen.Settings -> {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CleaningPreferencesCard(
                                        policy = uiState.policy,
                                        onPolicyChanged = cleanerViewModel::onPolicyChanged
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.showActionSheet && uiState.cleanedUrl.isNotBlank()) {
        ModalBottomSheet(
            onDismissRequest = cleanerViewModel::dismissActionSheet,
            modifier = Modifier.navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResourceSafe(context, R.string.actions_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResourceSafe(context, R.string.original_url),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = uiState.originalUrl,
                    style = MaterialTheme.typography.bodySmall
                )
                HorizontalDivider()
                Text(
                    text = stringResourceSafe(context, R.string.cleaned_url),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = uiState.cleanedUrl,
                    style = MaterialTheme.typography.bodyMedium
                )
                ResultActionsRow(
                    context = context,
                    cleanedUrl = uiState.cleanedUrl,
                    onClose = cleanerViewModel::dismissActionSheet
                )
            }
        }
    }
}

@Composable
private fun ResultCard(originalUrl: String, cleanedUrl: String) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.original_url),
                style = MaterialTheme.typography.labelLarge
            )
            Text(text = originalUrl, style = MaterialTheme.typography.bodySmall)
            HorizontalDivider()
            Text(
                text = stringResource(R.string.cleaned_url),
                style = MaterialTheme.typography.labelLarge
            )
            Text(text = cleanedUrl, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ResultActionsRow(
    context: Context,
    cleanedUrl: String,
    onClose: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = {
                shareCleanedUrl(context, cleanedUrl, launchAsNewTask = true)
                onClose()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResourceSafe(context, R.string.share))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    copyCleanedUrl(context, cleanedUrl)
                    onClose()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResourceSafe(context, R.string.copy))
            }
            OutlinedButton(
                onClick = {
                    openCleanedUrl(context, cleanedUrl, launchAsNewTask = true)
                    onClose()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResourceSafe(context, R.string.open))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LinkStripperPreview() {
    TruesightTheme {
        LinkStripperApp(sharePayload = null)
    }
}

@Composable
private fun CleaningPreferencesCard(
    policy: CleanerPolicy,
    onPolicyChanged: (CleanerPolicy) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        domainPolicySections.forEachIndexed { index, section ->
            DomainSettingsSection(
                titleResId = section.titleResId,
                redirectEnabled = section.redirectEnabled(policy),
                stripEnabled = section.stripEnabled(policy),
                onRedirectChanged = { enabled -> onPolicyChanged(section.updateRedirect(policy, enabled)) },
                onStripChanged = { enabled -> onPolicyChanged(section.updateStrip(policy, enabled)) }
            )
            if (section.showAmazonAffiliateToggle) {
                SettingsToggleRow(
                    labelResId = R.string.setting_remove_amazon_affiliate_tag,
                    checked = policy.amazonRemoveAffiliateTagEnabled,
                    onCheckedChange = { enabled ->
                        onPolicyChanged(policy.copy(amazonRemoveAffiliateTagEnabled = enabled))
                    }
                )
            }
            if (index != domainPolicySections.lastIndex) {
                HorizontalDivider()
            }
        }
        HorizontalDivider()
        SettingsToggleRow(
            labelResId = R.string.setting_twitter_to_nitter,
            checked = policy.twitterToNitterEnabled,
            onCheckedChange = { onPolicyChanged(policy.copy(twitterToNitterEnabled = it)) }
        )
        HorizontalDivider()
        Text(
            text = stringResource(R.string.settings_general_tracking_title),
            style = MaterialTheme.typography.titleSmall
        )
        SettingsToggleRow(
            labelResId = R.string.setting_strip_utm,
            checked = policy.utmTrackingStripEnabled,
            onCheckedChange = { onPolicyChanged(policy.copy(utmTrackingStripEnabled = it)) }
        )
        Text(
            text = stringResource(R.string.setting_strip_utm_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider()
        Text(
            text = stringResource(R.string.settings_ad_tracking_title),
            style = MaterialTheme.typography.titleSmall
        )
        AdVendorSettingsSection(titleResId = R.string.vendor_google_ads) {
            SettingsToggleRow(
                labelResId = R.string.setting_strip_google_ads,
                checked = policy.adTracking.googleEnabled,
                onCheckedChange = {
                    onPolicyChanged(
                        policy.copy(adTracking = policy.adTracking.copy(googleEnabled = it))
                    )
                }
            )
            SettingsToggleRow(
                labelResId = R.string.setting_aggressive_google_ads_stripping,
                checked = policy.adTracking.googleAggressiveEnabled,
                enabled = policy.adTracking.googleEnabled,
                onCheckedChange = {
                    onPolicyChanged(
                        policy.copy(adTracking = policy.adTracking.copy(googleAggressiveEnabled = it))
                    )
                }
            )
        }
        AdVendorSettingsSection(titleResId = R.string.vendor_meta_ads) {
            SettingsToggleRow(
                labelResId = R.string.setting_strip_meta_ads,
                checked = policy.adTracking.metaEnabled,
                onCheckedChange = {
                    onPolicyChanged(policy.copy(adTracking = policy.adTracking.copy(metaEnabled = it)))
                }
            )
        }
        AdVendorSettingsSection(titleResId = R.string.vendor_microsoft_ads) {
            SettingsToggleRow(
                labelResId = R.string.setting_strip_microsoft_ads,
                checked = policy.adTracking.microsoftEnabled,
                onCheckedChange = {
                    onPolicyChanged(policy.copy(adTracking = policy.adTracking.copy(microsoftEnabled = it)))
                }
            )
        }
        AdVendorSettingsSection(titleResId = R.string.vendor_tiktok_ads) {
            SettingsToggleRow(
                labelResId = R.string.setting_strip_tiktok_ads,
                checked = policy.adTracking.tiktokEnabled,
                onCheckedChange = {
                    onPolicyChanged(policy.copy(adTracking = policy.adTracking.copy(tiktokEnabled = it)))
                }
            )
        }
        AdVendorSettingsSection(titleResId = R.string.vendor_twitter_ads) {
            SettingsToggleRow(
                labelResId = R.string.setting_strip_twitter_ads,
                checked = policy.adTracking.twitterEnabled,
                onCheckedChange = {
                    onPolicyChanged(policy.copy(adTracking = policy.adTracking.copy(twitterEnabled = it)))
                }
            )
        }
        AdVendorSettingsSection(titleResId = R.string.vendor_linkedin_ads) {
            SettingsToggleRow(
                labelResId = R.string.setting_strip_linkedin_ads,
                checked = policy.adTracking.linkedInEnabled,
                onCheckedChange = {
                    onPolicyChanged(policy.copy(adTracking = policy.adTracking.copy(linkedInEnabled = it)))
                }
            )
        }
        AdVendorSettingsSection(titleResId = R.string.vendor_pinterest_ads) {
            SettingsToggleRow(
                labelResId = R.string.setting_strip_pinterest_ads,
                checked = policy.adTracking.pinterestEnabled,
                onCheckedChange = {
                    onPolicyChanged(policy.copy(adTracking = policy.adTracking.copy(pinterestEnabled = it)))
                }
            )
        }
        AdVendorSettingsSection(titleResId = R.string.vendor_snapchat_ads) {
            SettingsToggleRow(
                labelResId = R.string.setting_strip_snapchat_ads,
                checked = policy.adTracking.snapchatEnabled,
                onCheckedChange = {
                    onPolicyChanged(policy.copy(adTracking = policy.adTracking.copy(snapchatEnabled = it)))
                }
            )
        }
    }
}

@Composable
private fun AdVendorSettingsSection(
    titleResId: Int,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(titleResId),
            style = MaterialTheme.typography.labelLarge
        )
        content()
    }
}

@Composable
private fun DomainSettingsSection(
    titleResId: Int,
    redirectEnabled: Boolean,
    stripEnabled: Boolean,
    onRedirectChanged: (Boolean) -> Unit,
    onStripChanged: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(titleResId),
            style = MaterialTheme.typography.labelLarge
        )
        SettingsToggleRow(
            labelResId = R.string.setting_follow_redirects,
            checked = redirectEnabled,
            onCheckedChange = onRedirectChanged
        )
        SettingsToggleRow(
            labelResId = R.string.setting_strip_parameters,
            checked = stripEnabled,
            onCheckedChange = onStripChanged
        )
    }
}

@Composable
private fun SettingsToggleRow(
    labelResId: Int,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = stringResource(labelResId), style = MaterialTheme.typography.bodyMedium)
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange
        )
    }
}

private data class DomainPolicySection(
    val titleResId: Int,
    val redirectEnabled: (CleanerPolicy) -> Boolean,
    val stripEnabled: (CleanerPolicy) -> Boolean,
    val updateRedirect: (CleanerPolicy, Boolean) -> CleanerPolicy,
    val updateStrip: (CleanerPolicy, Boolean) -> CleanerPolicy,
    val showAmazonAffiliateToggle: Boolean = false
)

private val domainPolicySections = listOf(
    DomainPolicySection(
        titleResId = R.string.domain_google_share,
        redirectEnabled = { it.googleShareRedirectEnabled },
        stripEnabled = { it.googleShareStripEnabled },
        updateRedirect = { policy, enabled -> policy.copy(googleShareRedirectEnabled = enabled) },
        updateStrip = { policy, enabled -> policy.copy(googleShareStripEnabled = enabled) }
    ),
    DomainPolicySection(
        titleResId = R.string.domain_reddit,
        redirectEnabled = { it.redditRedirectEnabled },
        stripEnabled = { it.redditStripEnabled },
        updateRedirect = { policy, enabled -> policy.copy(redditRedirectEnabled = enabled) },
        updateStrip = { policy, enabled -> policy.copy(redditStripEnabled = enabled) }
    ),
    DomainPolicySection(
        titleResId = R.string.domain_amazon,
        redirectEnabled = { it.amazonRedirectEnabled },
        stripEnabled = { it.amazonStripEnabled },
        updateRedirect = { policy, enabled -> policy.copy(amazonRedirectEnabled = enabled) },
        updateStrip = { policy, enabled -> policy.copy(amazonStripEnabled = enabled) },
        showAmazonAffiliateToggle = true
    ),
    DomainPolicySection(
        titleResId = R.string.domain_instagram,
        redirectEnabled = { it.instagramRedirectEnabled },
        stripEnabled = { it.instagramStripEnabled },
        updateRedirect = { policy, enabled -> policy.copy(instagramRedirectEnabled = enabled) },
        updateStrip = { policy, enabled -> policy.copy(instagramStripEnabled = enabled) }
    ),
    DomainPolicySection(
        titleResId = R.string.domain_amp_cache,
        redirectEnabled = { it.ampCacheRedirectEnabled },
        stripEnabled = { it.ampCacheStripEnabled },
        updateRedirect = { policy, enabled -> policy.copy(ampCacheRedirectEnabled = enabled) },
        updateStrip = { policy, enabled -> policy.copy(ampCacheStripEnabled = enabled) }
    )
)

private fun statusTextFor(context: Context, status: CleanerStatus): String {
    return when (status) {
        CleanerStatus.ManualHint -> context.getString(R.string.manual_mode_hint)
        CleanerStatus.NoUrl -> context.getString(R.string.status_no_url)
        CleanerStatus.AlreadyClean -> context.getString(R.string.status_already_clean)
        CleanerStatus.Cleaned -> context.getString(R.string.status_cleaned)
    }
}

private fun stringResourceSafe(context: Context, resId: Int): String = context.getString(resId)
