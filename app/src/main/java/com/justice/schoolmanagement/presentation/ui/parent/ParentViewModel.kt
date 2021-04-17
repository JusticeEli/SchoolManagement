package com.justice.schoolmanagement.presentation.ui.parent

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.justice.schoolmanagement.presentation.ui.parent.model.ParentData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ParentViewModel @ViewModelInject constructor(private val repository: ParentRepository, @Assisted private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val TAG = "ParentViewModel"

    private val _parentChannelEvents = Channel<ParentsFragment.Event>()
    val parentChannelEvents get() = _parentChannelEvents.receiveAsFlow()

    //parents status
    val parentsFetchStatus = flow<Resource<QuerySnapshot>> {
        Log.d(TAG, "started fetching programs: ")
        emit(Resource.loading("started the process"))

        try {
            repository.getParents().collect {
                parentsList.value = it
                emit(it)
            }
        } catch (e: Exception) {
            emit(Resource.error(e))
            Log.e("ERROR:", e.message)
        }
    }

    fun setEvent(event: ParentsFragment.Event) {
        viewModelScope.launch {
            Log.d(TAG, "setEvent:")
            when (event) {
                is ParentsFragment.Event.ParentQuery -> {
                    _parentQueryStatus.send(Resource.loading("started querying"))
                    startQuery(event.query)
                }
                is ParentsFragment.Event.AddParent -> {
                    _parentChannelEvents.send(ParentsFragment.Event.AddParent)
                }
                is ParentsFragment.Event.ParentClicked -> {
                    Log.d(TAG, "setEvent: parentclicked")
                    _parentChannelEvents.send(ParentsFragment.Event.ParentClicked(event.parentSnapshot))
                }
                is ParentsFragment.Event.ParentEdit -> {
                    _parentChannelEvents.send(ParentsFragment.Event.ParentEdit(event.parentSnapshot))
                }
                is ParentsFragment.Event.ParentDelete -> {
                    _parentChannelEvents.send(ParentsFragment.Event.ParentDelete(event.parentSnapshot))
                }
                is ParentsFragment.Event.ParentSwiped -> {
                    _parentChannelEvents.send(ParentsFragment.Event.ParentSwiped(event.parentSnapshot))
                }
            }
        }

    }


    val parentsList = MutableStateFlow(Resource.empty<QuerySnapshot>())
    private suspend fun startQuery(query: String) {
        Log.d(TAG, "startQuery: query:$query")
        val filterList = mutableListOf<DocumentSnapshot>()
        Log.d(TAG, "startQuery: size:${parentsList.value.data?.size()}")
        parentsList.value.data?.documents?.forEach { documentSnapshot ->
            val parentData = documentSnapshot.toObject(ParentData::class.java)!!
            if (parentData.fullName.toLowerCase().contains(query.toLowerCase())) {
                filterList.add(documentSnapshot)
            }
        }
        _parentQueryStatus.send(Resource.success(filterList))
    }

    private val _parentQueryStatus = Channel<Resource<List<DocumentSnapshot>>>()
    val parentQueryStatus get() = _parentQueryStatus.receiveAsFlow()


}