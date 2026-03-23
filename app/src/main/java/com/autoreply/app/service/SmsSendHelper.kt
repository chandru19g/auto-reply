package com.autoreply.app.service

import android.content.Context
import android.os.Build
import android.telephony.SmsManager
import android.util.Log

/**
 * Reliable SMS sending from background components (BroadcastReceivers, etc.).
 *
 * - On API 31+ uses [Context.getSystemService] for [SmsManager] when available.
 * - Splits long text with [SmsManager.divideMessage] and uses
 *   [SmsManager.sendMultipartTextMessage] when needed (plain [sendTextMessage] can fail
 *   or behave badly for long / multi-segment texts on some devices).
 *
 * Note: We avoid [SubscriptionManager.getDefaultSmsSubscriptionId] here because some
 * compile SDK / source sets do not expose it; the system [SmsManager] service is enough
 * for typical single-SIM and many dual-SIM setups.
 */
object SmsSendHelper {

    private const val TAG = "SmsSendHelper"

    /**
     * Queues an SMS to [destination]. Returns true if [SmsManager] was obtained and
     * send was invoked without throwing.
     */
    fun sendText(context: Context, destination: String, text: String): Boolean {
        val smsManager = getSmsManager(context) ?: run {
            Log.e(TAG, "SmsManager is null — cannot send SMS to $destination")
            return false
        }
        val trimmedDest = destination.trim()
        if (trimmedDest.isEmpty()) {
            Log.e(TAG, "Empty destination")
            return false
        }
        return try {
            val parts = smsManager.divideMessage(text)
            if (parts.size <= 1) {
                smsManager.sendTextMessage(trimmedDest, null, text, null, null)
            } else {
                smsManager.sendMultipartTextMessage(trimmedDest, null, parts, null, null)
            }
            Log.d(TAG, "SMS queued to $trimmedDest (${parts.size} part(s), ${text.length} chars)")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS to $trimmedDest: ${e.message}", e)
            false
        }
    }

    private fun getSmsManager(context: Context): SmsManager? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
                    ?: @Suppress("DEPRECATION")
                    SmsManager.getDefault()
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
        } catch (e: Exception) {
            Log.w(TAG, "getSmsManager fallback: ${e.message}")
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else null
            } catch (_: Exception) {
                null
            } ?: run {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
        }
    }
}
