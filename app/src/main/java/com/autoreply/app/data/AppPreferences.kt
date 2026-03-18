package com.autoreply.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.autoreply.app.domain.Mode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _appState = MutableStateFlow(loadState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    private fun loadState(): AppState {
        val modeName = prefs.getString(PreferencesKeys.ACTIVE_MODE, Mode.OFF.name) ?: Mode.OFF.name
        val mode = try {
            Mode.valueOf(modeName)
        } catch (_: Exception) {
            Mode.OFF
        }
        return AppState(
            activeMode = mode,
            sleepingCallMessage = prefs.getString(PreferencesKeys.SLEEPING_CALL_MESSAGE, "") ?: "",
            sleepingSmsMessage = prefs.getString(PreferencesKeys.SLEEPING_SMS_MESSAGE, "") ?: "",
            sleepingWhatsAppMessage = prefs.getString(PreferencesKeys.SLEEPING_WHATSAPP_MESSAGE, "") ?: "",
            workingCallMessage = prefs.getString(PreferencesKeys.WORKING_CALL_MESSAGE, "") ?: "",
            workingSmsMessage = prefs.getString(PreferencesKeys.WORKING_SMS_MESSAGE, "") ?: "",
            workingWhatsAppMessage = prefs.getString(PreferencesKeys.WORKING_WHATSAPP_MESSAGE, "") ?: "",
            drivingCallMessage = prefs.getString(PreferencesKeys.DRIVING_CALL_MESSAGE, "") ?: "",
            drivingSmsMessage = prefs.getString(PreferencesKeys.DRIVING_SMS_MESSAGE, "") ?: "",
            drivingWhatsAppMessage = prefs.getString(PreferencesKeys.DRIVING_WHATSAPP_MESSAGE, "") ?: ""
        )
    }

    private fun emitState() {
        _appState.value = loadState()
    }

    fun setActiveMode(mode: Mode) {
        prefs.edit { putString(PreferencesKeys.ACTIVE_MODE, mode.name) }
        emitState()
    }

    fun setSleepingMessages(call: String, sms: String, whatsApp: String) {
        prefs.edit {
            putString(PreferencesKeys.SLEEPING_CALL_MESSAGE, call)
            putString(PreferencesKeys.SLEEPING_SMS_MESSAGE, sms)
            putString(PreferencesKeys.SLEEPING_WHATSAPP_MESSAGE, whatsApp)
        }
        emitState()
    }

    fun setWorkingMessages(call: String, sms: String, whatsApp: String) {
        prefs.edit {
            putString(PreferencesKeys.WORKING_CALL_MESSAGE, call)
            putString(PreferencesKeys.WORKING_SMS_MESSAGE, sms)
            putString(PreferencesKeys.WORKING_WHATSAPP_MESSAGE, whatsApp)
        }
        emitState()
    }

    fun setDrivingMessages(call: String, sms: String, whatsApp: String) {
        prefs.edit {
            putString(PreferencesKeys.DRIVING_CALL_MESSAGE, call)
            putString(PreferencesKeys.DRIVING_SMS_MESSAGE, sms)
            putString(PreferencesKeys.DRIVING_WHATSAPP_MESSAGE, whatsApp)
        }
        emitState()
    }

    fun getUserName(): String =
        prefs.getString(PreferencesKeys.USER_NAME, "") ?: ""

    /**
     * Saves the user's name and, if the messages for a mode are still blank,
     * fills them with personalised defaults. Already-customised messages are
     * never overwritten.
     */
    fun setUserNameAndInitDefaults(name: String) {
        prefs.edit { putString(PreferencesKeys.USER_NAME, name) }

        val trimmed = name.trim()

        // Only write defaults for keys that are still empty
        prefs.edit {
            if ((prefs.getString(PreferencesKeys.SLEEPING_CALL_MESSAGE, "") ?: "").isBlank()) {
                putString(PreferencesKeys.SLEEPING_CALL_MESSAGE, defaultSleepingCall(trimmed))
            }
            if ((prefs.getString(PreferencesKeys.SLEEPING_SMS_MESSAGE, "") ?: "").isBlank()) {
                putString(PreferencesKeys.SLEEPING_SMS_MESSAGE, defaultSleepingSms(trimmed))
            }
            if ((prefs.getString(PreferencesKeys.SLEEPING_WHATSAPP_MESSAGE, "") ?: "").isBlank()) {
                putString(PreferencesKeys.SLEEPING_WHATSAPP_MESSAGE, defaultSleepingWa(trimmed))
            }
            if ((prefs.getString(PreferencesKeys.WORKING_CALL_MESSAGE, "") ?: "").isBlank()) {
                putString(PreferencesKeys.WORKING_CALL_MESSAGE, defaultWorkingCall(trimmed))
            }
            if ((prefs.getString(PreferencesKeys.WORKING_SMS_MESSAGE, "") ?: "").isBlank()) {
                putString(PreferencesKeys.WORKING_SMS_MESSAGE, defaultWorkingSms(trimmed))
            }
            if ((prefs.getString(PreferencesKeys.WORKING_WHATSAPP_MESSAGE, "") ?: "").isBlank()) {
                putString(PreferencesKeys.WORKING_WHATSAPP_MESSAGE, defaultWorkingWa(trimmed))
            }
            if ((prefs.getString(PreferencesKeys.DRIVING_CALL_MESSAGE, "") ?: "").isBlank()) {
                putString(PreferencesKeys.DRIVING_CALL_MESSAGE, defaultDrivingCall(trimmed))
            }
            if ((prefs.getString(PreferencesKeys.DRIVING_SMS_MESSAGE, "") ?: "").isBlank()) {
                putString(PreferencesKeys.DRIVING_SMS_MESSAGE, defaultDrivingSms(trimmed))
            }
            if ((prefs.getString(PreferencesKeys.DRIVING_WHATSAPP_MESSAGE, "") ?: "").isBlank()) {
                putString(PreferencesKeys.DRIVING_WHATSAPP_MESSAGE, defaultDrivingWa(trimmed))
            }
        }
        emitState()
    }

    /** Synchronous snapshot for use from BroadcastReceivers and services. */
    fun getAppStateSnapshot(): AppState = loadState()

    companion object {
        private const val PREFS_NAME = "app_preferences"

        private const val AUTOMATED_NOTE =
            "Note: This is an automated message — please forgive me for that."

        fun withAutomatedNote(text: String): String {
            val trimmed = text.trim()
            if (trimmed.isBlank()) return trimmed
            val lower = trimmed.lowercase()
            if (lower.contains("automated message")) return trimmed
            return "$trimmed\n\n$AUTOMATED_NOTE"
        }

        fun defaultSleepingCall(name: String) =
            withAutomatedNote(
                "hey!! you've reached the right person, it's $name but I'm currently not available. will call you back"
            )

        fun defaultSleepingSms(name: String) =
            withAutomatedNote(
                "hey!! you've reached the right person, it's $name but I'm currently not available. will text you back"
            )

        fun defaultSleepingWa(name: String) =
            withAutomatedNote(
                "hey!! you've reached the right person, it's $name but I'm currently not available. will reply soon 😊"
            )

        fun defaultWorkingCall(name: String) =
            withAutomatedNote(
                "hey!! you've reached the right person, it's $name but I'm currently working intensely so will reach out to you later"
            )

        fun defaultWorkingSms(name: String) =
            withAutomatedNote(
                "hey!! you've reached the right person, it's $name but I'm currently working intensely so will reach out to you later"
            )

        fun defaultWorkingWa(name: String) =
            withAutomatedNote(
                "hey!! you've reached the right person, it's $name but I'm currently working intensely so will reach out to you later 🙏"
            )

        fun defaultDrivingCall(name: String) =
            withAutomatedNote(
                "hey!! you've reached the right person, it's $name but I'm currently driving. will call you back for sure"
            )

        fun defaultDrivingSms(name: String) =
            withAutomatedNote(
                "hey!! you've reached the right person, it's $name but I'm currently driving. will text you back for sure"
            )

        fun defaultDrivingWa(name: String) =
            withAutomatedNote(
                "hey!! you've reached the right person, it's $name but I'm currently driving. will reply soon 🚗"
            )
    }
}

data class AppState(
    val activeMode: Mode,
    val sleepingCallMessage: String,
    val sleepingSmsMessage: String,
    val sleepingWhatsAppMessage: String,
    val workingCallMessage: String,
    val workingSmsMessage: String,
    val workingWhatsAppMessage: String,
    val drivingCallMessage: String,
    val drivingSmsMessage: String,
    val drivingWhatsAppMessage: String
) {
    fun getCallMessageForMode(mode: Mode): String = when (mode) {
        Mode.OFF -> ""
        Mode.SLEEPING -> sleepingCallMessage
        Mode.WORKING -> workingCallMessage
        Mode.DRIVING -> drivingCallMessage
    }

    fun getSmsMessageForMode(mode: Mode): String = when (mode) {
        Mode.OFF -> ""
        Mode.SLEEPING -> sleepingSmsMessage
        Mode.WORKING -> workingSmsMessage
        Mode.DRIVING -> drivingSmsMessage
    }

    fun getWhatsAppMessageForMode(mode: Mode): String = when (mode) {
        Mode.OFF -> ""
        Mode.SLEEPING -> sleepingWhatsAppMessage
        Mode.WORKING -> workingWhatsAppMessage
        Mode.DRIVING -> drivingWhatsAppMessage
    }
}
