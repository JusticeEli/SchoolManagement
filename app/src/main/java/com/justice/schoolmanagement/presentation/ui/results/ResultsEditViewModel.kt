package com.justice.schoolmanagement.presentation.ui.results

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.justice.schoolmanagement.presentation.ui.student.models.StudentMarks
import com.justice.schoolmanagement.utils.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ResultsEditViewModel @ViewModelInject constructor(private val repository: ResultsRepository) :
    ViewModel() {
    private val TAG = "ResultsEditViewModel"


    val _getStudentMarks = Channel<Resource<DocumentSnapshot>>()
    val getStudentMarks = _getStudentMarks.receiveAsFlow()


    private val _currentStudentMarks = MutableLiveData<DocumentSnapshot>()
    val currentStudentMarks = _currentStudentMarks as LiveData<DocumentSnapshot>
    fun setCurrentStudentMarks(snapshot: DocumentSnapshot) {
        _currentStudentMarks.value = snapshot
    }

    private val _editMarksStatus = Channel<Resource<DocumentSnapshot>>()
    val editMarksStatus = _editMarksStatus.receiveAsFlow()
    fun setEvent(event: ResultsEditFragment.Event) {

        viewModelScope.launch {
            when (event) {
                is ResultsEditFragment.Event.GetStudentMarks -> {
                    repository.getStudentMarks(event.studentMarks.id!!).collect {
                        _getStudentMarks.send(it)
                    }
                }
                is ResultsEditFragment.Event.SubmitClicked -> {
                    submitClicked(event.studentMarks)
                }
            }


        }


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
        val hashmapMarks=getHashMap(studentMarks)


        updateDatabase(hashmapMarks)


    }

    private fun getHashMap(studentMarks: StudentMarks): HashMap<String,String> {
     val map= hashMapOf<String,String>(
         "math" to studentMarks.math,
         "science" to studentMarks.science,
         "english" to studentMarks.english,
         "kiswahili" to studentMarks.kiswahili,
         "sst_cre" to studentMarks.sst_cre,

         )


        return map

    }

    private suspend fun updateDatabase(hashMap: HashMap<String,String>) {
        Log.d(TAG, "updateDatabase: studentMarks:$hashMap")
        val snapshot = currentStudentMarks.value!!
        try {

            snapshot.reference.set(hashMap, SetOptions.merge()).await()

            Log.d(TAG, "updateDatabase: end")

        } catch (e: Exception) {
            Log.e(TAG, "updateDatabase: ", e)
        }
    }

    private fun computeTotalMarks(studentMarks: StudentMarks) {
        studentMarks.totalMarks =
            studentMarks.math.toIntOrZero() + studentMarks.science.toIntOrZero() + studentMarks.english.toIntOrZero() + studentMarks.kiswahili.toIntOrZero() + studentMarks.sst_cre.toIntOrZero()
    }
}

private const val TAG = "ResultsEditViewModel"
fun String.toIntOrZero():Int{
    try {
     return   this.toInt()
    }catch (e:Exception)
    {
        Log.e(TAG, "toIntOrZero: ", e)
    }

   return 0
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
