package dev.yakitori.barkforwarder.domain

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.ContextCompat

object AppPermissions {
    fun hasSmsAccess(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.RECEIVE_SMS) &&
            hasPermission(context, Manifest.permission.READ_SMS)
    }

    fun hasCallAccess(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.READ_PHONE_STATE) &&
            hasPermission(context, Manifest.permission.READ_CALL_LOG)
    }

    fun hasPostNotificationsPermission(context: Context): Boolean {
        return android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU ||
            hasPermission(context, Manifest.permission.POST_NOTIFICATIONS)
    }

    fun hasNotificationListenerAccess(context: Context): Boolean {
        val enabled = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners").orEmpty()
        return enabled.contains(context.packageName)
    }

    private fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}

