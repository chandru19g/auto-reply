package com.autoreply.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.autoreply.app.data.AppPreferences
import com.autoreply.app.data.SessionLogPreferences
import com.autoreply.app.data.WhitelistPreferences
import com.autoreply.app.domain.Mode
import java.util.concurrent.Executors

/**
 * Listens for incoming SMS. When a mode is active and SMS reply message is set,
 * sends an automatic reply to the sender.
 *
 * Uses [goAsync] + [SmsSendHelper] so the outgoing SMS is reliably queued before the
 * receiver process can be torn down.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val firstMessage = messages.first()
        val sender = firstMessage.originatingAddress?.trim() ?: return
        if (sender.isEmpty()) return

        if (MessageFilter.isBusinessSmsSender(sender)) {
            Log.d(TAG, "Skipping auto-reply to business/short-code sender: $sender")
            return
        }

        if (WhitelistPreferences(context).isNumberWhitelisted(sender)) {
            Log.d(TAG, "Whitelisted SMS sender $sender — skipping auto-reply")
            return
        }

        val msgTimestamp = firstMessage.timestampMillis

        val prefs = AppPreferences(context)
        val appState = prefs.getAppStateSnapshot()
        if (appState.activeMode == Mode.OFF) return

        SessionLogPreferences(context).recordSms(sender, appState.activeMode)

        val replyText = AppPreferences.withAutomatedNote(
            appState.getSmsMessageForMode(appState.activeMode)
        )
        if (replyText.isBlank()) return

        if (DedupHelper.alreadyRepliedToSms(context, sender, msgTimestamp)) {
            Log.d(TAG, "Duplicate SMS broadcast detected for $sender @ $msgTimestamp — skipping")
            return
        }

        if (!RateLimitHelper.canReplyToSms(context, sender)) return

        val appCtx = context.applicationContext
        val pendingResult = goAsync()

        executor.execute {
            try {
                val sent = SmsSendHelper.sendText(appCtx, sender, replyText)
                if (sent) {
                    DedupHelper.recordSmsReply(appCtx, sender, msgTimestamp)
                    RateLimitHelper.recordSmsReply(appCtx, sender)
                    Log.d(TAG, "SMS reply sent to $sender")
                } else {
                    Log.e(TAG, "Failed to queue SMS reply to $sender")
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "SmsReceiver"
        private val executor = Executors.newSingleThreadExecutor()
    }
}
