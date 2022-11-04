package com.example.dayplanner.ui.list_add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ListAddViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "ListAdd Fragment goes here"
    }
    val text: LiveData<String> = _text
}