package com.example.gdgfinder.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddGdgViewModel: ViewModel() {

    private val _showSnackBarEvent = MutableLiveData<Boolean?>()
    val showSnackBarEvent: LiveData<Boolean?>
        get() = _showSnackBarEvent

    fun doneShowingSnackBar(){
        _showSnackBarEvent.value = null
    }

    fun onSubmitApplication(){
        _showSnackBarEvent.value = true
    }


}