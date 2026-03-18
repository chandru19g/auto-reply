package com.autoreply.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autoreply.app.data.WhitelistedContact
import com.autoreply.app.data.WhitelistPreferences
import com.autoreply.app.data.normalisePhone
import kotlinx.coroutines.flow.StateFlow

class WhitelistViewModel(private val prefs: WhitelistPreferences) : ViewModel() {

    val contacts: StateFlow<List<WhitelistedContact>> = prefs.contacts

    fun addContact(name: String, phoneNumber: String) {
        val id = normalisePhone(phoneNumber)
        if (id.isEmpty()) return
        prefs.add(WhitelistedContact(id = id, name = name, phoneNumber = phoneNumber))
    }

    fun removeContact(contactId: String) {
        prefs.remove(contactId)
    }

    class Factory(private val prefs: WhitelistPreferences) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            WhitelistViewModel(prefs) as T
    }
}
