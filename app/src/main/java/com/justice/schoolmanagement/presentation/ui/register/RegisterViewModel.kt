package com.justice.schoolmanagement.presentation.ui.register

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*

class RegisterViewModel @ViewModelInject constructor(private val repository: RegisterRepository) : ViewModel() {

    private val TAG = "RegisterViewModel"
   
    val getCurrentDate = repository.getCurrentDate()
    private val _registerEvents = Channel<RegisterFragment.Event>()
    val registerEvents = _registerEvents.receiveAsFlow()
    fun setEvent(event: RegisterFragment.Event) {
        viewModelScope.launch {
            when (event) {
                is RegisterFragment.Event.TabSelected -> {

                }
                is RegisterFragment.Event.DateClicked -> {
                    _registerEvents.send(RegisterFragment.Event.DateClicked)

                }
                is RegisterFragment.Event.SpecificDateChoosen -> {
                    checkIfWeHaveChoosenCorrectDate(event.date)
                }
                is RegisterFragment.Event.ClassSelected -> {

                }
                is RegisterFragment.Event.FetchData -> {

                    startFetchingData(event.currentInfo)
                }
                is RegisterFragment.Event.CheckBoxClicked -> {

                    onCheckBoxClicked(event.snapshot, event.present)
                }
            }
        }
    }

    private suspend fun onCheckBoxClicked(snapshot: DocumentSnapshot, present: Boolean) {

        repository.onCheckBoxClicked(snapshot, present).collect {
            Log.d(TAG, "onCheckBoxClicked: ${it.status.name}")
            when (it.status) {
                Resource.Status.LOADING -> {

                }
                Resource.Status.SUCCESS -> {

                }
                Resource.Status.ERROR -> {

                }
            }
        }
    }

    private suspend fun startFetchingData(currentInfo: CurrentInfo) {
        repository.startFetchingData(currentInfo).collect {
            Log.d(TAG, "startFetchingData: ${it.status.name}")
            when (it.status) {
                Resource.Status.LOADING -> {

                }
                Resource.Status.SUCCESS -> {
                    documentExists(currentInfo, it.data!!)
                }
                Resource.Status.EMPTY -> {
                    //document does not exist

                }
                Resource.Status.ERROR -> {

                }
            }
        }

    }

    private suspend fun documentExists(currentInfo: CurrentInfo, snapshot: DocumentSnapshot) {
        repository.documentExist(currentInfo, snapshot).collect {
            when (it.status) {
                Resource.Status.LOADING -> {

                }
                Resource.Status.SUCCESS -> {
                    _fetchDataStatus.send(it)

                }
                Resource.Status.ERROR -> {
                    _fetchDataStatus.send(it)
                }
            }

        }
    }

    private suspend fun checkIfWeHaveChoosenCorrectDate(choosenDate: Date) {
        //check if we have choosen a future date and reject it if its future date
///checks if we are on same day

        repository.getCurrentDate().collect {
            val currentDateServer = it.data!!
            val cal1 = Calendar.getInstance()
            val cal2 = Calendar.getInstance()
            cal1.time = choosenDate
            cal2.time = currentDateServer

            val sameDay = cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR] &&
                    cal1[Calendar.YEAR] == cal2[Calendar.YEAR]

            if (choosenDate.after(currentDateServer)) {
                _registerEvents.send(RegisterFragment.Event.FutureDateChoosen)
            } else {
                _registerEvents.send(RegisterFragment.Event.SpecificDateChoosen(choosenDate))
            }
        }


    }


    private val _fetchDataStatus = Channel<Resource<List<DocumentSnapshot>>>()
    val fetchDataStatus = _fetchDataStatus.receiveAsFlow()


}