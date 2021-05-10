package com.justice.schoolmanagement.presentation.ui.results

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.presentation.ui.student.models.STUDENT_MARKS_ARGS
import com.justice.schoolmanagement.presentation.ui.student.models.StudentMarks
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class ResultsEditViewModel @ViewModelInject constructor(private val repository: ResultsRepository, @Assisted private val savedStateHandle: SavedStateHandle, private val coroutineScope: CoroutineScope) : ViewModel() {
    private val TAG = "ResultsEditViewModel"

    val getStudentMarks = repository.getStudentMarks(savedStateHandle.get<StudentMarks>(STUDENT_MARKS_ARGS)!!.id!!)

    private val _currentStudentMarks = MutableLiveData<DocumentSnapshot>()
    val currentStudentMarks = _currentStudentMarks as LiveData<DocumentSnapshot>
    fun setCurrentStudentMarks(snapshot: DocumentSnapshot) {
        _currentStudentMarks.value = snapshot
    }

    private val _editMarksStatus = Channel<Resource<DocumentSnapshot>>()
    val editMarksStatus = _editMarksStatus.receiveAsFlow()
    fun setEvent(event: ResultsEditFragment.Event) {

        coroutineScope.launch(handler) {
            supervisorScope {
                viewModelScope.launch {
                    when (event) {
                        is ResultsEditFragment.Event.SubmitClicked -> {
                            submitClicked(event.studentMarks)
                        }
                    }
                }

            }
        }


    }

    val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
        Log.d(TAG, "Handler Error::${throwable.message} ")
    }

    private suspend fun submitClicked(studentMarks: StudentMarks) {
        Log.d(TAG, "submitClicked: studentMarks:$studentMarks")
        if (fieldsAreEmpty(studentMarks)) {
            _editMarksStatus.send(Resource.error(Exception("Please Fill All Fields!!")))
            return
        }
        trimData(studentMarks)

        try {
            studentMarks.math.toInt()
            studentMarks.science.toInt()
            studentMarks.english.toInt()
            studentMarks.kiswahili.toInt()
            studentMarks.sst_cre.toInt()

        } catch (e: Exception) {
            _editMarksStatus.send(Resource.error(java.lang.Exception("The Marks should Be integers")))
            return
        }

        if (marksAreNotValid(studentMarks)) {
            _editMarksStatus.send(Resource.error(java.lang.Exception("Some Marks Are not valid!!")))
            return
        }

        computeTotalMarks(studentMarks)

        updateDatabase(studentMarks)


    }

    private suspend fun updateDatabase(studentMarks: StudentMarks) {
        Log.d(TAG, "updateDatabase: studentMarks:$studentMarks")

        val map = mapOf<String,String>("math" to studentMarks.math)


        repository.updateDatabase(currentStudentMarks.value!!, map).collect {
            Log.d(TAG, "updateDatabase: status:${it.status.name}")
            _editMarksStatus.send(it)
        }


    }

    private fun computeTotalMarks(studentMarks: StudentMarks) {
        studentMarks.totalMarks = studentMarks.math.toInt() + studentMarks.science.toInt() + studentMarks.english.toInt() + studentMarks.kiswahili.toInt() + studentMarks.sst_cre.toInt()}
    }

    private fun marksAreNotValid(studentMarks: StudentMarks): Boolean {
        return (studentMarks.math.toInt() > 100
                || studentMarks.science.toInt() > 100
                || studentMarks.english.toInt() > 100
                || studentMarks.kiswahili.toInt() > 100
                || studentMarks.sst_cre.toInt() > 100)

    }

    private fun trimData(studentMarks: StudentMarks) {
        studentMarks.math = studentMarks.math.trim()
        studentMarks.science = studentMarks.science.trim()
        studentMarks.english = studentMarks.english.trim()
        studentMarks.kiswahili = studentMarks.kiswahili.trim()
        studentMarks.sst_cre = studentMarks.sst_cre.trim()
    }

    private fun fieldsAreEmpty(studentMarks: StudentMarks): Boolean {
        return (studentMarks.math.isBlank()
                || studentMarks.science.isBlank()
                || studentMarks.english.isBlank()
                || studentMarks.kiswahili.isBlank()
                || studentMarks.sst_cre.isBlank())
    }
