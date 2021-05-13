package com.justice.schoolmanagement.presentation.ui.attendance

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.utils.Resource
import com.justice.schoolmanagement.utils.cleanString
import com.justice.schoolmanagement.utils.formatDate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*

class AttendanceViewModel @ViewModelInject constructor(private val repository: AttendanceRepository) : ViewModel() {

    private val TAG = "AttendanceViewModel"


    val getCurrentDate=repository.getCurrentDate2()

    private val _attendanceEvents = Channel<AttendanceFragment.Event>()
    val attendanceEvents = _attendanceEvents.receiveAsFlow()
    fun setEvent(event: AttendanceFragment.Event) {
        viewModelScope.launch {
            when (event) {
                is AttendanceFragment.Event.DateClicked -> {
                    _attendanceEvents.send(AttendanceFragment.Event.DateClicked)

                }
                is AttendanceFragment.Event.SetLocationClicked -> {
                    _attendanceEvents.send(AttendanceFragment.Event.SetLocationClicked)
                }
                is AttendanceFragment.Event.DateChoosen -> {
                    dateHasBeenChoosen(event.choosenDate)
                }
                is AttendanceFragment.Event.FetchAttendance -> {
                    startFetchingAttendance(event.choosenDate)
                }
            }
        }
    }

    private val _fetchAttendanceStatus = Channel<Resource<List<DocumentSnapshot>>>()
    val fetchAttendanceStatus = _fetchAttendanceStatus.receiveAsFlow()

    private suspend fun startFetchingAttendance(choosenDate: String) {
        val cleanedDate=choosenDate.cleanString
        repository.startFetchingAttendance(cleanedDate).collect {
            _fetchAttendanceStatus.send(it)
        }
    }

    private val _dateChoosenStatus = Channel<Resource<String>>()
    val dateChoosenStatus = _dateChoosenStatus.receiveAsFlow()

    private suspend fun dateHasBeenChoosen(choosenDate: Date) {
        _dateChoosenStatus.send(Resource.loading(""))
        Log.d(TAG, "dateHasBeenChoosen: choosenDate:${choosenDate.formatDate}")
        ///checks if we are on same day
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = choosenDate
        cal2.time = repository.getCurrentDate()

        val sameDay = cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR] &&
                cal1[Calendar.YEAR] == cal2[Calendar.YEAR]

        if (choosenDate.after(cal2.time)) {

            _dateChoosenStatus.send(Resource.error(Exception("Please Don't Choose  Future date only past  can be choosen")))

        } else {
            _dateChoosenStatus.send(Resource.success(choosenDate.formatDate))

        }


    }
}