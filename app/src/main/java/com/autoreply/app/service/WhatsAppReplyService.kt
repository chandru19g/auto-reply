package com.autoreply.app.service

import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.app.Notification
import com.autoreply.app.data.AppPreferences
import com.autoreply.app.data.SessionLogPreferences
import com.autoreply.app.data.WhitelistPreferences
import com.autoreply.app.domain.Mode

/**
 * Listens for WhatsApp (and WhatsApp Business) notifications.
 * When a mode is active and a WhatsApp reply message is set, sends the reply
 * via the notification's RemoteInput reply action.
 *
 * Deduplication: each notification key is tracked in memory. Once replied, we
 * skip any further updates to that same notification (WhatsApp re-posts the same
 * notification for badge updates, group counts, etc.). When the notification is
 * dismissed, the key is removed so the next new message triggers a fresh reply.
 */
class WhatsAppReplyService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        if (pkg != WHATSAPP_PACKAGE && pkg != WHATSAPP_BUSINESS_PACKAGE) return

        val appState = AppPreferences(this).getAppStateSnapshot()
        if (appState.activeMode == Mode.OFF) return

        val replyText = AppPreferences.withAutomatedNote(
            appState.getWhatsAppMessageForMode(appState.activeMode)
        )
        if (replyText.isBlank()) return

        val notification = sbn.notification ?: return

        // Whitelist: allow whitelisted contacts through (matched by sender name)
        val senderName = notification.extras
            ?.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""

        // Session log: record who contacted you during an active mode (1:1 chats only; groups/business filtered below)
        if (senderName.isNotBlank()) {
            SessionLogPreferences(this).recordWhatsApp(senderName, appState.activeMode)
        }

        if (senderName.isNotBlank() &&
            WhitelistPreferences(this).isSenderNameWhitelisted(senderName)
        ) {
            Log.d(TAG, "Whitelisted WhatsApp sender '$senderName' — skipping auto-reply")
            return
        }

        // Skip group chats
        if (MessageFilter.isWhatsAppGroupMessage(notification)) return

        // Skip messages from WhatsApp Business accounts
        if (MessageFilter.isWhatsAppBusinessMessage(notification)) return

        val notifKey = sbn.key

        // Skip if we already replied to this exact notification posting
        if (notifKey in repliedKeys) {
            Log.d(TAG, "Already replied to notification key: $notifKey — skipping")
            return
        }

        // Production rate limit (COOLDOWN_MS = 0 during testing)
        if (!RateLimitHelper.canReplyToWhatsApp(this, notifKey)) return

        val actions = notification.actions ?: run {
            Log.d(TAG, "No actions on notification")
            return
        }

        for (action in actions) {
            val remoteInputs = action.remoteInputs
            if (remoteInputs.isNullOrEmpty()) continue
            val pendingIntent = action.actionIntent ?: continue

            try {
                val replyIntent = Intent()
                val bundle = Bundle().apply {
                    for (ri in remoteInputs) {
                        putCharSequence(ri.resultKey, replyText)
                    }
                }
                RemoteInput.addResultsToIntent(remoteInputs, replyIntent, bundle)
                pendingIntent.send(applicationContext, 0, replyIntent)

                repliedKeys.add(notifKey)
                RateLimitHelper.recordWhatsAppReply(this, notifKey)
                Log.d(TAG, "WhatsApp reply sent for key: $notifKey")
                return
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send WhatsApp reply: ${e.message}")
            }
        }
        Log.w(TAG, "No usable reply action found in notification from $pkg")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Notification was dismissed — remove from replied set so the next new
        // message from this conversation triggers a fresh reply
        repliedKeys.remove(sbn.key)
        Log.d(TAG, "Notification removed, cleared key: ${sbn.key}")
    }

    companion object {
        private const val TAG = "WhatsAppReplyService"
        private const val WHATSAPP_PACKAGE = "com.whatsapp"
        private const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"

        // In-memory set of notification keys we've already replied to.
        // Scoped to the service's lifetime (survives notification updates for the same chat,
        // cleared on service restart which is fine — rare enough not to cause duplicates).
        private val repliedKeys = mutableSetOf<String>()
    }
}
