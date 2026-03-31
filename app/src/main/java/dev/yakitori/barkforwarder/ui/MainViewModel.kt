package dev.yakitori.barkforwarder.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.yakitori.barkforwarder.BarkBridgeApp
import dev.yakitori.barkforwarder.data.model.AppRule
import dev.yakitori.barkforwarder.data.model.BarkConfig
import dev.yakitori.barkforwarder.data.model.CryptoConfig
import dev.yakitori.barkforwarder.data.model.NotificationFilterConfig
import dev.yakitori.barkforwarder.domain.AppPermissions
import dev.yakitori.barkforwarder.domain.BarkSettingsValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val container = BarkBridgeApp.from(application).container
    private val _searchQuery = MutableStateFlow("")

    val barkConfig = container.settingsRepository.barkConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BarkConfig())
    val cryptoConfig = container.settingsRepository.cryptoConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CryptoConfig())
    val notificationFilterConfig = container.settingsRepository.notificationFilterConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificationFilterConfig())

    val searchQuery: StateFlow<String> = _searchQuery

    private val _permissionSnapshot = MutableStateFlow(readPermissionSnapshot())
    val permissionSnapshot: StateFlow<PermissionSnapshot> = _permissionSnapshot
    private val _barkSettingsMessage = MutableStateFlow<String?>(null)
    val barkSettingsMessage: StateFlow<String?> = _barkSettingsMessage

    val filteredRules: StateFlow<List<AppRule>> = combine(
        container.installedAppRepository.observeRules(),
        _searchQuery,
    ) { rules, query ->
        if (query.isBlank()) rules
        else rules.filter {
            it.appLabel.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun refreshInstalledApps() {
        viewModelScope.launch {
            container.installedAppRepository.refreshInstalledApps()
        }
    }

    fun refreshPermissionSnapshot() {
        _permissionSnapshot.value = readPermissionSnapshot()
    }

    fun saveBarkSettings(barkUrlOrKey: String, cryptoKey: String, fixedIv: String) {
        viewModelScope.launch {
            runCatching {
                BarkSettingsValidator.validate(
                    barkUrlOrKey = barkUrlOrKey,
                    cryptoKey = cryptoKey,
                    fixedIv = fixedIv,
                )
            }.onSuccess { validated ->
                val current = barkConfig.value
                container.settingsRepository.updateBarkConfig(
                    current.copy(
                        serverUrl = validated.endpoint.serverUrl,
                        deviceKey = validated.endpoint.deviceKey,
                    ),
                )
                container.settingsRepository.updateCryptoConfig(
                    cryptoConfig.value.copy(
                        key = validated.cryptoKey,
                        fixedIv = validated.fixedIv,
                    ),
                )
                _barkSettingsMessage.value = "Saved Bark settings."
            }.onFailure { error ->
                _barkSettingsMessage.value = error.message ?: "Could not save Bark settings."
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) = updateBarkConfig { copy(notificationsEnabled = enabled) }
    fun setSmsEnabled(enabled: Boolean) = updateBarkConfig { copy(smsEnabled = enabled) }
    fun setCallsEnabled(enabled: Boolean) = updateBarkConfig { copy(callsEnabled = enabled) }
    fun setRingIncomingCalls(enabled: Boolean) = updateBarkConfig { copy(ringIncomingCalls = enabled) }

    fun setDuplicateWindowSeconds(seconds: Int) {
        viewModelScope.launch {
            container.settingsRepository.updateNotificationFilterConfig(NotificationFilterConfig(seconds.coerceIn(1, 120)))
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setAppExcluded(packageName: String, excluded: Boolean) {
        viewModelScope.launch {
            container.appRuleRepository.updateExcluded(packageName, excluded)
        }
    }

    fun updateManualIconUrl(packageName: String, manualUrl: String?) {
        viewModelScope.launch {
            container.appRuleRepository.updateManualIconUrl(packageName, manualUrl)
        }
    }

    private fun updateBarkConfig(transform: BarkConfig.() -> BarkConfig) {
        viewModelScope.launch {
            container.settingsRepository.updateBarkConfig(barkConfig.value.transform())
        }
    }

    private fun readPermissionSnapshot(): PermissionSnapshot {
        val context = getApplication<Application>()
        return PermissionSnapshot(
            notificationListenerEnabled = AppPermissions.hasNotificationListenerAccess(context),
            smsPermissionsGranted = AppPermissions.hasSmsAccess(context),
            callPermissionsGranted = AppPermissions.hasCallAccess(context),
            postNotificationsGranted = AppPermissions.hasPostNotificationsPermission(context),
        )
    }

    data class PermissionSnapshot(
        val notificationListenerEnabled: Boolean,
        val smsPermissionsGranted: Boolean,
        val callPermissionsGranted: Boolean,
        val postNotificationsGranted: Boolean,
    )
}
