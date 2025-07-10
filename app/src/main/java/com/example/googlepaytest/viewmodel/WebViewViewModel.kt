package com.example.googlepaytest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WebViewViewModel : ViewModel() {
    private val _url = MutableStateFlow("https://www.google.com")
    val url: StateFlow<String> = _url

    fun loadUrl(newUrl: String) {
        viewModelScope.launch {
            _url.emit(newUrl)
        }
    }
}
