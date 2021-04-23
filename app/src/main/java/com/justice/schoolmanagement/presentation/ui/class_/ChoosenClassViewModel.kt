package com.justice.schoolmanagement.presentation.ui.class_

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.presentation.ui.results.ResultsFragment
import com.justice.schoolmanagement.presentation.ui.results.ResultsRepository
import com.justice.schoolmanagement.presentation.ui.student.StudentsFragment
import com.justice.schoolmanagement.presentation.ui.student.StudentsRepository
import com.justice.schoolmanagement.presentation.ui.student.models.CLASS_GRADE
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ChoosenClassViewModel @ViewModelInject constructor(private val studentRepo: StudentsRepository,
                                                         private val repository: ResultsRepository,
                                                         @Assisted private val savedStateHandle: SavedStateHandle) : ViewModel() {

    //student section
    val getStudents = studentRepo.getStudentsByClass(savedStateHandle.get<String>(CLASS_GRADE)!!)
    private val _studentsEvents = Channel<StudentsFragment.Event>()
    val studentsEvents = _studentsEvents.receiveAsFlow()

    fun setEventStudent(event: ChoosenClassFragment.Event) {
        viewModelScope.launch {
            when (event) {
                is ChoosenClassFragment.Event.StudentClicked -> {
                    _studentsEvents.send(StudentsFragment.Event.StudentClicked(event.parentSnapshot))
                }
                is ChoosenClassFragment.Event.StudentEdit -> {
                    _studentsEvents.send(StudentsFragment.Event.StudentEdit(event.parentSnapshot))

                }
                is ChoosenClassFragment.Event.StudentDelete -> {
                    _studentsEvents.send(StudentsFragment.Event.StudentDelete(event.parentSnapshot))

                }
                is ChoosenClassFragment.Event.StudentDeleteConfirmed -> {
                    deleteStudent(event.parentSnapshot)
                }


            }
        }


    }

    private val _deleteStudentStatus = Channel<Resource<DocumentSnapshot>>()
    val deleteStudentStatus = _deleteStudentStatus.receiveAsFlow()
    private suspend fun deleteStudent(parentSnapshot: DocumentSnapshot) {
        studentRepo.deleteStudentPhoto(parentSnapshot).collect {
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
        studentRepo.deleteStudentMetaData(parentSnapshot).collect {
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

        studentRepo.deleteStudentMarks(snapshot).collect {
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

    private val _currentStudentsList = MutableLiveData<List<DocumentSnapshot>>()
    val currentStudentsList get() = _currentStudentsList
    fun setCurrentStudentsList(documents: List<DocumentSnapshot>?) {
        currentStudentsList.value = documents
    }


    //results Section
    val getAllMarks = repository.getAllMarksByClass(savedStateHandle.get<String>(CLASS_GRADE)!!)

    private val _resultEvents = Channel<ResultsFragment.Event>()
    val resultEvents = _resultEvents.receiveAsFlow()
    fun setEventResults(event: ChoosenClassFragment.Event) {
        viewModelScope.launch {
            when (event) {
                is ChoosenClassFragment.Event.EditClicked -> {
                    _resultEvents.send(ResultsFragment.Event.EditClicked(event.snapshot))
                }
            }
        }

    }

}