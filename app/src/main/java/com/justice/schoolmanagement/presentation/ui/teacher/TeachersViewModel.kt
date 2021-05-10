package com.justice.schoolmanagement.presentation.ui.teacher

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.presentation.ui.teacher.TeachersFragment.Event
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TeachersViewModel @ViewModelInject constructor(private val repository: TeacherRepository) : ViewModel() {

    private val TAG = "TeachersViewModel"

    private val _teacherChannelEvents = Channel<Event>()
    val teacherChannelEvents get() = _teacherChannelEvents.receiveAsFlow()

    //teachers status
    val getTeachers = repository.getTeachers()

    private val _teachersListLiveData = MutableLiveData<List<DocumentSnapshot>>()
    val teachersListLiveData get() = _teachersListLiveData as LiveData<List<DocumentSnapshot>>
    fun setCurrentTeacherListLiveData(documentSnapshots: List<DocumentSnapshot>) {
        _teachersListLiveData.value = documentSnapshots
    }

    fun setEvent(event: Event) {
        viewModelScope.launch {
            Log.d(TAG, "setEvent:")
            when (event) {
                is Event.TeacherQuery -> {
                    _teacherQueryStatus.send(Resource.loading("started querying"))
                    startQuery(event.query)
                }

                is Event.TeacherClicked -> {
                    Log.d(TAG, "setEvent: parentclicked")
                    _teacherChannelEvents.send(Event.TeacherClicked(event.snapshot))
                }
                is Event.TeacherEdit -> {
                    _teacherChannelEvents.send(Event.TeacherEdit(event.snapshot))
                }
                is Event.TeacherDelete -> {
                    _teacherChannelEvents.send(Event.TeacherDelete(event.snapshot))
                }
                is Event.TeacherConfirmDelete -> {
                    deleteTeacher(event.snapshot)
                }
                is Event.TeacherSwiped -> {
                    _teacherChannelEvents.send(Event.TeacherSwiped(event.snapshot))
                }
            }
        }

    }

    private val _deleteTeacherStatus = Channel<Resource<DocumentSnapshot>>()
    val deleteTeacherStatus = _deleteTeacherStatus.receiveAsFlow()

    private suspend fun deleteTeacher(snapshot: DocumentSnapshot) {
        repository.deleteTeacher(snapshot).collect {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    deleteTeacherMetadata(snapshot)

                }
                Resource.Status.ERROR -> {
                    _deleteTeacherStatus.send(it)
                }
            }
        }


    }

    private suspend fun deleteTeacherMetadata(snapshot: DocumentSnapshot) {
        repository.deleteTeacherMetadata(snapshot).collect {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    _deleteTeacherStatus.send(it)
                }
                Resource.Status.ERROR -> {
                    _deleteTeacherStatus.send(it)
                }
            }
        }

    }



    private suspend fun startQuery(query: String) {
        Log.d(TAG, "startQuery: query:$query")
        val filterList = mutableListOf<DocumentSnapshot>()
        teachersListLiveData.value!!.forEach { documentSnapshot ->
            val teacherData = documentSnapshot.toObject(TeacherData::class.java)!!
            if (teacherData.fullName.toLowerCase().contains(query.toLowerCase())) {
                filterList.add(documentSnapshot)
            }
        }
        _teacherQueryStatus.send(Resource.success(filterList))
    }

    private val _teacherQueryStatus = Channel<Resource<List<DocumentSnapshot>>>()
    val teacherQueryStatus get() = _teacherQueryStatus.receiveAsFlow()

}