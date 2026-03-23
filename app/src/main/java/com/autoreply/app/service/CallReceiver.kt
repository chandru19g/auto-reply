package com.autoreply.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import com.autoreply.app.data.AppPreferences
import com.autoreply.app.data.SessionLogPreferences
import com.autoreply.app.data.WhitelistPreferences
import com.autoreply.app.domain.Mode
import java.lang.reflect.Method
import java.util.concurrent.Executors

/**
 * Listens for incoming calls. When a mode is active and call reply message is set,
 * sends an SMS to the caller and rejects the call.
 *
 * SMS is sent **before** ending the call — some devices/carriers behave badly if you
 * end the call first. Sending uses [SmsSendHelper] (multipart + correct SmsManager).
 *
 * [goAsync] keeps the receiver alive until SMS is queued (avoids the process dying
 * right after [onReceive] returns).
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

        SessionLogPreferences(context).recordCall(number, appState.activeMode)

        val replyText = AppPreferences.withAutomatedNote(
            appState.getCallMessageForMode(appState.activeMode)
        )
        if (replyText.isBlank()) return

        if (WhitelistPreferences(context).isNumberWhitelisted(number)) {
            Log.d(TAG, "Whitelisted caller $number — skipping auto-reply")
            return
        }

        if (DedupHelper.alreadyRepliedToCall(context, number)) {
            Log.d(TAG, "Duplicate RINGING broadcast for $number — skipping")
            return
        }

        if (!RateLimitHelper.canReplyToCall(context, number)) return

        val appCtx = context.applicationContext
        val pendingResult = goAsync()

        executor.execute {
            try {
                // 1) Queue SMS first (before ending the call)
                val sent = SmsSendHelper.sendText(appCtx, number, replyText)
                if (sent) {
                    DedupHelper.recordCallReply(appCtx, number)
                    RateLimitHelper.recordCallReply(appCtx, number)
                } else {
                    Log.e(TAG, "SMS not queued for $number — dedup not recorded (retry possible)")
                }

                // 2) End call after SMS is queued
                try {
                    rejectCall(appCtx)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to reject call: ${e.message}")
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun rejectCall(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
                ?: throw IllegalStateException("TelecomManager unavailable")
            @Suppress("MissingPermission")
            telecomManager.endCall()
        } else {
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

    companion object {
        private const val TAG = "CallReceiver"
        private val executor = Executors.newSingleThreadExecutor()
    }
}
