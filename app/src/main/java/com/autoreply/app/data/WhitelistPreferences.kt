package com.autoreply.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WhitelistPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _contacts = MutableStateFlow(load())
    val contacts: StateFlow<List<WhitelistedContact>> = _contacts.asStateFlow()

    private fun load(): List<WhitelistedContact> {
        val raw = prefs.getStringSet(KEY_CONTACTS, emptySet()) ?: emptySet()
        return raw.mapNotNull { WhitelistedContact.decode(it) }
            .sortedBy { it.name.lowercase() }
    }

    private fun save(contacts: List<WhitelistedContact>) {
        val encoded = contacts.map { WhitelistedContact.encode(it) }.toSet()
        prefs.edit().putStringSet(KEY_CONTACTS, encoded).apply()
        _contacts.value = contacts.sortedBy { it.name.lowercase() }
    }

    fun add(contact: WhitelistedContact) {
        val current = load().toMutableList()
        if (current.none { it.id == contact.id }) {
            current.add(contact)
            save(current)
        }
    }

    fun remove(contactId: String) {
        val updated = load().filter { it.id != contactId }
        save(updated)
    }

    /** Synchronous check used from receivers/services. */
    fun isNumberWhitelisted(incomingNumber: String): Boolean {
        val key = normalisePhone(incomingNumber)
        if (key.isEmpty()) return false
        return load().any { it.id == key }
    }

    /** Synchronous check: WhatsApp sender name matches any whitelisted contact name. */
    fun isSenderNameWhitelisted(senderName: String): Boolean {
        val lower = senderName.trim().lowercase()
        return load().any { it.name.trim().lowercase() == lower }
    }

    companion object {
        private const val PREFS_NAME = "whitelist_preferences"
        private const val KEY_CONTACTS = "contacts"
    }
}
