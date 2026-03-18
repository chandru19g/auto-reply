package com.autoreply.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.autoreply.app.data.AppState
import com.autoreply.app.data.ModeRepository
import com.autoreply.app.domain.Mode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val repository: ModeRepository) : ViewModel() {

    val appState: StateFlow<AppState> = repository.appState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppState(
            activeMode = Mode.OFF,
            sleepingCallMessage = "",
            sleepingSmsMessage = "",
            sleepingWhatsAppMessage = "",
            workingCallMessage = "",
            workingSmsMessage = "",
            workingWhatsAppMessage = "",
            drivingCallMessage = "",
            drivingSmsMessage = "",
            drivingWhatsAppMessage = ""
        ))

    // true when name has not been set yet → triggers the name dialog
    private val _showNameDialog = MutableStateFlow(repository.getUserName().isBlank())
    val showNameDialog: StateFlow<Boolean> = _showNameDialog.asStateFlow()

    fun saveUserName(name: String) {
        viewModelScope.launch {
            repository.setUserNameAndInitDefaults(name)
            _showNameDialog.value = false
        }
    }

    fun setMode(mode: Mode) {
        viewModelScope.launch {
            repository.setActiveMode(mode)
        }
    }

    fun setSleepingMessages(call: String, sms: String, whatsApp: String) {
        viewModelScope.launch {
            repository.setSleepingMessages(call, sms, whatsApp)
        }
    }

    fun setWorkingMessages(call: String, sms: String, whatsApp: String) {
        viewModelScope.launch {
            repository.setWorkingMessages(call, sms, whatsApp)
        }
    }

    fun setDrivingMessages(call: String, sms: String, whatsApp: String) {
        viewModelScope.launch {
            repository.setDrivingMessages(call, sms, whatsApp)
        }
    }

    class Factory(private val repository: ModeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repository) as T
        }
    }
}
