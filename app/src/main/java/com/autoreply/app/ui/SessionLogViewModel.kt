package com.autoreply.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autoreply.app.data.SessionEvent
import com.autoreply.app.data.SessionLogPreferences
import kotlinx.coroutines.flow.StateFlow

class SessionLogViewModel(private val prefs: SessionLogPreferences) : ViewModel() {

    val events: StateFlow<List<SessionEvent>> = prefs.events

    fun clear() = prefs.clear()

    fun hasAny(): Boolean = prefs.hasAny()

    class Factory(private val prefs: SessionLogPreferences) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SessionLogViewModel(prefs) as T
    }
}

