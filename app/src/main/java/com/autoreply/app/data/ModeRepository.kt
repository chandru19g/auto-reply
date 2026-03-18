package com.autoreply.app.data

import android.content.Context
import com.autoreply.app.domain.Mode

class ModeRepository(context: Context) {

    private val prefs = AppPreferences(context)

    val appState = prefs.appState

    fun setActiveMode(mode: Mode) = prefs.setActiveMode(mode)

    fun setSleepingMessages(call: String, sms: String, whatsApp: String) =
        prefs.setSleepingMessages(call, sms, whatsApp)

    fun setWorkingMessages(call: String, sms: String, whatsApp: String) =
        prefs.setWorkingMessages(call, sms, whatsApp)

    fun setDrivingMessages(call: String, sms: String, whatsApp: String) =
        prefs.setDrivingMessages(call, sms, whatsApp)

    fun getUserName(): String = prefs.getUserName()

    fun setUserNameAndInitDefaults(name: String) = prefs.setUserNameAndInitDefaults(name)

    /** For use from receivers and services when process may have been restarted. */
    fun getAppStateSnapshot(context: Context): AppState {
        return AppPreferences(context).getAppStateSnapshot()
    }
}
