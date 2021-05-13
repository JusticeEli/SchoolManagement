package com.justice.schoolmanagement.presentation.ui.fees

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.presentation.ui.student.StudentsFragment
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import com.justice.schoolmanagement.utils.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class FeesAddEditViewModel @ViewModelInject constructor(private val repository: FeesRepository, @Assisted private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val _addEditEvents = Channel<FeesAddEditFragment.Event>()
    val addEditEvents = _addEditEvents.receiveAsFlow()

    private val _studentFeesStatus = Channel<Resource<DocumentSnapshot>>()
    val studentFeesStatus = _studentFeesStatus.receiveAsFlow()
    fun setEvent(event: FeesAddEditFragment.Event) {
        viewModelScope.launch {
            when (event) {
                is FeesAddEditFragment.Event.CheckUpdating -> {
                    checkIfUpdating()
                }
                is FeesAddEditFragment.Event.FetchFees -> {
                    startFetchingFees()

                }
                is FeesAddEditFragment.Event.AddEditFees -> {
                    startAddEditFees(event.studentFees)

                }
            }
        }

    }

    private suspend fun startAddEditFees(studentFees: StudentFees) {
        if (fieldIsEmpty(studentFees)) {
            _addEditFeesStatus.send(Resource.error(Exception("Please Fill The Fees!!")))
            return
        }
        trimFees(studentFees)

        try {
            studentFees.payedAmount.toInt()

        } catch (e: Exception) {
            _addEditFeesStatus.send(Resource.error(e))
            return
        }

        if (isUpdatingLiveData.value!!) {
            startUpdating(studentFees)

        } else {
            startAdding(studentFees)
        }


    }

    private suspend fun startAdding(studentFees: StudentFees) {
        repository.startAdding(currentStudent.value!!, studentFees).collect {
            _addEditFeesStatus.send(it)
        }
    }

    private suspend fun startUpdating(studentFees: StudentFees) {
        repository.startUpdating(currentFees.value!!, studentFees).collect {
            _addEditFeesStatus.send(it)
        }
    }

    private fun trimFees(studentFees: StudentFees) {
        studentFees.payedAmount = studentFees.payedAmount.trim()
    }

    private fun fieldIsEmpty(studentFees: StudentFees): Boolean {
        return studentFees.payedAmount.isBlank()
    }


    private val _addEditFeesStatus = Channel<Resource<StudentFees>>()
    val addEditFeesStatus = _addEditFeesStatus.receiveAsFlow()
    private suspend fun startFetchingFees() {
        val feesId = savedStateHandle.get<StudentFees>(STUDENT_FEES_ARGS)!!.id!!
        val studentId = currentStudent.value!!.id!!
        repository.startFetchingFees(studentId, feesId).collect {
            _studentFeesStatus.send(it)
        }
    }


    val getStudent = repository.getStudent(savedStateHandle.get<StudentData>(StudentsFragment.STUDENT_ARGS)!!.id!!)
    private val currentStudent = MutableLiveData<DocumentSnapshot>()
    fun setCurrentStudent(snapshot: DocumentSnapshot) {
        currentStudent.value = snapshot
    }

    private val currentFees = MutableLiveData<DocumentSnapshot>()
    fun setCurrentFees(snapshot: DocumentSnapshot) {
        currentFees.value = snapshot
    }

    private val _isUpdatingLiveData = MutableLiveData<Boolean>()
    val isUpdatingLiveData = _isUpdatingLiveData as LiveData<Boolean>
    fun setIsUpdating(isUpdating: Boolean) {
        _isUpdatingLiveData.value = isUpdating
    }

    private suspend fun checkIfUpdating() {
        val fees = savedStateHandle.get<StudentFees>(STUDENT_FEES_ARGS)
        if (fees == null) {
            _addEditEvents.send(FeesAddEditFragment.Event.CheckUpdating(false))
        } else {
            _addEditEvents.send(FeesAddEditFragment.Event.CheckUpdating(true))

        }
    }
}