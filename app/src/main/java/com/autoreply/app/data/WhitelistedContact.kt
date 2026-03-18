package com.autoreply.app.data

/**
 * Represents a contact that bypasses auto-reply for all modes.
 *
 * [id]          Unique key — the normalised phone number (digits only, last 10).
 * [name]        Display name shown in the whitelist UI.
 * [phoneNumber] Raw number as returned from the contacts picker.
 */
data class WhitelistedContact(
    val id: String,
    val name: String,
    val phoneNumber: String
) {
    companion object {
        /** Encode as a single storable string. */
        fun encode(contact: WhitelistedContact): String =
            "${contact.id}|||${contact.name}|||${contact.phoneNumber}"

        /** Decode a stored string back to a contact. Returns null if malformed. */
        fun decode(raw: String): WhitelistedContact? {
            val parts = raw.split("|||")
            if (parts.size != 3) return null
            return WhitelistedContact(id = parts[0], name = parts[1], phoneNumber = parts[2])
        }
    }
}

/**
 * Normalises a phone number so that "+91 98765 43210" and "9876543210" both
 * produce the same key for comparison purposes.
 */
fun normalisePhone(number: String): String {
    val digits = number.filter { it.isDigit() }
    return if (digits.length >= 10) digits.takeLast(10) else digits
}
