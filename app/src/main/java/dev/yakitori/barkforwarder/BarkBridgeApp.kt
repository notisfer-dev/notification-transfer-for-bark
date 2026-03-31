package dev.yakitori.barkforwarder

import android.app.Application

class BarkBridgeApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }

    companion object {
        fun from(context: android.content.Context): BarkBridgeApp {
            return context.applicationContext as BarkBridgeApp
        }
    }
}

