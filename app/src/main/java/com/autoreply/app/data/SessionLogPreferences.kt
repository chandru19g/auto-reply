package com.autoreply.app.data

import android.content.Context
import android.content.SharedPreferences
import com.autoreply.app.domain.Mode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionLogPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _events = MutableStateFlow(load())
    val events: StateFlow<List<SessionEvent>> = _events.asStateFlow()

    private fun load(): List<SessionEvent> {
        val raw = prefs.getStringSet(KEY_EVENTS, emptySet()) ?: emptySet()
        return raw.mapNotNull { SessionEvent.decode(it) }
            .sortedByDescending { it.timestampMs }
    }

    private fun save(list: List<SessionEvent>) {
        val trimmed = list.take(MAX_EVENTS)
        prefs.edit().putStringSet(KEY_EVENTS, trimmed.map { SessionEvent.encode(it) }.toSet()).apply()
        _events.value = trimmed
    }

    fun clear() {
        prefs.edit().remove(KEY_EVENTS).apply()
        _events.value = emptyList()
    }

    fun hasAny(): Boolean = load().isNotEmpty()

    fun recordCall(number: String, mode: Mode) {
        val identifier = normalisePhone(number)
        if (identifier.isBlank()) return
        record(
            channel = SessionChannel.CALL,
            display = number.trim(),
            identifier = identifier,
            mode = mode,
            dedupWindowMs = 10_000L
        )
    }

    fun recordSms(sender: String, mode: Mode) {
        val identifier = normalisePhone(sender)
        if (identifier.isBlank()) return
        record(
            channel = SessionChannel.SMS,
            display = sender.trim(),
            identifier = identifier,
            mode = mode,
            dedupWindowMs = 30_000L
        )
    }

    fun recordWhatsApp(senderName: String, mode: Mode) {
        val display = senderName.trim()
        if (display.isBlank()) return
        record(
            channel = SessionChannel.WHATSAPP,
            display = display,
            identifier = display.lowercase(),
            mode = mode,
            dedupWindowMs = 30_000L
        )
    }

    private fun record(
        channel: SessionChannel,
        display: String,
        identifier: String,
        mode: Mode,
        dedupWindowMs: Long
    ) {
        val now = System.currentTimeMillis()
        val list = load().toMutableList()

        // Dedup within a short window for same contact + channel (prevents spam from duplicate broadcasts)
        val already = list.any {
            it.channel == channel &&
                it.identifier == identifier &&
                (now - it.timestampMs) <= dedupWindowMs
        }
        if (already) return

        val id = "${channel.name}:${identifier}:${now / 1000}"
        list.add(
            SessionEvent(
                id = id,
                channel = channel,
                display = display,
                identifier = identifier,
                mode = mode,
                timestampMs = now
            )
        )
        save(list.sortedByDescending { it.timestampMs })
    }

    companion object {
        private const val PREFS_NAME = "session_log_preferences"
        private const val KEY_EVENTS = "events"
        private const val MAX_EVENTS = 200
    }
}

