package com.learning.image.detection.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.learning.image.detection.data.DetectedObject

// keeping data in view model so that it can survive configuration change
class DetectedObjectListViewModel : ViewModel() {

    private val _detectedObjectList = MutableLiveData<List<DetectedObject>>()
    val detectedObjectList: LiveData<List<DetectedObject>> = _detectedObjectList

    fun updateData(detectedObjects: List<DetectedObject>) {
        _detectedObjectList.postValue(detectedObjects)
    }
}
