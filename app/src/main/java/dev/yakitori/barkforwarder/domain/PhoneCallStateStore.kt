package dev.yakitori.barkforwarder.domain

import android.content.Context
import android.telephony.TelephonyManager

class PhoneCallStateStore(context: Context) {
    private val preferences = context.getSharedPreferences("phone_state", Context.MODE_PRIVATE)

    var lastState: String
        get() = preferences.getString(KEY_STATE, TelephonyManager.EXTRA_STATE_IDLE).orEmpty()
        set(value) = preferences.edit().putString(KEY_STATE, value).apply()

    var lastIncomingNumber: String?
        get() = preferences.getString(KEY_NUMBER, null)
        set(value) = preferences.edit().putString(KEY_NUMBER, value).apply()

    var callAnswered: Boolean
        get() = preferences.getBoolean(KEY_ANSWERED, false)
        set(value) = preferences.edit().putBoolean(KEY_ANSWERED, value).apply()

    fun reset() {
        preferences.edit()
            .putString(KEY_STATE, TelephonyManager.EXTRA_STATE_IDLE)
            .remove(KEY_NUMBER)
            .putBoolean(KEY_ANSWERED, false)
            .apply()
    }

    private companion object {
        const val KEY_STATE = "state"
        const val KEY_NUMBER = "number"
        const val KEY_ANSWERED = "answered"
    }
}

