package com.truesight.truesight

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.truesight.truesight.shared.CleanerPolicyStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal data class ShareOverlayUiState(
    val isSheetOpen: Boolean = true,
    val originalUrl: String? = null,
    val cleanedUrl: String? = null,
    val hasUrlInInput: Boolean? = null
)

internal class ShareOverlayViewModel(
    private val settingsStore: CleanerPolicyStore
) : ViewModel() {
    private var resolveJob: Job? = null
    private var latestRequestId: Long = 0

    var uiState by mutableStateOf(ShareOverlayUiState())
        private set

    fun onSharedText(sharedText: String?) {
        latestRequestId += 1
        val requestId = latestRequestId
        resolveJob?.cancel()
        resolveJob = viewModelScope.launch {
            val firstUrl = sharedText?.let(UrlExtractor::extractFirstUrl)
            if (requestId != latestRequestId) {
                return@launch
            }

            uiState = uiState.copy(hasUrlInInput = firstUrl != null, originalUrl = firstUrl, cleanedUrl = null)

            if (firstUrl == null) {
                return@launch
            }

            val initialPolicy = settingsStore.loadPolicy()
            var cleaned = UrlCleaner.cleanWithResolvedRedirects(firstUrl, initialPolicy)
            val refreshedPolicy = settingsStore.loadPolicy()
            if (refreshedPolicy != initialPolicy) {
                cleaned = UrlCleaner.cleanWithResolvedRedirects(firstUrl, refreshedPolicy)
            }
            if (requestId == latestRequestId) {
                uiState = uiState.copy(cleanedUrl = cleaned)
            }
        }
    }

    fun dismissSheet() {
        uiState = uiState.copy(isSheetOpen = false)
    }
}

internal class ShareOverlayViewModelFactory(
    context: Context
) : ViewModelProvider.Factory {
    private val appContext = context.applicationContext

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShareOverlayViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShareOverlayViewModel(CleanerSettingsStore(appContext)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
