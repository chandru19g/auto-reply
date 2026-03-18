package com.autoreply.app.service

import android.content.Context
import android.content.SharedPreferences

private const val PREFS_NAME = "rate_limit_prefs"
private const val KEY_CALL_PREFIX = "call_"
private const val KEY_SMS_PREFIX = "sms_"
private const val KEY_WHATSAPP_PREFIX = "wa_"

// TODO [PRODUCTION]: Change COOLDOWN_MS back to 30 * 60 * 1000L (30 minutes) before releasing to Play Store.
// It is set to 0 for testing so every incoming call/SMS/WhatsApp triggers a reply with no wait.
private const val COOLDOWN_MS = 0L

object RateLimitHelper {

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun canReplyToCall(context: Context, number: String): Boolean {
        if (COOLDOWN_MS == 0L) return true
        val last = prefs(context).getLong(KEY_CALL_PREFIX + number, 0)
        return System.currentTimeMillis() - last > COOLDOWN_MS
    }

    fun recordCallReply(context: Context, number: String) {
        prefs(context).edit().putLong(KEY_CALL_PREFIX + number, System.currentTimeMillis()).apply()
    }

    fun canReplyToSms(context: Context, number: String): Boolean {
        if (COOLDOWN_MS == 0L) return true
        val last = prefs(context).getLong(KEY_SMS_PREFIX + number, 0)
        return System.currentTimeMillis() - last > COOLDOWN_MS
    }

    fun recordSmsReply(context: Context, number: String) {
        prefs(context).edit().putLong(KEY_SMS_PREFIX + number, System.currentTimeMillis()).apply()
    }

    fun canReplyToWhatsApp(context: Context, conversationKey: String): Boolean {
        if (COOLDOWN_MS == 0L) return true
        val last = prefs(context).getLong(KEY_WHATSAPP_PREFIX + conversationKey, 0)
        return System.currentTimeMillis() - last > COOLDOWN_MS
    }

    fun recordWhatsAppReply(context: Context, conversationKey: String) {
        prefs(context).edit().putLong(KEY_WHATSAPP_PREFIX + conversationKey, System.currentTimeMillis()).apply()
    }
}
