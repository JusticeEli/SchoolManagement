package com.justice.schoolmanagement.presentation.ui.student

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.presentation.ui.student.StudentsFragment.Companion.STUDENT_ARGS
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class StudentDetailsViewModel @ViewModelInject constructor(private val repository: StudentsRepository, @Assisted private val savedStateHandle: SavedStateHandle) : ViewModel() {

    val currentStudent = repository.getCurrentStudent(savedStateHandle.get<StudentData>(STUDENT_ARGS)!!.id!!)

    //  val currentStudent = MutableStateFlow<Resource<String>>(Resource.success(""))
    private val _studentDetailEvents = Channel<StudentDetailsFragment.Event>()
    val studentDetailEvents = _studentDetailEvents.receiveAsFlow()

    fun setEvent(event: StudentDetailsFragment.Event) {
        viewModelScope.launch {
            when (event) {
                is StudentDetailsFragment.Event.StudentDelete -> {
                    _studentDetailEvents.send(StudentDetailsFragment.Event.StudentDelete(event.snapshot))
                }
                is StudentDetailsFragment.Event.StudentEdit -> {
                    _studentDetailEvents.send(StudentDetailsFragment.Event.StudentEdit(event.snapshot))
                }
                is StudentDetailsFragment.Event.FeesClicked -> {
                    _studentDetailEvents.send(StudentDetailsFragment.Event.FeesClicked(event.snapshot))
                }
                is StudentDetailsFragment.Event.StudentDeleteConfirmed -> {
                    deleteStudent(event.snapshot)

                }
            }
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

    private val _currentSnapshot = MutableLiveData<DocumentSnapshot>()
    val currentSnapshot = _currentSnapshot as LiveData<DocumentSnapshot>
    fun setCurrentSnapshot(snapshot: DocumentSnapshot) {
        _currentSnapshot.value = snapshot
    }
}