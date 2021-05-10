package com.justice.schoolmanagement.presentation.ui.parent

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.presentation.ui.parent.ParentsFragment.Companion.PARENT_ARGS
import com.justice.schoolmanagement.presentation.ui.parent.model.ParentData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ParentDetailsViewModel @ViewModelInject constructor(private val repository: ParentRepository ,@Assisted private val savedStateHandle:SavedStateHandle) : ViewModel() {

    private val TAG = "ParentDetailsViewModel"
    private val _parentDetailsEvent = Channel<ParentDetailsFragment.Event>()
    val parentDetailsEvent get() = _parentDetailsEvent.receiveAsFlow()
    private val _deleteStatus = Channel<Resource<DocumentSnapshot>>()
    val deleteStatus get() = _deleteStatus.receiveAsFlow()


    private val _currentSnapshot = MutableLiveData<DocumentSnapshot>()
    val currentSnapshot get() = _currentSnapshot
    fun setCurrentSnapshot(snapshot: DocumentSnapshot) {
        _currentSnapshot.value = snapshot
    }


    val getParent = repository.getParent(savedStateHandle.get<ParentData>(PARENT_ARGS)?.id!!)
    fun setEvent(event: ParentDetailsFragment.Event) {
        Log.d(TAG, "setEvent: ")
        viewModelScope.launch {

            when (event) {
                is ParentDetailsFragment.Event.ParentDelete -> {
                    _parentDetailsEvent.send(ParentDetailsFragment.Event.ParentDelete(event.parentSnapshot))
                }
                is ParentDetailsFragment.Event.ParentDeleteConfirmed -> {
                    deleteParentPhoto(event.parentSnapshot)
                }
                is ParentDetailsFragment.Event.ParentEdit -> {
                    _parentDetailsEvent.send(ParentDetailsFragment.Event.ParentEdit(event.parentSnapshot))
                }
                is ParentDetailsFragment.Event.ParentCall -> {
                    _parentDetailsEvent.send(ParentDetailsFragment.Event.ParentCall(event.number))
                }
                is ParentDetailsFragment.Event.ParentEmail -> {
                    _parentDetailsEvent.send(ParentDetailsFragment.Event.ParentEmail(event.email))
                }
            }

        }


    }

    private suspend fun deleteParentPhoto(parentSnapshot: DocumentSnapshot) {
        Log.d(TAG, "deleteParentPhoto: ")
        repository.deleteParentPhoto(parentSnapshot).collect {
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
        repository.deleteParentMetadata(parentSnapshot).collect {
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