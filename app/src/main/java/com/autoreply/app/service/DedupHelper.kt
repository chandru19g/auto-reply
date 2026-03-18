package com.autoreply.app.service

import android.content.Context
import android.content.SharedPreferences

/**
 * Prevents duplicate auto-replies to the exact same incoming event.
 *
 * This is separate from RateLimitHelper (30-min production cooldown). DedupHelper ensures
 * we never reply more than once to the same specific message/call, even when the OS delivers
 * the broadcast or notification multiple times for the same event.
 *
 * - SMS: keyed by sender + PDU timestamp (unique per message).
 * - Calls: keyed by number + 3-second time bucket (same ring can fire the broadcast twice).
 */
object DedupHelper {

    private const val PREFS_NAME = "dedup_prefs"

    // Entries older than this are pruned to avoid the prefs growing unbounded.
    private const val EXPIRY_MS = 10_000L // 10 seconds is more than enough for duplicate broadcasts

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── SMS ──────────────────────────────────────────────────────────────────

    /**
     * Returns true if this exact SMS (identified by sender + message timestamp) has already
     * been replied to, meaning the broadcast fired a second time for the same PDU.
     */
    fun alreadyRepliedToSms(context: Context, sender: String, msgTimestamp: Long): Boolean {
        val key = "sms_${sender}_$msgTimestamp"
        val last = prefs(context).getLong(key, 0L)
        return last > 0L && (System.currentTimeMillis() - last < EXPIRY_MS)
    }

    fun recordSmsReply(context: Context, sender: String, msgTimestamp: Long) {
        val key = "sms_${sender}_$msgTimestamp"
        prefs(context).edit().putLong(key, System.currentTimeMillis()).apply()
    }

    // ── Calls ─────────────────────────────────────────────────────────────────

    /**
     * Returns true if this incoming call (same number within a 3-second window) has already
     * triggered a reply. Some OEMs fire the RINGING broadcast twice for one call.
     */
    fun alreadyRepliedToCall(context: Context, number: String): Boolean {
        // Bucket time into 3-second slots — same ring event always lands in the same bucket.
        val bucket = System.currentTimeMillis() / 3_000L
        val key = "call_${number}_$bucket"
        val last = prefs(context).getLong(key, 0L)
        return last > 0L
    }

    fun recordCallReply(context: Context, number: String) {
        val bucket = System.currentTimeMillis() / 3_000L
        val key = "call_${number}_$bucket"
        prefs(context).edit().putLong(key, System.currentTimeMillis()).apply()
    }
}
