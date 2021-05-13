package com.justice.schoolmanagement.presentation.ui.student

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import com.justice.schoolmanagement.utils.Resource
import com.justice.schoolmanagement.utils.exhaustive
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class StudentsViewModel @ViewModelInject constructor(private val repository: StudentsRepository, @Assisted private val savedStateHandle: SavedStateHandle) : ViewModel() {


    val getStudents = repository.getStudents()


    private val _studentsEvents = Channel<StudentsFragment.Event>()
    val studentsEvents = _studentsEvents.receiveAsFlow()


    fun setEvent(event: StudentsFragment.Event) {
        viewModelScope.launch {
            when (event) {
                is StudentsFragment.Event.StudentClicked -> {
                    _studentsEvents.send(StudentsFragment.Event.StudentClicked(event.parentSnapshot))
                }
                is StudentsFragment.Event.StudentEdit -> {
                    _studentsEvents.send(StudentsFragment.Event.StudentEdit(event.parentSnapshot))

                }
                is StudentsFragment.Event.StudentDelete -> {
                    _studentsEvents.send(StudentsFragment.Event.StudentDelete(event.parentSnapshot))

                }
                is StudentsFragment.Event.StudentDeleteConfirmed -> {
                    deleteStudent(event.parentSnapshot)
                }
                is StudentsFragment.Event.StudentSwiped -> {
                    _studentsEvents.send(StudentsFragment.Event.StudentSwiped(event.parentSnapshot))

                }
                is StudentsFragment.Event.StudentQuery -> {
                    startQuery(event.query)
                }
                StudentsFragment.Event.AddStudent -> {
                    _studentsEvents.send(StudentsFragment.Event.AddStudent)

                }
            }.exhaustive
        }


    }

    private val _deleteStudentStatus = Channel<Resource<DocumentSnapshot>>()
    val deleteStudentStatus = _deleteStudentStatus.receiveAsFlow()
    private suspend fun deleteStudent(parentSnapshot: DocumentSnapshot) {
        repository.deleteStudentPhoto(parentSnapshot).collect {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    deleteStudentMetaData(parentSnapshot)
                }
                Resource.Status.ERROR -> {
                    _deleteStudentStatus.send(it)
                }
            }
        }
    }

    private suspend fun deleteStudentMetaData(parentSnapshot: DocumentSnapshot) {
        repository.deleteStudentMetaData(parentSnapshot).collect {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    deleteStudentMarks(parentSnapshot)
                }
                Resource.Status.ERROR -> {
                    _deleteStudentStatus.send(it)
                }
            }

        }
    }

    private suspend fun deleteStudentMarks(snapshot: DocumentSnapshot) {

        repository.deleteStudentMarks(snapshot).collect {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    _deleteStudentStatus.send(it)
                }
                Resource.Status.ERROR -> {
                    _deleteStudentStatus.send(it)

                }
            }
        }


    }

    private val _currentListLiveData = MutableLiveData<List<DocumentSnapshot>>()
    val currentListLiveData get() = _currentListLiveData
    fun setCurrentListLiveData(documents: List<DocumentSnapshot>?) {
        currentListLiveData.value = documents
    }

    private val _studentQueryStatus = Channel<Resource<List<DocumentSnapshot>>>()
    val studentQueryStatus = _studentQueryStatus.receiveAsFlow()

    private suspend fun startQuery(query: String) {
        if (query.isBlank()) {
            _studentQueryStatus.send(Resource.empty())
        } else {
            val filteredList = mutableListOf<DocumentSnapshot>()
            currentListLiveData.value?.forEach {
                val student = it.toObject(StudentData::class.java)!!
                if (student.fullName.toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(it)
                }
            }

            _studentQueryStatus.send(Resource.success(filteredList))

        }
    }

}