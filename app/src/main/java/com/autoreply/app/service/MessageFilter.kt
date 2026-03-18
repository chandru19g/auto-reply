package com.autoreply.app.service

import android.app.Notification
import android.util.Log
import androidx.core.app.NotificationCompat

/**
 * Filters out messages that should NOT receive auto-replies:
 *
 * WhatsApp:
 *  - Group chats: detected via MessagingStyle.isGroupConversation or EXTRA_CONVERSATION_TITLE
 *  - Business accounts: detected via notification channel ID and title suffix "(Business)"
 *
 * SMS:
 *  - Business/company senders: alphanumeric sender IDs, short codes (3–6 digits),
 *    or telecom DLT prefixes (e.g. "AD-", "VM-", "IM-"). These are never real people.
 */
object MessageFilter {

    private const val TAG = "MessageFilter"

    // ── WhatsApp ─────────────────────────────────────────────────────────────

    /**
     * Returns true if the notification is from a WhatsApp group chat.
     *
     * Detection strategy:
     *  1. MessagingStyle.isGroupConversation() — most reliable (API 24+).
     *  2. EXTRA_CONVERSATION_TITLE present — WhatsApp sets this only for groups.
     */
    fun isWhatsAppGroupMessage(notification: Notification): Boolean {
        // 1. NotificationCompat.MessagingStyle (AndroidX — works on all supported API levels)
        val style = NotificationCompat.MessagingStyle
            .extractMessagingStyleFromNotification(notification)
        if (style != null) {
            if (style.isGroupConversation) {
                Log.d(TAG, "Skipping WhatsApp group message (MessagingStyle)")
                return true
            }
            // Non-null style with no group flag → individual chat → don't skip
            return false
        }

        // 2. Fallback: EXTRA_CONVERSATION_TITLE is set only for group notifications
        val conversationTitle = notification.extras
            ?.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)
        if (conversationTitle != null) {
            Log.d(TAG, "Skipping WhatsApp group message (EXTRA_CONVERSATION_TITLE)")
            return true
        }

        return false
    }

    /**
     * Returns true if the notification is from a WhatsApp Business account.
     *
     * Detection strategy:
     *  1. Notification channel ID contains "business" — WhatsApp routes business chats
     *     through a separate channel.
     *  2. Notification title ends with "(Business)" — WhatsApp appends this suffix for
     *     verified business account chats.
     *  3. Notification subText contains "Business".
     */
    fun isWhatsAppBusinessMessage(notification: Notification): Boolean {
        // 1. Channel ID
        val channelId = notification.channelId?.lowercase() ?: ""
        if (channelId.contains("business")) {
            Log.d(TAG, "Skipping WhatsApp business message (channel: $channelId)")
            return true
        }

        // 2. Title suffix "(Business)"
        val title = notification.extras
            ?.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        if (title.endsWith("(Business)", ignoreCase = true)) {
            Log.d(TAG, "Skipping WhatsApp business message (title: $title)")
            return true
        }

        // 3. Sub-text
        val subText = notification.extras
            ?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""
        if (subText.contains("Business", ignoreCase = true)) {
            Log.d(TAG, "Skipping WhatsApp business message (subText: $subText)")
            return true
        }

        return false
    }

    // ── SMS ──────────────────────────────────────────────────────────────────

    /**
     * Returns true if the SMS sender is a business, company, or automated sender —
     * not a real person's phone number. These should never receive auto-replies.
     *
     * Detection strategy:
     *  1. Sender contains letters → alphanumeric sender ID (e.g. "AMAZON", "HDFC-BANK").
     *  2. Sender is a short code → 3–6 digits only (e.g. "12345", "567").
     *  3. Sender starts with a known telecom DLT promotional prefix
     *     (e.g. "AD-", "VM-", "IM-", "JD-", "BZ-") used in India and some other countries.
     *  4. Sender does NOT match an E.164-style phone number → treat as automated.
     *
     *  A real personal number: optional leading +, then 7–15 digits, nothing else.
     */
    fun isBusinessSmsSender(sender: String): Boolean {
        val s = sender.trim()

        // 1. Contains any letter → alphanumeric sender ID
        if (s.any { it.isLetter() }) {
            Log.d(TAG, "Skipping business SMS (alphanumeric sender): $s")
            return true
        }

        // Strip common leading + or spaces for digit-only checks
        val digits = s.filter { it.isDigit() }

        // 2. Short code: 3–6 digit numbers
        if (digits.length in 3..6) {
            Log.d(TAG, "Skipping business SMS (short code): $s")
            return true
        }

        // 3. E.164 validation: optional +, then 7–15 digits, nothing else
        val e164Regex = Regex("""^\+?[0-9]{7,15}$""")
        if (!e164Regex.matches(s)) {
            Log.d(TAG, "Skipping business SMS (non-E.164 sender): $s")
            return true
        }

        return false
    }
}
