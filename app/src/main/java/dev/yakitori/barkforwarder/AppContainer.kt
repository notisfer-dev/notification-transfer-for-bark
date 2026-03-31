package dev.yakitori.barkforwarder

import android.content.Context
import dev.yakitori.barkforwarder.data.db.AppDatabase
import dev.yakitori.barkforwarder.data.prefs.SecureSettingsStore
import dev.yakitori.barkforwarder.data.prefs.SettingsRepository
import dev.yakitori.barkforwarder.data.repo.AppRuleRepository
import dev.yakitori.barkforwarder.data.repo.DedupeRepository
import dev.yakitori.barkforwarder.data.repo.InstalledAppRepository
import dev.yakitori.barkforwarder.domain.BarkPushClient
import dev.yakitori.barkforwarder.domain.ForwardingEngine
import dev.yakitori.barkforwarder.domain.PlayIconResolver
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database by lazy { AppDatabase.create(appContext) }
    private val httpClient by lazy {
        OkHttpClient.Builder()
            .callTimeout(15, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    val secureSettingsStore by lazy { SecureSettingsStore(appContext) }
    val settingsRepository by lazy { SettingsRepository(appContext, secureSettingsStore) }
    val appRuleRepository by lazy {
        AppRuleRepository(
            packageManager = appContext.packageManager,
            selfPackageName = appContext.packageName,
            dao = database.appRuleDao(),
        )
    }
    val installedAppRepository by lazy { InstalledAppRepository(appContext, appRuleRepository) }
    val dedupeRepository by lazy { DedupeRepository(database.dedupeDao()) }
    val playIconResolver by lazy { PlayIconResolver(httpClient, appRuleRepository) }
    val barkPushClient by lazy { BarkPushClient(httpClient) }
    val forwardingEngine by lazy {
        ForwardingEngine(
            settingsRepository = settingsRepository,
            appRuleRepository = appRuleRepository,
            dedupeRepository = dedupeRepository,
            playIconResolver = playIconResolver,
            barkPushClient = barkPushClient,
        )
    }
}

