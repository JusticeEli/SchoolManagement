package com.justice.schoolmanagement.presentation.ui.subjects

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.justice.schoolmanagement.utils.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SubjectsViewModel @ViewModelInject constructor(private val repository: SubjectsRepository) : ViewModel() {

    private val TAG = "SubjectsViewModel"
   
    val getAllTeachers = repository.getAllTeachers()

    private val _currentTeachersLiveData = MutableLiveData<List<DocumentSnapshot>>()
    val currentTeachersLiveData = _currentTeachersLiveData as LiveData<List<DocumentSnapshot>>
    fun setCurrentTeachersLiveData(snapshots: List<DocumentSnapshot>) {
        _currentTeachersLiveData.value = snapshots
    }

    private val _clickedResults = Channel<Resource<List<String>>>()
    val clickedResults = _clickedResults.receiveAsFlow()

    fun setEvent(event: SubjectsFragment.Event) {
        viewModelScope.launch {
            when (event) {
                is SubjectsFragment.Event.SubjectClicked -> {
                    subjectClicked(event.position)

                }
            }
        }
    }

    private suspend fun subjectClicked(position: Int) {
        when (position) {
            0 -> {
                val list = getMath()
                _clickedResults.send(Resource.success(list))
                }
            1 -> {
                val list = getScience()
                _clickedResults.send(Resource.success(list))
            }
            2 -> {
                val list = getEnglish()
                _clickedResults.send(Resource.success(list))
            }
            3 -> {
                val list = getKiswahili()
                _clickedResults.send(Resource.success(list))
            }
            4 -> {
                val list = getSst_cre()
                _clickedResults.send(Resource.success(list))
            }
        }

    }


    private fun getMath(): List<String> {
        Log.d(TAG, "getMath: ")
        val list = ArrayList<String>()
        currentTeachersLiveData.value!!.forEach {
            val teacherData=it.toObject(TeacherData::class.java)!!
            Log.d(TAG, "getMath: $teacherData")
            if (teacherData.subject == "Math") {
                list.add(teacherData.fullName)
            }
        }
        Log.d(TAG, "getMath: size: ${list.size}")
        return list
    }

    private fun getScience(): List<String> {
        val list = ArrayList<String>()
        currentTeachersLiveData.value!!.forEach {
            val teacherData=it.toObject(TeacherData::class.java)!!
            if (teacherData.subject == "Science") {
                list.add(teacherData.fullName)
            }
        }
        return list
    }

    private fun getEnglish(): List<String> {
        val list = ArrayList<String>()
        currentTeachersLiveData.value!!.forEach {
            val teacherData = it.toObject(TeacherData::class.java)!!
            if (teacherData.subject == "English") {
                list.add(teacherData.fullName)
            }
        }
        return list
    }
    private fun getKiswahili(): List<String> {
        val list = ArrayList<String>()
        currentTeachersLiveData.value!!.forEach {
            val teacherData=it.toObject(TeacherData::class.java)!!
            if (teacherData.subject == "Kiswahili") {
                list.add(teacherData.fullName)
            }
        }
        return list
    }

    private fun getSst_cre(): List<String> {
         val list = ArrayList<String>()
        currentTeachersLiveData.value!!.forEach {
            val teacherData=it.toObject(TeacherData::class.java)!!
            if (teacherData.subject == "sst_cre") {
                list.add(teacherData.fullName)
            }
        }
        return list
    }

}