package com.unpas.showroom.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Selamat Datang Di ShowRoom"
    }
    val text: LiveData<String> = _text
}