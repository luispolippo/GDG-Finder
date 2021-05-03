package com.example.gdgfinder.search

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gdgfinder.network.GdgApi
import com.example.gdgfinder.network.GdgChapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException


class GdgListViewModel: ViewModel() {

    private val repository = GdgChapterRepository(GdgApi.retrofitService)

    private var filter = FilterHolder()

    private var currentJob: Job? = null

    private val _gdgList = MutableLiveData<List<GdgChapter>>()
    val gdgList: LiveData<List<GdgChapter>>
        get() = _gdgList

    private val _regionList = MutableLiveData<List<String>>()
    val regionList: LiveData<List<String>>
        get() = _regionList

    private val _showNeedLocation = MutableLiveData<Boolean>()
    val showNeedLocation:LiveData<Boolean>
        get() = _showNeedLocation

    init {
        onQueryChanged()

        viewModelScope.launch {
            delay(5_000)
            _showNeedLocation.value = !repository.isFullyInitialized
        }
    }

    private fun onQueryChanged(){
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            try {
                _gdgList.value = repository.getChapterForFilter(filter.currentValue)
                repository.getFilters().let {
                    if (it != _regionList.value){
                        _regionList.value = it
                    }
                }
            }catch (e: IOException){
                _gdgList.value = listOf()
            }
        }
    }

    fun onLocationUpdate(location: Location){
        viewModelScope.launch {
            repository.onLocationChanged(location)
            onQueryChanged()
        }
    }

    fun onFilterChanged(filter: String, isChecked: Boolean){
        if (this.filter.update(filter, isChecked)){
            onQueryChanged()
        }
    }

    private class FilterHolder{
        var currentValue: String? = null
            private set

        fun update(changedFilter: String, isChecked: Boolean): Boolean{
            if (isChecked){
                currentValue = changedFilter
                return true
            } else if (currentValue == changedFilter){
                currentValue = null
                return true
            }
            return false
        }
    }
}