package com.autoreply.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import com.autoreply.app.data.AppPreferences
import com.autoreply.app.data.SessionLogPreferences
import com.autoreply.app.data.WhitelistPreferences
import com.autoreply.app.domain.Mode
import java.lang.reflect.Method

/**
 * Listens for incoming calls. When a mode is active and call reply message is set,
 * rejects the call and sends an SMS to the caller.
 *
 * Call rejection uses TelecomManager.endCall() on Android 9+ (API 28+) which requires
 * ANSWER_PHONE_CALLS permission. Reflection is used as a fallback for older devices.
 */
class CallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        if (state != TelephonyManager.EXTRA_STATE_RINGING) return

        val prefs = AppPreferences(context)
        val appState = prefs.getAppStateSnapshot()
        if (appState.activeMode == Mode.OFF) return

        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            ?: return
        val number = incomingNumber.trim()
        if (number.isEmpty()) return

        // Session log: record who contacted you during an active mode
        SessionLogPreferences(context).recordCall(number, appState.activeMode)

        val replyText = AppPreferences.withAutomatedNote(
            appState.getCallMessageForMode(appState.activeMode)
        )
        if (replyText.isBlank()) return

        // Whitelist: whitelisted contacts always get through
        if (WhitelistPreferences(context).isNumberWhitelisted(number)) {
            Log.d(TAG, "Whitelisted caller $number — skipping auto-reply")
            return
        }

        // Dedup: some OEMs fire RINGING twice for one call; 3-second bucket prevents double reply
        if (DedupHelper.alreadyRepliedToCall(context, number)) {
            Log.d(TAG, "Duplicate RINGING broadcast for $number — skipping")
            return
        }

        // Production rate limit (COOLDOWN_MS = 0 during testing)
        if (!RateLimitHelper.canReplyToCall(context, number)) return

        // Record dedup immediately before async operations to block any second broadcast
        DedupHelper.recordCallReply(context, number)

        try {
            rejectCall(context)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reject call: ${e.message}")
        }

        try {
            sendSmsReply(context, number, replyText)
            RateLimitHelper.recordCallReply(context, number)
            Log.d(TAG, "SMS reply sent to caller $number")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS to caller: ${e.message}")
        }
    }

    private fun rejectCall(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // API 28+: use TelecomManager.endCall() (requires ANSWER_PHONE_CALLS permission)
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
                ?: throw IllegalStateException("TelecomManager unavailable")
            @Suppress("MissingPermission")
            telecomManager.endCall()
        } else {
            // Fallback for older Android versions via reflection
            val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                ?: throw IllegalStateException("TelephonyManager unavailable")
            val method: Method = telephony.javaClass.getDeclaredMethod("getITelephony")
            method.isAccessible = true
            val telephonyInterface = method.invoke(telephony)
                ?: throw IllegalStateException("ITelephony unavailable")
            val endCallMethod = telephonyInterface.javaClass.getDeclaredMethod("endCall")
            endCallMethod.isAccessible = true
            endCallMethod.invoke(telephonyInterface)
        }
    }

    private fun sendSmsReply(context: Context, number: String, text: String) {
        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }
        smsManager?.sendTextMessage(number, null, text, null, null)
            ?: Log.e(TAG, "SmsManager unavailable")
    }

    companion object {
        private const val TAG = "CallReceiver"
    }
}
