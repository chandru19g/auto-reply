package com.autoreply.app.data

import com.autoreply.app.domain.Mode

enum class SessionChannel {
    CALL,
    SMS,
    WHATSAPP
}

data class SessionEvent(
    val id: String,
    val channel: SessionChannel,
    val display: String,
    val identifier: String,
    val mode: Mode,
    val timestampMs: Long
) {
    companion object {
        fun encode(e: SessionEvent): String =
            listOf(
                e.id,
                e.channel.name,
                e.display,
                e.identifier,
                e.mode.name,
                e.timestampMs.toString()
            ).joinToString("|||")

        fun decode(raw: String): SessionEvent? {
            val parts = raw.split("|||")
            if (parts.size != 6) return null
            return try {
                SessionEvent(
                    id = parts[0],
                    channel = SessionChannel.valueOf(parts[1]),
                    display = parts[2],
                    identifier = parts[3],
                    mode = Mode.valueOf(parts[4]),
                    timestampMs = parts[5].toLong()
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}

