package dev.yakitori.barkforwarder.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.yakitori.barkforwarder.data.model.BarkConfig
import dev.yakitori.barkforwarder.data.model.CryptoConfig
import dev.yakitori.barkforwarder.data.model.NotificationFilterConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(
    private val context: Context,
    private val secureSettingsStore: SecureSettingsStore,
) {
    val barkConfig: Flow<BarkConfig> = context.dataStore.data.map { prefs ->
        BarkConfig(
            serverUrl = prefs[Keys.SERVER_URL] ?: DEFAULT_SERVER_URL,
            deviceKey = secureSettingsStore.decrypt(prefs[Keys.DEVICE_KEY]),
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            smsEnabled = prefs[Keys.SMS_ENABLED] ?: true,
            callsEnabled = prefs[Keys.CALLS_ENABLED] ?: true,
            ringIncomingCalls = prefs[Keys.RING_INCOMING_CALLS] ?: true,
        )
    }

    val cryptoConfig: Flow<CryptoConfig> = context.dataStore.data.map { prefs ->
        CryptoConfig(
            enabled = prefs[Keys.ENCRYPTION_ENABLED] ?: true,
            key = secureSettingsStore.decrypt(prefs[Keys.ENCRYPTION_KEY]),
            fixedIv = secureSettingsStore.decrypt(prefs[Keys.FIXED_IV]),
        )
    }

    val notificationFilterConfig: Flow<NotificationFilterConfig> = context.dataStore.data.map { prefs ->
        NotificationFilterConfig(
            duplicateWindowSeconds = (prefs[Keys.DUPLICATE_WINDOW_SECONDS] ?: 5).coerceIn(1, 120),
        )
    }

    suspend fun updateBarkConfig(config: BarkConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SERVER_URL] = config.serverUrl.ifBlank { DEFAULT_SERVER_URL }
            prefs[Keys.DEVICE_KEY] = encodeSecret(config.deviceKey)
            prefs[Keys.NOTIFICATIONS_ENABLED] = config.notificationsEnabled
            prefs[Keys.SMS_ENABLED] = config.smsEnabled
            prefs[Keys.CALLS_ENABLED] = config.callsEnabled
            prefs[Keys.RING_INCOMING_CALLS] = config.ringIncomingCalls
        }
    }

    suspend fun updateCryptoConfig(config: CryptoConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ENCRYPTION_ENABLED] = config.enabled
            prefs[Keys.ENCRYPTION_KEY] = encodeSecret(config.key)
            prefs[Keys.FIXED_IV] = encodeSecret(config.fixedIv)
        }
    }

    suspend fun updateNotificationFilterConfig(config: NotificationFilterConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DUPLICATE_WINDOW_SECONDS] = config.duplicateWindowSeconds.coerceIn(1, 120)
        }
    }

    private fun encodeSecret(value: String): String {
        return if (value.isBlank()) "" else secureSettingsStore.encrypt(value)
    }

    private object Keys {
        val SERVER_URL: Preferences.Key<String> = stringPreferencesKey("server_url")
        val DEVICE_KEY: Preferences.Key<String> = stringPreferencesKey("device_key")
        val ENCRYPTION_KEY: Preferences.Key<String> = stringPreferencesKey("encryption_key")
        val FIXED_IV: Preferences.Key<String> = stringPreferencesKey("fixed_iv")
        val ENCRYPTION_ENABLED: Preferences.Key<Boolean> = booleanPreferencesKey("encryption_enabled")
        val NOTIFICATIONS_ENABLED: Preferences.Key<Boolean> = booleanPreferencesKey("notifications_enabled")
        val SMS_ENABLED: Preferences.Key<Boolean> = booleanPreferencesKey("sms_enabled")
        val CALLS_ENABLED: Preferences.Key<Boolean> = booleanPreferencesKey("calls_enabled")
        val RING_INCOMING_CALLS: Preferences.Key<Boolean> = booleanPreferencesKey("ring_incoming_calls")
        val DUPLICATE_WINDOW_SECONDS: Preferences.Key<Int> = intPreferencesKey("duplicate_window_seconds")
    }

    companion object {
        const val DEFAULT_SERVER_URL = "https://api.day.app/push"
    }
}
