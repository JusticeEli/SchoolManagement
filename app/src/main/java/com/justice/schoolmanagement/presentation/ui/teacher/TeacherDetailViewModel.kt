package com.justice.schoolmanagement.presentation.ui.teacher

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.presentation.ui.teacher.TeachersFragment.Companion.TEACHER_ARGS
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TeacherDetailViewModel @ViewModelInject constructor(private val repository: TeacherRepository, @Assisted private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val TAG = "TeacherDetailViewModel"
    private val _teacherDetailsEvent = Channel<TeacherDetailsFragment.Event>()
    val teacherDetailsEvent get() = _teacherDetailsEvent.receiveAsFlow()
    private val _deleteStatus = Channel<Resource<DocumentSnapshot>>()
    val deleteStatus get() = _deleteStatus.receiveAsFlow()


    private val _currentSnapshot = MutableLiveData<DocumentSnapshot>()
    val currentSnapshot get() = _currentSnapshot
    fun setCurrentSnapshot(snapshot: DocumentSnapshot) {
        _currentSnapshot.value = snapshot
    }


    val getTeacher = repository.getTeacher(savedStateHandle.get<TeacherData>(TEACHER_ARGS)?.id!!)
    fun setEvent(event: TeacherDetailsFragment.Event) {
        Log.d(TAG, "setEvent: ")
        viewModelScope.launch {

            when (event) {
                is TeacherDetailsFragment.Event.TeacherDelete -> {
                    _teacherDetailsEvent.send(TeacherDetailsFragment.Event.TeacherDelete(event.snapshot))
                }
                is TeacherDetailsFragment.Event.TeacherDeleteConfirmed -> {
                    deleteParentPhoto(event.snapshot)
                }
                is TeacherDetailsFragment.Event.TeacherEdit -> {
                    _teacherDetailsEvent.send(TeacherDetailsFragment.Event.TeacherEdit(event.snapshot))
                }
                is TeacherDetailsFragment.Event.TeacherCall -> {
                    _teacherDetailsEvent.send(TeacherDetailsFragment.Event.TeacherCall(event.number))
                }
                is TeacherDetailsFragment.Event.TeacherEmail -> {
                    _teacherDetailsEvent.send(TeacherDetailsFragment.Event.TeacherEmail(event.email))
                }
            }

        }


    }

    private suspend fun deleteParentPhoto(parentSnapshot: DocumentSnapshot) {
        Log.d(TAG, "deleteParentPhoto: ")
        repository.deleteTeacherPhoto(parentSnapshot).collect {
            Log.d(TAG, "setEvent: status of photo deletion:${it.status.name}")
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    deleteParentMetaData(parentSnapshot)
                }
                Resource.Status.ERROR -> {
                    Log.d(TAG, "setEvent: Error: ${it.exception?.message}")
                    _deleteStatus.send(Resource.error(it.exception))
                }
            }
        }

    }

    private suspend fun deleteParentMetaData(parentSnapshot: DocumentSnapshot) {
        Log.d(TAG, "deleteParentMetaData: ")
        repository.deleteTeacherMetadata(parentSnapshot).collect {
            Log.d(TAG, "deleteParentMetaData: status:${it.status.name}")
            when (it.status) {
                Resource.Status.LOADING -> {

                }
                Resource.Status.SUCCESS -> {
                    _deleteStatus.send(Resource.success(parentSnapshot))
                }
                Resource.Status.ERROR -> {
                    Log.d(TAG, "deleteParentMetaData: Error:${it.exception?.message}")
                    _deleteStatus.send(Resource.error(it.exception))
                }
            }
        }

    }

}