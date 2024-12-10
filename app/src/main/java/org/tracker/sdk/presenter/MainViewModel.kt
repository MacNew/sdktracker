package org.tracker.sdk.presenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel: ViewModel()  {
    private val _logMessage = MutableStateFlow("Welcome to Analytics SDK Example!")
    val logMessage : StateFlow<String> get() = _logMessage

    private val _toastEvent = MutableSharedFlow<String>() // For triggering toast
    val toastEvent : SharedFlow<String> get() = _toastEvent

    fun updateLogMessage(message: String) {
        _logMessage.value = message
    }

    fun triggerToastMessage(data: String) {
        viewModelScope.launch {
            _toastEvent.emit(data)
        }
    }
}