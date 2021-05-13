package com.justice.schoolmanagement.presentation.ui.attendance

import android.location.Location
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justice.schoolmanagement.presentation.ui.attendance.model.CurrentPosition
import com.justice.schoolmanagement.utils.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class SetLocationViewModel @ViewModelInject constructor(private val repository: AttendanceRepository) : ViewModel() {

    private val TAG = "SetLocationViewModel"

    private val _setLocationEvents = Channel<SetLocationFragment.Event>()
    val setLocationEvents = _setLocationEvents.receiveAsFlow()
    fun setEvent(event: SetLocationFragment.Event) {
        viewModelScope.launch {
            when (event) {
                is SetLocationFragment.Event.GoToSettingScreen -> {
                    _setLocationEvents.send(SetLocationFragment.Event.GoToSettingScreen)
                }
                is SetLocationFragment.Event.SetClicked -> {
                    setClicked(event.radius)
                }
                is SetLocationFragment.Event.StopLocationUpdates -> {
                    _setLocationEvents.send(SetLocationFragment.Event.StopLocationUpdates)
                }
                is SetLocationFragment.Event.UploadCurrentPosition -> {
                    uploadCurrentPosition(event.location, event.radius)
                }
            }
        }
    }

    private val _uploadCurrentPositionStatus = Channel<Resource<CurrentPosition>>()
    val uploadCurrentPositionStatus = _uploadCurrentPositionStatus.receiveAsFlow()
    private suspend fun uploadCurrentPosition(location: Location, radius: Int) {
        val currentPosition = CurrentPosition(location.latitude, location.longitude, radius)

        repository.uploadCurrentPosition(currentPosition).collect {
            _uploadCurrentPositionStatus.send(it)
        }


    }

    private suspend fun setClicked(originalRadius: String) {
        if (fieldIsEmpty(originalRadius)) {
            _setRadiusStatus.send(Resource.error(Exception("Please Fill The Radius!!")))
            return
        }
        val trimmedRadius = originalRadius.trim()
        try {
            trimmedRadius.toInt()

        } catch (e: java.lang.Exception) {
            _setRadiusStatus.send(Resource.error(java.lang.Exception("Please Input A digit")))
        }


        _setLocationEvents.send(SetLocationFragment.Event.StartLocationUpdates(trimmedRadius.toInt()))

    }


    private fun fieldIsEmpty(radius: String): Boolean {
        return radius.isBlank()
    }

    private val _setRadiusStatus = Channel<Resource<String>>()
    val setRadiusStatus = _setRadiusStatus.receiveAsFlow()
}