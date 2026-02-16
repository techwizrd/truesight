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

internal enum class CleanerStatus {
    ManualHint,
    NoUrl,
    AlreadyClean,
    Cleaned
}

internal enum class AppScreen {
    Cleaner,
    Settings
}

internal data class CleanerUiState(
    val inputText: String = "",
    val status: CleanerStatus = CleanerStatus.ManualHint,
    val originalUrl: String = "",
    val cleanedUrl: String = "",
    val showActionSheet: Boolean = false,
    val policy: CleanerPolicy = CleanerPolicy(),
    val currentScreen: AppScreen = AppScreen.Cleaner,
    val isCleaning: Boolean = false
)

internal class CleanerViewModel(
    private val settingsStore: CleanerPolicyStore
) : ViewModel() {
    private var cleaningJob: Job? = null
    private var latestRequestId: Long = 0

    var uiState by mutableStateOf(
        CleanerUiState(policy = settingsStore.loadPolicy())
    )
        private set

    fun onInputTextChanged(input: String) {
        uiState = uiState.copy(inputText = input)
    }

    fun onIncomingShareText(text: String) {
        uiState = uiState.copy(inputText = text)
        cleanFirstUrlFromText(text)
    }

    fun cleanFromCurrentInput() {
        cleanFirstUrlFromText(uiState.inputText)
    }

    fun openSettings() {
        uiState = uiState.copy(currentScreen = AppScreen.Settings)
    }

    fun backToCleaner() {
        uiState = uiState.copy(currentScreen = AppScreen.Cleaner)
    }

    fun dismissActionSheet() {
        uiState = uiState.copy(showActionSheet = false)
    }

    fun onPolicyChanged(updatedPolicy: CleanerPolicy) {
        cleaningJob?.cancel()
        settingsStore.savePolicy(updatedPolicy)
        RemoteRedirectFollower.invalidateCache()
        uiState = uiState.copy(policy = updatedPolicy, isCleaning = false)
    }

    private fun cleanFirstUrlFromText(sourceText: String) {
        latestRequestId += 1
        val requestId = latestRequestId
        cleaningJob?.cancel()
        cleaningJob = viewModelScope.launch {
            uiState = uiState.copy(isCleaning = true)
            val firstUrl = UrlExtractor.extractFirstUrl(sourceText)
            if (firstUrl == null) {
                if (requestId == latestRequestId) {
                    uiState = uiState.copy(
                        status = CleanerStatus.NoUrl,
                        isCleaning = false
                    )
                }
                return@launch
            }

            val cleaned = UrlCleaner.cleanWithResolvedRedirects(firstUrl, uiState.policy)
            if (requestId == latestRequestId) {
                uiState = uiState.copy(
                    originalUrl = firstUrl,
                    cleanedUrl = cleaned,
                    status = if (cleaned == firstUrl) CleanerStatus.AlreadyClean else CleanerStatus.Cleaned,
                    showActionSheet = true,
                    isCleaning = false
                )
            }
        }
    }
}

internal class CleanerViewModelFactory(
    context: Context
) : ViewModelProvider.Factory {
    private val appContext = context.applicationContext

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CleanerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CleanerViewModel(CleanerSettingsStore(appContext)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
